package ac.sfj.miniappcompetition.ui.feature.match.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.Match
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.model.MatchStatus
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.HintText
import ac.sfj.miniappcompetition.ui.component.molecule.HintTone
import ac.sfj.miniappcompetition.ui.component.molecule.RadioOptionRow
import ac.sfj.miniappcompetition.ui.component.molecule.ScoreStepper
import ac.sfj.miniappcompetition.ui.component.organism.FormSheetContent
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.MatchResultForm
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.time.Instant

@Composable
fun MatchResultFields(
    form: MatchResultForm,
    match: Match,
    participants: List<Participant>?,
    enabled: Boolean,
    onScoreChange: (MatchSlot, Boolean) -> Unit,
    onClearScores: () -> Unit,
    onSelectWinner: (MatchSlot) -> Unit,
) {
    for (slot in MatchSlot.entries) {
        val score = if (slot == MatchSlot.ONE) form.score1 else form.score2
        ScoreStepper(participantName(match.participantId(slot), participants), score,
            onDecrement = { onScoreChange(slot, false) }, onIncrement = { onScoreChange(slot, true) },
            decrementEnabled = enabled && score != null && score > 0,
            incrementEnabled = enabled && score != Int.MAX_VALUE)
    }
    AppButton(stringResource(R.string.match_clear_scores), onClearScores,
        variant = AppButtonVariant.Text, enabled = enabled)
    HintText(stringResource(if (form.automaticWinner == null) R.string.match_manual_winner else R.string.match_auto_winner))
    Text(stringResource(R.string.match_winner), style = MaterialTheme.typography.titleSmall)
    Column(Modifier.selectableGroup()) {
        for (slot in MatchSlot.entries) RadioOptionRow(participantName(match.participantId(slot), participants),
            form.winner == slot, onClick = { onSelectWinner(slot) }, enabled = enabled && form.automaticWinner == null)
    }
    form.error?.let { HintText(stringResource(it.toErrorMessage().textRes), icon = AppIcons.Error, tone = HintTone.Error) }
}

@ComponentPreviews
@Composable
private fun ResultPreview() {
    MiniAppCompetitionTheme {
        val people = listOf(Participant(1,1,"Tim Kenari", emptyList(), Instant.EPOCH),
            Participant(2,1,"Tim Cendrawasih", emptyList(), Instant.EPOCH))
        FormSheetContent(stringResource(R.string.match_result_title, 3), stringResource(R.string.match_round, 1),
            stringResource(R.string.match_save), {}, stringResource(R.string.action_cancel), {}, true, false) {
            MatchResultFields(MatchResultForm(3,1,1,MatchSlot.ONE),
                Match(3,1,1,3,1,2,null,null,null,null,MatchStatus.READY), people, true, { _, _ -> }, {}, {})
        }
    }
}
