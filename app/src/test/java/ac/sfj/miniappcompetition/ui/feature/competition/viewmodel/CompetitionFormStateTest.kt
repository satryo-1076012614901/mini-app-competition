package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.NameError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Aturan sheet Buat dan Ubah kompetisi (Referensi UI hal. 7). */
class CompetitionFormStateTest {

    private val competition = Competition(
        id = 7,
        name = "Turnamen Catur Antar Kelas",
        participantType = ParticipantType.INDIVIDUAL,
        status = CompetitionStatus.OPEN,
        createdAt = Instant.parse("2026-10-05T03:00:00Z"),
    )

    @Test
    fun create_requiresValidNameAndChosenParticipantType() {
        val empty = CompetitionFormState.create()
        assertFalse(empty.isEdit)
        assertNull(empty.participantType)
        assertFalse(empty.canSave)

        val named = empty.withName("Futsal Cup")
        assertFalse(named.canSave)
        assertTrue(named.withParticipantType(ParticipantType.TEAM).canSave)
    }

    @Test
    fun name_isValidatedAfterTrimming() {
        val form = CompetitionFormState.create().withParticipantType(ParticipantType.TEAM)

        assertEquals(NameError.BLANK, form.withName("   ").nameError)
        assertEquals(NameError.TOO_LONG, form.withName("a".repeat(256)).nameError)
        assertNull(form.withName("  " + "a".repeat(255) + "  ").nameError)
        assertFalse(form.withName("   ").canSave)
    }

    @Test
    fun saving_disablesSave() {
        val form = CompetitionFormState.create().withName("Futsal Cup").withParticipantType(ParticipantType.TEAM)
        assertFalse(form.copy(isSaving = true).canSave)
    }

    @Test
    fun edit_isPrefilledAndLockedOnlyWhenParticipantsExist() {
        val unknownCount = CompetitionFormState.edit(competition)
        assertTrue(unknownCount.isEdit)
        assertEquals(competition.name, unknownCount.name)
        assertEquals(ParticipantType.INDIVIDUAL, unknownCount.participantType)
        assertFalse(unknownCount.participantTypeLocked)

        assertFalse(CompetitionFormState.edit(competition, participantCount = 0).participantTypeLocked)
        assertTrue(CompetitionFormState.edit(competition, participantCount = 3).participantTypeLocked)
    }

    @Test
    fun lockParticipantType_restoresSavedTypeAndIgnoresFurtherChanges() {
        val locked = CompetitionFormState.edit(competition)
            .withParticipantType(ParticipantType.TEAM)
            .copy(isSaving = true)
            .lockParticipantType()

        assertEquals(ParticipantType.INDIVIDUAL, locked.participantType)
        assertTrue(locked.participantTypeLocked)
        assertFalse(locked.isSaving)
        assertEquals(ParticipantType.INDIVIDUAL, locked.withParticipantType(ParticipantType.TEAM).participantType)
    }
}
