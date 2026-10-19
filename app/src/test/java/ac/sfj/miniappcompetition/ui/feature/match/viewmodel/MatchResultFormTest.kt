package ac.sfj.miniappcompetition.ui.feature.match.viewmodel

import ac.sfj.miniappcompetition.data.model.MatchSlot
import org.junit.Assert.*
import org.junit.Test

class MatchResultFormTest {
    @Test fun emptyAndTiedScores_requireManualWinner() {
        assertFalse(MatchResultForm(1).canSave)
        assertTrue(MatchResultForm(1, manualWinner = MatchSlot.TWO).canSave)
        assertFalse(MatchResultForm(1, 2, 2).canSave)
        assertTrue(MatchResultForm(1, 2, 2, MatchSlot.ONE).canSave)
    }
    @Test fun unequalScores_overrideManualWinner() {
        val form = MatchResultForm(1, 0, 2, MatchSlot.ONE)
        assertEquals(MatchSlot.TWO, form.winner)
        assertTrue(form.canSave)
    }
    @Test fun editingScores_clearsOldManualWinner() {
        val form = MatchResultForm(1, 1, 1, MatchSlot.ONE).withScore(MatchSlot.ONE, 2).withScore(MatchSlot.TWO, 2)
        assertNull(form.winner)
        assertFalse(form.canSave)
    }
    @Test fun partialScores_allowManualWinner_butNegativeScoresDoNot() {
        assertTrue(MatchResultForm(1, null, 3, MatchSlot.ONE).canSave)
        assertFalse(MatchResultForm(1, -1, 3, MatchSlot.TWO).canSave)
    }
}
