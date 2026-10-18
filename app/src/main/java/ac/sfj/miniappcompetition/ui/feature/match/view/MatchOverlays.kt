package ac.sfj.miniappcompetition.ui.feature.match.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.rule.BracketShape
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.organism.ConfirmationDialog
import ac.sfj.miniappcompetition.ui.component.organism.FormBottomSheet
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketUiState
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketViewModel
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.MatchConfirmation
import ac.sfj.miniappcompetition.ui.shared.bracketSummary
import ac.sfj.miniappcompetition.ui.shared.displayName
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun MatchOverlays(state: BracketUiState, viewModel: BracketViewModel) {
    state.form?.let { form ->
        val match = state.topology.findMatch(form.matchId) ?: return@let
        FormBottomSheet(title = stringResource(R.string.match_result_title, match.matchNumber),
            subtitle = state.topology.roundName(match.round).displayName(),
            confirmLabel = stringResource(R.string.match_save), onConfirm = viewModel::submitResult,
            onDismiss = viewModel::dismissForm, confirmEnabled = state.canAct && form.canSave, confirmLoading = state.isMutating) {
            MatchResultFields(form, match, state.participants, state.canAct,
                viewModel::changeScore, viewModel::clearScores, viewModel::selectWinner)
        }
    }
    val confirmation = state.confirmation ?: return
    val competition = state.competition ?: return
    val title: String
    val body: String
    val label: String
    when (confirmation) {
        MatchConfirmation.Generate -> {
            val shape = BracketShape.of(state.participants.orEmpty().size) ?: return
            title = stringResource(R.string.match_generate_title)
            body = stringResource(R.string.match_generate_body, bracketSummary(shape))
            label = stringResource(R.string.match_generate)
        }
        MatchConfirmation.Reset -> {
            title = stringResource(R.string.match_reset_title)
            body = stringResource(R.string.match_reset_body, competition.name)
            label = stringResource(R.string.match_reset)
        }
        is MatchConfirmation.Undo -> {
            val match = state.topology.findMatch(confirmation.matchId) ?: return
            val next = state.topology.nextMatch(match)
            title = stringResource(R.string.match_undo_title, match.matchNumber)
            val score = "${match.score1 ?: "–"}–${match.score2 ?: "–"}"
            val winner = participantName(match.winnerId, state.participants)
            body = if (next == null) stringResource(R.string.match_undo_final, score, winner)
                else stringResource(R.string.match_undo_body, score, winner,
                    "${state.topology.roundName(next.round).displayName()} ${stringResource(R.string.match_number, next.matchNumber)}")
            label = stringResource(R.string.match_undo)
        }
    }
    ConfirmationDialog(title, body, label, viewModel::confirm, viewModel::dismissConfirmation,
        destructive = confirmation == MatchConfirmation.Reset, confirmLoading = state.isMutating)
}
