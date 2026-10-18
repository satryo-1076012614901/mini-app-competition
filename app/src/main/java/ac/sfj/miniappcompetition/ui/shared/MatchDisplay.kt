package ac.sfj.miniappcompetition.ui.shared

import ac.sfj.miniappcompetition.data.rule.BracketShape
import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.rule.RoundName
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun RoundName.displayName(): String = when (this) {
    RoundName.Final -> stringResource(R.string.match_final)
    RoundName.Semifinal -> stringResource(R.string.match_semifinal)
    is RoundName.Numbered -> stringResource(R.string.match_round, round)
}

@Composable
fun bracketSummary(shape: BracketShape): String = stringResource(R.string.match_shape,
    shape.participantCount, shape.roundCount, shape.playedMatchCount, shape.byeCount)
