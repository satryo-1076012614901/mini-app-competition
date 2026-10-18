package ac.sfj.miniappcompetition.data.model

/**
 * Satu node bracket (`MatchResponse` di backend). Bye juga berupa node; gunakan
 * `BracketTopology.isBye` untuk membedakannya dari pertandingan yang dimainkan.
 *
 * Nilai per slot saling berpasangan: [score1] milik [participant1Id], [score2] milik [participant2Id].
 */
data class Match(
    val id: Long,
    val competitionId: Long,
    val round: Int,
    val matchNumber: Int,
    val participant1Id: Long?,
    val participant2Id: Long?,
    val winnerId: Long?,
    val nextMatchId: Long?,
    val score1: Int?,
    val score2: Int?,
    val status: MatchStatus,
) {
    /** ID peserta pada [slot]; `null` bila slot masih menunggu pemenang match sebelumnya. */
    fun participantId(slot: MatchSlot): Long? = when (slot) {
        MatchSlot.ONE -> participant1Id
        MatchSlot.TWO -> participant2Id
    }

    /** Skor pada [slot]. */
    fun score(slot: MatchSlot): Int? = when (slot) {
        MatchSlot.ONE -> score1
        MatchSlot.TWO -> score2
    }

    /** Slot pemenang, atau `null` bila match belum memiliki pemenang. */
    val winnerSlot: MatchSlot?
        get() = when {
            winnerId == null -> null
            winnerId == participant1Id -> MatchSlot.ONE
            winnerId == participant2Id -> MatchSlot.TWO
            else -> null
        }
}

/** Status match (Referensi UI hal. 3). */
enum class MatchStatus {
    /** Menunggu pemenang dari match sebelumnya. */
    PENDING,

    /** Kedua peserta sudah terisi; hasil dapat diinput. */
    READY,

    /** Sudah memiliki pemenang, termasuk bye yang selesai otomatis. */
    COMPLETED,
}

/** Posisi peserta dalam match: slot 1 (`participant1Id`, `score1`) atau slot 2. */
enum class MatchSlot {
    ONE,
    TWO,
}
