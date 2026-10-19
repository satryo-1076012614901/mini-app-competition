package ac.sfj.miniappcompetition.data.rule

import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.CompetitionStatus.COMPLETED
import ac.sfj.miniappcompetition.data.model.CompetitionStatus.IN_MATCH
import ac.sfj.miniappcompetition.data.model.CompetitionStatus.OPEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Baris tabel "Aturan status", Referensi UI hal. 3. */
class CompetitionRulesTest {

    private fun allowedStatuses(rule: (CompetitionStatus) -> Boolean): Set<CompetitionStatus> =
        CompetitionStatus.entries.filter(rule).toSet()

    @Test
    fun editDeleteAndManageParticipants_onlyWhenOpen() {
        assertEquals(setOf(OPEN), allowedStatuses(CompetitionRules::canEdit))
        assertEquals(setOf(OPEN), allowedStatuses(CompetitionRules::canDelete))
        assertEquals(setOf(OPEN), allowedStatuses(CompetitionRules::canManageParticipants))
    }

    @Test
    fun generateBracket_onlyWhenOpenWithTwoToSixteenParticipants() {
        assertFalse(CompetitionRules.canGenerateBracket(OPEN, participantCount = 1))
        assertTrue(CompetitionRules.canGenerateBracket(OPEN, participantCount = 2))
        assertTrue(CompetitionRules.canGenerateBracket(OPEN, participantCount = 16))
        assertFalse(CompetitionRules.canGenerateBracket(OPEN, participantCount = 17))
        assertFalse(CompetitionRules.canGenerateBracket(IN_MATCH, participantCount = 8))
        assertFalse(CompetitionRules.canGenerateBracket(COMPLETED, participantCount = 8))
    }

    @Test
    fun resetBracket_onlyWhenInMatch() {
        assertEquals(setOf(IN_MATCH), allowedStatuses(CompetitionRules::canResetBracket))
    }

    @Test
    fun participantType_lockedOnceCompetitionHasParticipants() {
        assertFalse(CompetitionRules.isParticipantTypeLocked(participantCount = 0))
        assertTrue(CompetitionRules.isParticipantTypeLocked(participantCount = 1))
    }

    @Test
    fun validateName_trimsThenChecksBlankAndLength() {
        assertEquals(NameError.BLANK, CompetitionRules.validateName(""))
        assertEquals(NameError.BLANK, CompetitionRules.validateName("   "))
        assertNull(CompetitionRules.validateName("Futsal Cup Fakultas"))
        assertNull(CompetitionRules.validateName("a".repeat(255)))
        assertNull(CompetitionRules.validateName("  " + "a".repeat(255) + "  "))
        assertEquals(NameError.TOO_LONG, CompetitionRules.validateName("a".repeat(256)))
        assertEquals("Futsal Cup", CompetitionRules.normalizeName("  Futsal Cup  "))
    }
}
