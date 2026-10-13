//package ac.sfj.miniappcompetition.data.rule

///**
// * Bentuk bracket untuk sejumlah peserta, dihitung dengan logika yang sama dengan `BracketPlanner`
// * backend (Referensi UI hal. 4): pada setiap babak peserta dipasangkan berurutan, dan bila jumlahnya
// * ganjil satu peserta mendapat bye. Dipakai untuk ringkasan di tab Peserta dan dialog Buat bracket.
// *
// * Bila logika `BracketPlanner` berubah, perhitungan ini dan tabel di `BracketShapeTest` perlu
// * disesuaikan.
// */
//data class BracketShape(
//    val participantCount: Int,
//    /** Jumlah node per babak (match yang dimainkan ditambah bye), mulai babak 1. */
//    val nodesPerRound: List<Int>,
//    /** Jumlah bye per babak (0 atau 1), mulai babak 1. */
//    val byesPerRound: List<Int>,
//) {
//    val roundCount: Int
//        get() = nodesPerRound.size

//    val byeCount: Int
//        get() = byesPerRound.sum()

//    /** Pertandingan yang benar-benar dimainkan; selalu sama dengan jumlah peserta dikurangi satu. */
//    val playedMatchCount: Int
//        get() = nodesPerRound.sum() - byeCount

//    /** Babak (mulai 1) yang memiliki bye, misalnya untuk teks "1 bye di Semifinal". */
//    val roundsWithBye: List<Int>
//        get() = byesPerRound.indices.filter { byesPerRound[it] > 0 }.map { it + 1 }

//    /** Nama babak ke-[round] pada bracket ini. */
//    fun roundName(round: Int): RoundName = RoundName.of(round, roundCount)

//    companion object {
//        /** Batas jumlah peserta untuk membuat bracket (`BracketPlanner.MIN_PARTICIPANTS`). */
//        const val MIN_PARTICIPANTS = 2

//        /** Batas jumlah peserta untuk membuat bracket (`BracketPlanner.MAX_PARTICIPANTS`). */
//        const val MAX_PARTICIPANTS = 16

//        fun isSupported(participantCount: Int): Boolean =
//            participantCount in MIN_PARTICIPANTS..MAX_PARTICIPANTS

//        /** Bentuk bracket, atau `null` bila jumlah peserta di luar 2–16. */
//        fun of(participantCount: Int): BracketShape? {
//            if (!isSupported(participantCount)) return null
//            val nodesPerRound = mutableListOf<Int>()
//            val byesPerRound = mutableListOf<Int>()
//            var entrants = participantCount
//            while (entrants > 1) {
//                val nodes = (entrants + 1) / 2
//                nodesPerRound += nodes
//                byesPerRound += entrants % 2
//                entrants = nodes
//            }
//            return BracketShape(participantCount, nodesPerRound, byesPerRound)
//        }
//    }
//}
