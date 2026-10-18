package ac.sfj.miniappcompetition.ui.feature.match.viewmodel

import ac.sfj.miniappcompetition.MiniCompetitionApplication
import ac.sfj.miniappcompetition.data.model.apiCode
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.Match
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.model.MatchStatus
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.data.repository.CompetitionRepository
import ac.sfj.miniappcompetition.data.repository.MatchRepository
import ac.sfj.miniappcompetition.data.repository.ParticipantRepository
import ac.sfj.miniappcompetition.data.rule.BracketTopology
import ac.sfj.miniappcompetition.data.rule.CompetitionRules
import ac.sfj.miniappcompetition.data.rule.MatchResultRules
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BracketNavigation { Participants, Bracket }
sealed interface MatchConfirmation {
    data object Generate : MatchConfirmation
    data object Reset : MatchConfirmation
    data class Undo(val matchId: Long) : MatchConfirmation
}
data class MatchMessage(val id: Long, val error: AppError? = null, val savedMatchId: Long? = null)
data class BracketUiState(
    val competition: Competition? = null,
    val participants: List<Participant>? = null,
    val matches: List<Match>? = null,
    val selectedRound: Int? = null,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val syncError: AppError? = null,
    val form: MatchResultForm? = null,
    val confirmation: MatchConfirmation? = null,
    val message: MatchMessage? = null,
    val navigation: BracketNavigation? = null,
    val competitionMissing: Boolean = false,
) {
    val topology get() = BracketTopology(matches.orEmpty())
    val canAct get() = competition != null && participants != null && matches != null &&
        !isRefreshing && !isMutating && syncError == null && !competitionMissing
    val canGenerate get() = canAct && matches.orEmpty().isEmpty() &&
        CompetitionRules.canGenerateBracket(competition!!.status, participants!!.size)
    val canReset get() = canAct && CompetitionRules.canResetBracket(competition!!.status)
}

/** Shared by both detail tabs; repositories remain the source of competition status and names. */
class BracketViewModel(
    private val competitionId: Long,
    private val competitions: CompetitionRepository,
    private val participants: ParticipantRepository,
    private val matches: MatchRepository,
) : ViewModel() {
    private val state = MutableStateFlow(BracketUiState())
    val uiState = state.asStateFlow()
    private var messageId = 0L
    private var pendingNavigation: BracketNavigation? = null

    init {
        viewModelScope.launch {
            competitions.observeCompetition(competitionId).collect { competition ->
                state.update { old ->
                    val completed = competition?.status == CompetitionStatus.COMPLETED &&
                        old.competition?.status != CompetitionStatus.COMPLETED
                    old.copy(competition = competition,
                        selectedRound = if (completed) old.topology.roundCount.takeIf { it > 0 } else old.selectedRound)
                }
                reconcile()
            }
        }
        viewModelScope.launch {
            participants.observeParticipants(competitionId).collect { list -> state.update { it.copy(participants = list) } }
        }
        viewModelScope.launch {
            matches.observeMatches(competitionId).collect { list ->
                state.update { it.copy(matches = list) }
                reconcile()
            }
        }
        refresh()
    }

    private fun reconcile() {
        state.update { old ->
            val topology = old.topology
            val round = when {
                topology.roundCount == 0 -> null
                old.selectedRound in 1..topology.roundCount -> old.selectedRound
                else -> topology.initialRound(old.competition?.status ?: CompetitionStatus.OPEN)
            }
            val form = old.form?.takeIf { form ->
                old.competition?.status == CompetitionStatus.IN_MATCH &&
                    topology.findMatch(form.matchId)?.status == MatchStatus.READY
            }
            old.copy(selectedRound = round, form = form)
        }
    }

    fun selectRound(round: Int) {
        if (round in 1..state.value.topology.roundCount) state.update { it.copy(selectedRound = round) }
    }

    fun refresh() {
        if (state.value.isRefreshing || state.value.isMutating) return
        state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch { synchronize() }
    }

    /** Reads only: a successful mutation is never repeated when synchronization fails. */
    private suspend fun synchronize() {
        state.update { it.copy(isRefreshing = true) }
        val results = listOf(competitions.refreshCompetition(competitionId),
            participants.refreshParticipants(competitionId), matches.refreshMatches(competitionId))
        val errors = results.filterIsInstance<AppResult.Failure>().map { it.error }
        val missing = errors.any { it.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND }
        state.update { it.copy(isRefreshing = false, syncError = errors.firstOrNull(),
            competitionMissing = missing, navigation = if (errors.isEmpty()) pendingNavigation else it.navigation) }
        if (errors.isEmpty()) pendingNavigation = null
        reconcile()
    }

    fun openResult(matchId: Long) {
        val current = state.value
        val match = current.topology.findMatch(matchId) ?: return
        if (!current.canAct || !MatchResultRules.canSubmitResult(current.competition!!.status, match)) return
        state.update { it.copy(form = MatchResultForm(matchId)) }
    }
    fun dismissForm() { if (!state.value.isMutating) state.update { it.copy(form = null) } }
    fun changeScore(slot: MatchSlot, increment: Boolean) {
        if (!state.value.canAct) return
        state.update { old ->
            val form = old.form ?: return@update old
            val score = if (slot == MatchSlot.ONE) form.score1 else form.score2
            val next = if (increment) (score ?: 0).let { if (it == Int.MAX_VALUE) it else it + 1 }
                else score?.let { (it - 1).coerceAtLeast(0) }
            old.copy(form = form.withScore(slot, next))
        }
    }
    fun clearScores() {
        if (state.value.canAct) state.update { it.copy(form = it.form?.copy(score1 = null, score2 = null, manualWinner = null, error = null)) }
    }
    fun selectWinner(slot: MatchSlot) {
        if (state.value.canAct) state.update { old ->
            old.copy(form = old.form?.takeIf { it.automaticWinner == null }?.copy(manualWinner = slot, error = null) ?: old.form)
        }
    }
    fun requestGenerate() { if (state.value.canGenerate) state.update { it.copy(confirmation = MatchConfirmation.Generate) } }
    fun requestReset() { if (state.value.canReset) state.update { it.copy(confirmation = MatchConfirmation.Reset) } }
    fun requestUndo(matchId: Long) {
        if (canUndo(matchId)) state.update { it.copy(confirmation = MatchConfirmation.Undo(matchId)) }
    }
    private fun canUndo(matchId: Long): Boolean {
        val current = state.value
        val match = current.topology.findMatch(matchId) ?: return false
        return current.canAct && MatchResultRules.canUndo(current.competition!!.status, match, current.topology)
    }
    fun dismissConfirmation() { if (!state.value.isMutating) state.update { it.copy(confirmation = null) } }
    fun confirm() {
        when (val confirmation = state.value.confirmation) {
            MatchConfirmation.Generate -> if (state.value.canGenerate) mutate(BracketNavigation.Bracket) { matches.generateBracket(competitionId) }
            MatchConfirmation.Reset -> if (state.value.canReset) mutate(BracketNavigation.Participants) { matches.resetBracket(competitionId) }
            is MatchConfirmation.Undo -> undo(confirmation.matchId)
            null -> Unit
        }
    }
    /** Snackbar undo deliberately bypasses confirmation, but rechecks current topology. */
    fun undo(matchId: Long) {
        if (canUndo(matchId)) mutate { matches.undoResult(competitionId, matchId) }
    }
    fun submitResult() {
        val current = state.value
        val form = current.form ?: return
        val match = current.topology.findMatch(form.matchId) ?: return
        if (!current.canAct || !form.canSave || !MatchResultRules.canSubmitResult(current.competition!!.status, match)) return
        val winnerId = match.participantId(form.winner!!) ?: return
        mutate(savedMatchId = match.id) { matches.submitResult(competitionId, match.id, winnerId, form.score1, form.score2) }
    }
    private fun mutate(
        navigation: BracketNavigation? = null,
        savedMatchId: Long? = null,
        action: suspend () -> AppResult<*>,
    ) {
        if (!state.value.canAct) return
        state.update { it.copy(isMutating = true) }
        viewModelScope.launch {
            when (val result = action()) {
                is AppResult.Success -> {
                    pendingNavigation = navigation
                    state.update { it.copy(form = null, confirmation = null) }
                    synchronize()
                    state.update { it.copy(message = MatchMessage(++messageId, savedMatchId = savedMatchId)) }
                }
                is AppResult.Failure -> {
                    val stale = result.error.apiCode in setOf(ApiErrorCode.COMPETITION_NOT_OPEN,
                        ApiErrorCode.COMPETITION_NOT_IN_MATCH, ApiErrorCode.MATCH_NOT_READY,
                        ApiErrorCode.MATCH_UNDO_NOT_ALLOWED, ApiErrorCode.MATCH_NOT_FOUND,
                        ApiErrorCode.COMPETITION_NOT_FOUND, ApiErrorCode.DATA_INTEGRITY_VIOLATION)
                    state.update { old ->
                        if (old.form != null && !stale) old.copy(form = old.form.copy(error = result.error))
                        else old.copy(form = null, confirmation = null, message = MatchMessage(++messageId, error = result.error))
                    }
                    if (stale) synchronize()
                }
            }
            state.update { it.copy(isMutating = false) }
        }
    }
    fun messageShown(id: Long) { state.update { if (it.message?.id == id) it.copy(message = null) else it } }
    fun navigationHandled() { state.update { it.copy(navigation = null) } }

    companion object {
        fun factory(competitionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MiniCompetitionApplication
                BracketViewModel(competitionId, app.competitionContainer.competitionRepository,
                    app.participantContainer.participantRepository, app.matchContainer.matchRepository)
            }
        }
    }
}
