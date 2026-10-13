//package ac.sfj.miniappcompetition.data.rule

//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertNotNull
//import org.junit.Assert.assertNull
//import org.junit.Assert.assertTrue
//import org.junit.Test

//class BracketShapeTest {

//    private data class Row(
//        val participants: Int,
//        val rounds: Int,
//        val playedMatches: Int,
//        val byes: Int,
//        val nodesPerRound: List<Int>,
//        val roundLabels: String,
//    )

//    /** Tabel "Bentuk bracket per jumlah peserta", Referensi UI hal. 4. */
//    private val referenceTable = listOf(
//        Row(2, 1, 1, 0, listOf(1), "Final"),
//        Row(3, 2, 2, 1, listOf(2, 1), "Semifinal, Final"),
//        Row(4, 2, 3, 0, listOf(2, 1), "Semifinal, Final"),
//        Row(5, 3, 4, 2, listOf(3, 2, 1), "Babak 1, Semifinal, Final"),
//        Row(6, 3, 5, 1, listOf(3, 2, 1), "Babak 1, Semifinal, Final"),
//        Row(7, 3, 6, 1, listOf(4, 2, 1), "Babak 1, Semifinal, Final"),
//        Row(8, 3, 7, 0, listOf(4, 2, 1), "Babak 1, Semifinal, Final"),
//        Row(9, 4, 8, 3, listOf(5, 3, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(10, 4, 9, 2, listOf(5, 3, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(11, 4, 10, 2, listOf(6, 3, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(12, 4, 11, 1, listOf(6, 3, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(13, 4, 12, 2, listOf(7, 4, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(14, 4, 13, 1, listOf(7, 4, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(15, 4, 14, 1, listOf(8, 4, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//        Row(16, 4, 15, 0, listOf(8, 4, 2, 1), "Babak 1, Babak 2, Semifinal, Final"),
//    )

//    @Test
//    fun shape_matchesReferenceTable() {
//        for (row in referenceTable) {
//            val label = "${row.participants} peserta"
//            val shape = BracketShape.of(row.participants)
//            assertNotNull(label, shape)
//            checkNotNull(shape)
//            assertEquals(label, row.rounds, shape.roundCount)
//            assertEquals(label, row.playedMatches, shape.playedMatchCount)
//            assertEquals(label, row.byes, shape.byeCount)
//            assertEquals(label, row.nodesPerRound, shape.nodesPerRound)
//            assertEquals(label, row.roundLabels, (1..shape.roundCount).joinToString { shape.roundName(it).label() })
//        }
//    }

//    @Test
//    fun shape_matchesBackendPlanner() {
//        for (participants in BracketShape.MIN_PARTICIPANTS..BracketShape.MAX_PARTICIPANTS) {
//            val shape = BracketShape.of(participants)!!
//            val matches = BracketSimulator(participants).let { simulator ->
//                simulator.matches().map { it to (it.id in simulator.plannedByeIds) }
//            }
//            val rounds = matches.maxOf { it.first.round }
//            val label = "$participants peserta"
//            assertEquals(label, rounds, shape.roundCount)
//            assertEquals(label, (1..rounds).map { r -> matches.count { it.first.round == r } }, shape.nodesPerRound)
//            assertEquals(
//                label,
//                (1..rounds).map { r -> matches.count { it.first.round == r && it.second } },
//                shape.byesPerRound,
//            )
//            assertEquals(label, participants - 1, shape.playedMatchCount)
//        }
//    }

//    @Test
//    fun roundsWithBye_sixParticipants_hasByeInSemifinal() {
//        val shape = BracketShape.of(6)!!

//        assertEquals(listOf(2), shape.roundsWithBye)
//        assertEquals(RoundName.Semifinal, shape.roundName(2))
//    }

//    @Test
//    fun of_outsideTwoToSixteen_returnsNull() {
//        assertNull(BracketShape.of(0))
//        assertNull(BracketShape.of(1))
//        assertNull(BracketShape.of(17))
//        assertFalse(BracketShape.isSupported(1))
//        assertTrue(BracketShape.isSupported(2))
//        assertTrue(BracketShape.isSupported(16))
//        assertFalse(BracketShape.isSupported(17))
//    }

//    @Test
//    fun roundName_followsFinalSemifinalThenNumbered() {
//        assertEquals(RoundName.Final, RoundName.of(round = 1, roundCount = 1))
//        assertEquals(RoundName.Semifinal, RoundName.of(round = 1, roundCount = 2))
//        assertEquals(RoundName.Numbered(1), RoundName.of(round = 1, roundCount = 4))
//        assertEquals(RoundName.Numbered(2), RoundName.of(round = 2, roundCount = 4))
//        assertEquals(RoundName.Semifinal, RoundName.of(round = 3, roundCount = 4))
//        assertEquals(RoundName.Final, RoundName.of(round = 4, roundCount = 4))
//    }

//    private fun RoundName.label(): String = when (this) {
//        RoundName.Final -> "Final"
//        RoundName.Semifinal -> "Semifinal"
//        is RoundName.Numbered -> "Babak $round"
//    }
//}
