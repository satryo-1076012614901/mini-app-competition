package ac.sfj.miniappcompetition.ui.feature.match.view

import ac.sfj.miniappcompetition.ui.theme.extendedColors
import ac.sfj.miniappcompetition.ui.component.foundation.dashedBorder
import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.Match
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.model.MatchStatus
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.BracketTopology
import ac.sfj.miniappcompetition.data.rule.MatchResultRules
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.atom.ScoreText
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.HintText
import ac.sfj.miniappcompetition.ui.component.molecule.HintTone
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLabel
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLeading
import ac.sfj.miniappcompetition.ui.component.molecule.StatusTone
import ac.sfj.miniappcompetition.ui.component.organism.EmptyState
import ac.sfj.miniappcompetition.ui.component.organism.ErrorState
import ac.sfj.miniappcompetition.ui.component.organism.LoadingPlaceholder
import ac.sfj.miniappcompetition.ui.component.organism.ScrollableTabs
import ac.sfj.miniappcompetition.ui.component.organism.TabItem
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketUiState
import ac.sfj.miniappcompetition.ui.shared.displayName
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
internal fun participantName(id: Long?, participants: List<Participant>?): String =
    participants?.firstOrNull { it.id == id }?.name
        ?: id?.let { stringResource(R.string.match_participant_fallback, it) }.orEmpty()

/** Stateless detail-tab content. The host owns the ViewModel and snackbar. */
@Composable
fun BracketContent(
    state: BracketUiState,
    contentPadding: PaddingValues,
    onSelectRound: (Int) -> Unit,
    onInput: (Long) -> Unit,
    onUndo: (Long) -> Unit,
    onRefresh: () -> Unit,
    onParticipants: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topology = state.topology
    Column(modifier.fillMaxSize()) {
        if (state.syncError != null && state.matches != null) {
            MatchSyncWarning(onRefresh, !state.isRefreshing && !state.isMutating)
        }
        when {
            state.matches == null && state.syncError != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorState(stringResource(state.syncError.toErrorMessage().textRes), onRetry = onRefresh)
            }
            state.matches == null || state.competition == null -> LoadingPlaceholder()
            topology.roundCount == 0 -> Box(Modifier.weight(1f).fillMaxWidth().padding(contentPadding), contentAlignment = Alignment.Center) {
                EmptyState(title = stringResource(R.string.match_empty_title), message = stringResource(R.string.match_empty_body),
                    icon = AppIcons.AccountTree, actionLabel = stringResource(R.string.match_open_participants), onAction = onParticipants)
            }
            else -> {
                val round = state.selectedRound?.coerceIn(1, topology.roundCount)
                    ?: topology.initialRound(state.competition.status) ?: 1
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("bracket-match-list"),
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding,
                        top = Dimens.SpaceLg, bottom = contentPadding.calculateBottomPadding() + Dimens.SpaceLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    if (state.competition.status == CompetitionStatus.COMPLETED && topology.championId != null) {
                        item(key = "champion") {
                            ChampionCard(topology.finalMatch!!, state.participants)
                        }
                    }
                    item(key = "round-tabs") {
                        ScrollableTabs(
                            tabs = (1..topology.roundCount).map { index ->
                                val progress = topology.roundProgress(index)
                                TabItem(topology.roundName(index).displayName(), "${progress.completed}/${progress.total}")
                            }, selectedIndex = round - 1, onSelect = { onSelectRound(it + 1) },
                        )
                    }
                    items(topology.matchesInRound(round), key = { it.id }) { match ->
                        MatchCard(match, topology, state.participants, state.competition.status, state.canAct,
                            onInput = { onInput(match.id) }, onUndo = { onUndo(match.id) })
                    }
                    item { AppButton(stringResource(R.string.match_refresh), onRefresh,
                        variant = AppButtonVariant.Text, enabled = !state.isRefreshing && !state.isMutating) }
                }
            }
        }
    }
}

@Composable
fun MatchSyncWarning(onRefresh: () -> Unit, enabled: Boolean) {
    Column(Modifier.fillMaxWidth().padding(Dimens.SpaceLg)) {
        HintText(stringResource(R.string.match_sync_failed), icon = AppIcons.Error, tone = HintTone.Error)
        AppButton(stringResource(R.string.match_refresh), onRefresh, variant = AppButtonVariant.Text, enabled = enabled)
    }
}

@Composable
private fun MatchCard(
    match: Match, topology: BracketTopology, participants: List<Participant>?,
    status: CompetitionStatus, enabled: Boolean, onInput: () -> Unit, onUndo: () -> Unit,
) {
    val bye = topology.isBye(match)
    val shape = MaterialTheme.shapes.medium
    val ready = MatchResultRules.canSubmitResult(status, match) && !bye
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().then(if (bye) Modifier.dashedBorder(MaterialTheme.colorScheme.outline, shape) else Modifier),
        shape = shape,
        border = BorderStroke(if (ready) 2.dp else 1.dp,
            if (bye) androidx.compose.ui.graphics.Color.Transparent else if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(Dimens.SpaceLg), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.match_number, match.matchNumber), style = MaterialTheme.typography.labelLarge)
                StatusLabel(text = stringResource(when {
                    bye -> R.string.match_bye
                    match.status == MatchStatus.READY -> R.string.match_ready
                    match.status == MatchStatus.COMPLETED -> R.string.match_completed
                    else -> R.string.match_pending
                }), tone = if (ready) StatusTone.Primary else if (bye || match.status == MatchStatus.PENDING) StatusTone.Outline else StatusTone.Neutral,
                    leading = if (!bye && match.status == MatchStatus.COMPLETED) StatusLeading.Check else StatusLeading.None, dashed = bye)
            }
            for (slot in MatchSlot.entries) {
                if (bye && slot == MatchSlot.TWO) continue
                val id = match.participantId(slot)
                val source = topology.slotSource(match, slot)
                val name = if (id != null) participantName(id, participants) else if (source != null)
                    stringResource(R.string.match_source, topology.roundName(source.round).displayName(), source.matchNumber)
                    else stringResource(R.string.match_pending)
                val winner = id != null && id == match.winnerId
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    if (winner) Icon(AppIcons.CheckCircle, stringResource(R.string.match_winner), tint = MaterialTheme.colorScheme.primary)
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (winner) FontWeight.Bold else FontWeight.Normal)
                    if (!bye) ScoreText(match.score(slot), emphasized = winner)
                }
            }
            if (bye) {
                val next = topology.nextMatch(match)
                HintText(stringResource(R.string.match_auto_advance, next?.let { topology.roundName(it.round).displayName() }.orEmpty()))
            } else if (ready) {
                AppButton(stringResource(R.string.match_input), onInput, Modifier.align(Alignment.End), enabled = enabled)
            } else if (MatchResultRules.canUndo(status, match, topology)) {
                AppButton(stringResource(R.string.match_undo), onUndo, Modifier.align(Alignment.End),
                    variant = AppButtonVariant.Text, leadingIcon = AppIcons.Undo, enabled = enabled)
            }
        }
    }
}

@Composable
private fun ChampionCard(final: Match, participants: List<Participant>?) {
    val winner = participantName(final.winnerId, participants)
    val loser = participantName(if (final.winnerId == final.participant1Id) final.participant2Id else final.participant1Id, participants)
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.extendedColors.championContainer, contentColor = MaterialTheme.extendedColors.onChampionContainer) {
        Row(Modifier.padding(Dimens.SpaceLg), horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg), verticalAlignment = Alignment.CenterVertically) {
            Icon(AppIcons.Trophy, null)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.match_champion), style = MaterialTheme.typography.labelLarge)
                Text(winner, style = MaterialTheme.typography.headlineSmall)
                val winnerScore = if (final.winnerId == final.participant1Id) final.score1 else final.score2
                val loserScore = if (final.winnerId == final.participant1Id) final.score2 else final.score1
                Text(if (winnerScore != null && loserScore != null) stringResource(R.string.match_champion_result, "$winnerScore–$loserScore", loser)
                    else stringResource(R.string.match_champion_no_score, loser), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun BracketPreview() {
    MiniAppCompetitionTheme {
        val competition = Competition(1, "Futsal Cup Fakultas", ParticipantType.TEAM, CompetitionStatus.IN_MATCH, Instant.EPOCH)
        val participants = (1L..6L).map { Participant(it, 1, "Tim ${listOf("Garuda", "Rajawali", "Elang", "Merpati", "Kenari", "Cendrawasih")[it.toInt()-1]}", emptyList(), Instant.EPOCH) }
        val matches = listOf(
            Match(1,1,1,1,1,2,1,4,3,1,MatchStatus.COMPLETED),
            Match(2,1,1,2,3,4,4,4,0,2,MatchStatus.COMPLETED),
            Match(3,1,1,3,5,6,null,5,null,null,MatchStatus.READY),
            Match(4,1,2,1,1,4,null,6,null,null,MatchStatus.READY),
            Match(5,1,2,2,null,null,null,6,null,null,MatchStatus.PENDING),
            Match(6,1,3,1,null,null,null,null,null,null,MatchStatus.PENDING))
        BracketContent(BracketUiState(competition, participants, matches, selectedRound = 1), PaddingValues(), {}, {}, {}, {}, {})
    }
}
