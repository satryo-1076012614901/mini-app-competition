//package ac.sfj.miniappcompetition.data.rule

//import ac.sfj.miniappcompetition.data.model.CompetitionStatus
//import ac.sfj.miniappcompetition.data.model.MatchSlot
//import ac.sfj.miniappcompetition.data.model.MatchStatus
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertNull
//import org.junit.Assert.assertTrue
//import org.junit.Test
//import kotlin.random.Random

//class MatchResultRulesTest {

//    @Test
//    fun canUndo_matchesBackendDuringRandomPlay() {
//        for (participants in 2..16) {
//            for (seed in 0 until 5) {
//                val simulator = BracketSimulator(participants)
//                val random = Random(participants * 31 + seed)
//                var steps = 0
//                while (steps < 200) {
//                    assertRulesMatchBackend(participants, simulator)
//                    val undoable = simulator.matches().filter { simulator.undoAllowed(it.id) }
//                    val ready = simulator.matches().filter { it.status == MatchStatus.READY }
//                    if (simulator.competitionStatus == CompetitionStatus.COMPLETED && random.nextInt(3) != 0) break
//                    if (undoable.isNotEmpty() && (ready.isEmpty() || random.nextInt(4) == 0)) {
//                        simulator.undo(undoable[random.nextInt(undoable.size)].id)
//                    } else if (ready.isNotEmpty() && simulator.competitionStatus == CompetitionStatus.IN_MATCH) {
//                        val match = ready[random.nextInt(ready.size)]
//                        simulator.submit(match.id, if (random.nextBoolean()) MatchSlot.ONE else MatchSlot.TWO)
//                    } else {
//                        break
//                    }
//                    steps++
//                }
//                assertRulesMatchBackend(participants, simulator)
//            }
//        }
//    }

//    private fun assertRulesMatchBackend(participants: Int, simulator: BracketSimulator) {
//        val status = simulator.competitionStatus
//        val topology = BracketTopology(simulator.matches())
//        for (match in topology.matches) {
//            val label = "$participants peserta, status $status, babak ${match.round} match ${match.matchNumber}"
//            assertEquals(label, simulator.undoAllowed(match.id), MatchResultRules.canUndo(status, match, topology))
//            assertEquals(
//                label,
//                status == CompetitionStatus.IN_MATCH && match.status == MatchStatus.READY,
//                MatchResultRules.canSubmitResult(status, match),
//            )
//        }
//    }

//    @Test
//    fun canUndo_roundOneMatchUntilSemifinalCompleted() {
//        // Alur Referensi UI hal. 12: hasil Babak 1 Match 1 dapat diurungkan selama Semifinal Match 1 belum selesai.
//        val simulator = BracketSimulator(6)
//        val roundOneMatchOne = simulator.match(1, 1)
//        simulator.submit(roundOneMatchOne.id, MatchSlot.ONE, score1 = 3, score2 = 1)
//        assertTrue(canUndo(simulator, roundOneMatchOne.id))

//        simulator.submit(simulator.match(1, 2).id, MatchSlot.TWO, score1 = 0, score2 = 2)
//        assertTrue(canUndo(simulator, roundOneMatchOne.id))

//        simulator.submit(simulator.match(2, 1).id, MatchSlot.ONE)
//        assertFalse(canUndo(simulator, roundOneMatchOne.id))
//    }

//    @Test
//    fun canUndo_whenNextIsBye_checksMatchAfterBye() {
//        // 6 peserta: pemenang Babak 1 Match 3 melewati bye Semifinal Match 2 menuju Final.
//        val simulator = BracketSimulator(6)
//        val roundOneMatchThree = simulator.match(1, 3)
//        simulator.submit(roundOneMatchThree.id, MatchSlot.ONE)
//        assertEquals(MatchStatus.COMPLETED, simulator.match(2, 2).status)
//        assertTrue(canUndo(simulator, roundOneMatchThree.id))
//        assertFalse(canUndo(simulator, simulator.match(2, 2).id))

//        simulator.submit(simulator.match(1, 1).id, MatchSlot.ONE)
//        simulator.submit(simulator.match(1, 2).id, MatchSlot.ONE)
//        simulator.submit(simulator.match(2, 1).id, MatchSlot.ONE)
//        simulator.submit(simulator.match(3, 1).id, MatchSlot.ONE)
//        assertFalse(canUndo(simulator, roundOneMatchThree.id))
//    }

//    @Test
//    fun canUndo_roundOneByeIsNeverUndoable() {
//        val simulator = BracketSimulator(5)
//        val bye = simulator.match(1, 3)

//        assertEquals(MatchStatus.COMPLETED, bye.status)
//        assertFalse(canUndo(simulator, bye.id))
//    }

//    @Test
//    fun canUndo_completedCompetitionAllowsOnlyFinal() {
//        val simulator = BracketSimulator(8)
//        while (simulator.competitionStatus == CompetitionStatus.IN_MATCH) {
//            val match = simulator.matches().first { it.status == MatchStatus.READY }
//            simulator.submit(match.id, MatchSlot.ONE)
//        }
//        val topology = BracketTopology(simulator.matches())
//        val undoable = topology.matches.filter { MatchResultRules.canUndo(CompetitionStatus.COMPLETED, it, topology) }

//        assertEquals(listOf(topology.finalMatch?.id), undoable.map { it.id })
//    }

//    @Test
//    fun canSubmitResult_requiresInMatchAndReady() {
//        val simulator = BracketSimulator(4)
//        val ready = simulator.match(1, 1)
//        val pending = simulator.match(2, 1)

//        assertTrue(MatchResultRules.canSubmitResult(CompetitionStatus.IN_MATCH, ready))
//        assertFalse(MatchResultRules.canSubmitResult(CompetitionStatus.IN_MATCH, pending))
//        assertFalse(MatchResultRules.canSubmitResult(CompetitionStatus.COMPLETED, ready))
//        assertFalse(MatchResultRules.canSubmitResult(CompetitionStatus.OPEN, ready))
//    }

//    @Test
//    fun winnerByScore_higherScoreWinsOtherwiseManual() {
//        assertEquals(MatchSlot.ONE, MatchResultRules.winnerByScore(3, 1))
//        assertEquals(MatchSlot.TWO, MatchResultRules.winnerByScore(0, 2))
//        assertNull(MatchResultRules.winnerByScore(1, 1))
//        assertNull(MatchResultRules.winnerByScore(null, 2))
//        assertNull(MatchResultRules.winnerByScore(null, null))
//    }

//    @Test
//    fun validateResult_rejectsNegativeMissingOrContradictingWinner() {
//        assertEquals(ResultError.NEGATIVE_SCORE, MatchResultRules.validateResult(-1, 0, MatchSlot.TWO))
//        assertEquals(ResultError.WINNER_REQUIRED, MatchResultRules.validateResult(1, 1, null))
//        assertEquals(ResultError.WINNER_CONTRADICTS_SCORE, MatchResultRules.validateResult(3, 1, MatchSlot.TWO))
//        assertNull(MatchResultRules.validateResult(3, 1, MatchSlot.ONE))
//        assertNull(MatchResultRules.validateResult(1, 1, MatchSlot.TWO))
//        assertNull(MatchResultRules.validateResult(null, null, MatchSlot.ONE))
//    }

//    private fun canUndo(simulator: BracketSimulator, matchId: Long): Boolean {
//        val topology = BracketTopology(simulator.matches())
//        val match = checkNotNull(topology.findMatch(matchId))
//        return MatchResultRules.canUndo(simulator.competitionStatus, match, topology)
//    }
//}
