package ac.sfj.miniappcompetition.ui.feature.participant.viewmodel

import ac.sfj.miniappcompetition.MiniCompetitionApplication
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.repository.ParticipantRepository
import ac.sfj.miniappcompetition.data.rule.ParticipantRules
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParticipantUiState(
    val participants: List<Participant>? = null,
    val isRefreshing: Boolean = false,
    val loadError: AppError? = null,
    val form: ParticipantFormState? = null,
    val participantToDelete: Participant? = null,
    val isDeleting: Boolean = false,
    val actionError: AppError? = null,
)

class ParticipantViewModel(
    private val competitionId: Long,
    private val participantRepository: ParticipantRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticipantUiState())
    val uiState: StateFlow<ParticipantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            participantRepository
                .observeParticipants(competitionId)
                .filterNotNull()
                .collect { participants ->
                    _uiState.update {
                        it.copy(
                            participants = participants,
                            loadError = null,
                        )
                    }
                }
        }

        refresh()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return

        _uiState.update {
            it.copy(
                isRefreshing = true,
                loadError = null,
            )
        }

        viewModelScope.launch {
            val result =
                participantRepository.refreshParticipants(competitionId)

            _uiState.update { state ->
                when (result) {
                    is AppResult.Success ->
                        state.copy(isRefreshing = false)

                    is AppResult.Failure ->
                        state.copy(
                            isRefreshing = false,
                            loadError = result.error,
                        )
                }
            }
        }
    }

    fun openCreateForm(participantType: ParticipantType) {
        _uiState.update {
            it.copy(
                form = ParticipantFormState.create(participantType),
                actionError = null,
            )
        }
    }

    fun openEditForm(
        participantType: ParticipantType,
        participant: Participant,
    ) {
        _uiState.update {
            it.copy(
                form = ParticipantFormState.edit(
                    participantType = participantType,
                    participant = participant,
                ),
                actionError = null,
            )
        }
    }

    fun closeForm() {
        _uiState.update { state ->
            if (state.form?.isSaving == true) {
                state
            } else {
                state.copy(form = null)
            }
        }
    }

    fun updateFormName(name: String) {
        _uiState.update { state ->
            state.copy(
                form = state.form?.copy(
                    name = name,
                    error = null,
                ),
            )
        }
    }

    fun updateMember(index: Int, name: String) {
        _uiState.update { state ->
            val form = state.form ?: return@update state
            if (index !in form.members.indices) return@update state

            state.copy(
                form = form.copy(
                    members = form.members.mapIndexed { memberIndex, member ->
                        if (memberIndex == index) name else member
                    },
                    error = null,
                ),
            )
        }
    }

    fun addMember() {
        _uiState.update { state ->
            val form = state.form ?: return@update state

            state.copy(
                form = form.copy(
                    members = form.members + "",
                ),
            )
        }
    }

    fun removeMember(index: Int) {
        _uiState.update { state ->
            val form = state.form ?: return@update state
            if (index !in form.members.indices) return@update state

            state.copy(
                form = form.copy(
                    members = form.members.filterIndexed { memberIndex, _ ->
                        memberIndex != index
                    },
                ),
            )
        }
    }

    fun submitForm() {
        val form = _uiState.value.form ?: return
        if (!form.canSave) return

        _uiState.update {
            it.copy(
                form = form.copy(
                    isSaving = true,
                    error = null,
                ),
            )
        }

        viewModelScope.launch {
            val name = ParticipantRules.normalizeName(form.name)
            val members = ParticipantRules.membersForRequest(
                participantType = form.participantType,
                members = form.members,
            )

            val result =
                if (form.participantId == null) {
                    participantRepository.createParticipant(
                        competitionId = competitionId,
                        name = name,
                        members = members,
                    )
                } else {
                    participantRepository.updateParticipant(
                        competitionId = competitionId,
                        participantId = form.participantId,
                        name = name,
                        members = members,
                    )
                }

            _uiState.update { state ->
                when (result) {
                    is AppResult.Success ->
                        state.copy(form = null)

                    is AppResult.Failure ->
                        state.copy(
                            form = state.form?.copy(
                                isSaving = false,
                                error = result.error,
                            ),
                        )
                }
            }
        }
    }

    fun requestDelete(participant: Participant) {
        _uiState.update {
            it.copy(
                participantToDelete = participant,
                actionError = null,
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { state ->
            if (state.isDeleting) {
                state
            } else {
                state.copy(
                    participantToDelete = null,
                    actionError = null,
                )
            }
        }
    }

    fun confirmDelete() {
        val participant =
            _uiState.value.participantToDelete ?: return

        if (_uiState.value.isDeleting) return

        _uiState.update {
            it.copy(
                isDeleting = true,
                actionError = null,
            )
        }

        viewModelScope.launch {
            val result =
                participantRepository.deleteParticipant(
                    competitionId = competitionId,
                    participantId = participant.id,
                )

            _uiState.update { state ->
                when (result) {
                    is AppResult.Success ->
                        state.copy(
                            participantToDelete = null,
                            isDeleting = false,
                        )

                    is AppResult.Failure ->
                        state.copy(
                            isDeleting = false,
                            actionError = result.error,
                        )
                }
            }
        }
    }

    companion object {
        fun factory(
            competitionId: Long,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application =
                        this[APPLICATION_KEY]
                                as MiniCompetitionApplication

                    ParticipantViewModel(
                        competitionId = competitionId,
                        participantRepository =
                            application
                                .participantContainer
                                .participantRepository,
                    )
                }
            }
    }
}