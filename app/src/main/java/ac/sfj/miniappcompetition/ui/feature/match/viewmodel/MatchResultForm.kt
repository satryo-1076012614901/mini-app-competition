package ac.sfj.miniappcompetition.ui.feature.match.viewmodel

import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.rule.MatchResultRules

data class MatchResultForm(
    val matchId: Long,
    val score1: Int? = null,
    val score2: Int? = null,
    val manualWinner: MatchSlot? = null,
    val error: AppError? = null,
) {
    val automaticWinner get() = MatchResultRules.winnerByScore(score1, score2)
    val winner get() = automaticWinner ?: manualWinner
    val canSave get() = MatchResultRules.validateResult(score1, score2, winner) == null

    fun withScore(slot: MatchSlot, value: Int?): MatchResultForm = when (slot) {
        MatchSlot.ONE -> copy(score1 = value, manualWinner = null, error = null)
        MatchSlot.TWO -> copy(score2 = value, manualWinner = null, error = null)
    }
}
