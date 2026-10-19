package ac.sfj.miniappcompetition.data.rule

/**
 * Nama babak untuk label tab dan teks asal slot (Referensi UI hal. 4): babak terakhir "Final",
 * satu sebelumnya "Semifinal", sisanya "Babak n". Aturan ini berlaku juga untuk jumlah peserta
 * yang bukan pangkat dua. Teks yang tampil dibentuk di lapisan UI.
 */
sealed interface RoundName {
    data object Final : RoundName

    data object Semifinal : RoundName

    /** "Babak [round]". */
    data class Numbered(val round: Int) : RoundName

    companion object {
        /** Nama babak ke-[round] (mulai 1) dari bracket dengan [roundCount] babak. */
        fun of(round: Int, roundCount: Int): RoundName {
            require(round in 1..roundCount) { "Babak $round di luar rentang 1..$roundCount" }
            return when (round) {
                roundCount -> Final
                roundCount - 1 -> Semifinal
                else -> Numbered(round)
            }
        }
    }
}
