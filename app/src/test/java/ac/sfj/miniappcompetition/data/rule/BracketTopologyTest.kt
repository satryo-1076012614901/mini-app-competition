package ac.sfj.miniappcompetition.data.rule

import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.model.MatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BracketTopologyTest {

    @Test
    fun isBye_matchesPlannerForAllParticipantCounts() {
        for (participants in 2..16) {
            val simulator = BracketSimulator(participants)
            val topology = BracketTopology(simulator.matches())
            for (match in topology.matches) {
                assertEquals(
                    "$participants peserta, babak ${match.round} match ${match.matchNumber}",
                    match.id in simulator.plannedByeIds,
                    topology.isBye(match),
                )
            }
        }
    }

    @Test
    fun sixParticipants_semifinalMatchTwoIsByeFromRoundOneMatchThree() {
        // Contoh 6 peserta pada Referensi UI hal. 4.
        val simulator = BracketSimulator(6)
        val topology = BracketTopology(simulator.matches())
        val semifinalBye = topology.matchesInRound(2).single { it.matchNumber == 2 }

        assertTrue(topology.isBye(semifinalBye))
        assertEquals(simulator.match(1, 3).id, topology.slotSource(semifinalBye, MatchSlot.ONE)?.id)
        assertNull(topology.slotSource(semifinalBye, MatchSlot.TWO))
        assertEquals(RoundName.Numbered(1), topology.roundName(1))
        assertEquals(RoundName.Semifinal, topology.roundName(2))
        assertEquals(RoundName.Final, topology.roundName(3))

        val final = checkNotNull(topology.finalMatch)
        assertEquals(topology.matchesInRound(2).map { it.id }, topology.sourcesOf(final).map { it.id })
    }

    @Test
    fun slotSource_winnerFillsMatchingSlotThroughoutPlay() {
        for (participants in 2..16) {
            val simulator = BracketSimulator(participants)
            val random = Random(participants)
            while (simulator.competitionStatus == CompetitionStatus.IN_MATCH) {
                assertSlotsFollowSources(participants, BracketTopology(simulator.matches()))
                val ready = simulator.matches().filter { it.status == MatchStatus.READY }
                val match = ready[random.nextInt(ready.size)]
                simulator.submit(match.id, if (random.nextBoolean()) MatchSlot.ONE else MatchSlot.TWO)
            }
            assertSlotsFollowSources(participants, BracketTopology(simulator.matches()))
        }
    }

    private fun assertSlotsFollowSources(participants: Int, topology: BracketTopology) {
        for (match in topology.matches.filter { it.round > 1 }) {
            for (slot in MatchSlot.entries) {
                val source = topology.slotSource(match, slot)
                val participantId = match.participantId(slot)
                val label = "$participants peserta, babak ${match.round} match ${match.matchNumber} slot $slot"
                if (source == null) {
                    assertTrue(label, topology.isBye(match) && slot == MatchSlot.TWO)
                    assertNull(label, participantId)
                } else {
                    assertEquals(label, match.id, source.nextMatchId)
                    assertEquals(label, source.winnerId, participantId)
                }
            }
        }
    }

    @Test
    fun roundProgress_excludesByes() {
        val simulator = BracketSimulator(6)
        val topology = BracketTopology(simulator.matches())

        assertEquals(RoundProgress(completed = 0, total = 3), topology.roundProgress(1))
        assertEquals(RoundProgress(completed = 0, total = 1), topology.roundProgress(2))
        assertEquals(RoundProgress(completed = 0, total = 1), topology.roundProgress(3))
        for (participants in 2..16) {
            val all = BracketTopology(BracketSimulator(participants).matches())
            assertEquals(participants - 1, (1..all.roundCount).sumOf { all.roundProgress(it).total })
        }
    }

    @Test
    fun initialRound_firstRoundWithReadyMatchOrFinalWhenCompleted() {
        val simulator = BracketSimulator(6)
        assertEquals(1, BracketTopology(simulator.matches()).initialRound(CompetitionStatus.IN_MATCH))

        simulator.matches().filter { it.round == 1 }.forEach { simulator.submit(it.id, MatchSlot.ONE) }
        assertEquals(2, BracketTopology(simulator.matches()).initialRound(CompetitionStatus.IN_MATCH))

        playToEnd(simulator)
        val finished = BracketTopology(simulator.matches())
        assertEquals(3, finished.initialRound(CompetitionStatus.COMPLETED))
        assertNull(BracketTopology(emptyList()).initialRound(CompetitionStatus.OPEN))
    }

    @Test
    fun championId_isFinalWinnerOnlyAfterFinalCompleted() {
        val simulator = BracketSimulator(5)
        assertNull(BracketTopology(simulator.matches()).championId)

        playToEnd(simulator)
        val topology = BracketTopology(simulator.matches())
        val final = checkNotNull(topology.finalMatch)
        assertEquals(CompetitionStatus.COMPLETED, simulator.competitionStatus)
        assertEquals(final.winnerId, topology.championId)
        assertFalse(topology.championId == null)
    }

    @Test
    fun emptyBracket_hasNoRoundsAndNoFinal() {
        val topology = BracketTopology(emptyList())

        assertEquals(0, topology.roundCount)
        assertNull(topology.finalMatch)
        assertNull(topology.championId)
    }

    private fun playToEnd(simulator: BracketSimulator) {
        while (simulator.competitionStatus == CompetitionStatus.IN_MATCH) {
            val match = simulator.matches().first { it.status == MatchStatus.READY }
            simulator.submit(match.id, MatchSlot.ONE)
        }
    }
}
