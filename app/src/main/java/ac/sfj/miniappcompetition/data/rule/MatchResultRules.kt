//package ac.sfj.miniappcompetition.data.rule

//import ac.sfj.miniappcompetition.data.model.CompetitionStatus
//import ac.sfj.miniappcompetition.data.model.Match
//import ac.sfj.miniappcompetition.data.model.MatchSlot
//import ac.sfj.miniappcompetition.data.model.MatchStatus

///**
// * Aturan input dan urungkan hasil match (Referensi UI hal. 3, 11, dan 12), diturunkan dari
// * `MatchService` backend.
// */
//object MatchResultRules {
//    /** Input hasil: kompetisi Berlangsung dan match berstatus Siap. */
//    fun canSubmitResult(competitionStatus: CompetitionStatus, match: Match): Boolean =
//        competitionStatus == CompetitionStatus.IN_MATCH && match.status == MatchStatus.READY

//    /**
//     * Urungkan hasil: match selesai, bukan bye, dan match berikutnya belum selesai. Bila match
//     * berikutnya adalah bye, yang diperiksa adalah match sesudah bye tersebut, sama dengan
//     * `MatchService.clearProgression`. Pada kompetisi Selesai, aturan ini hanya meloloskan final.
//     */
//    fun canUndo(competitionStatus: CompetitionStatus, match: Match, topology: BracketTopology): Boolean {
//        if (competitionStatus == CompetitionStatus.OPEN) return false
//        if (match.status != MatchStatus.COMPLETED || topology.isBye(match)) return false
//        val next = topology.nextMatch(match) ?: return true
//        val blocking = if (topology.isBye(next)) topology.nextMatch(next) else next
//        return blocking?.status != MatchStatus.COMPLETED
//    }

//    /**
//     * Slot pemenang menurut skor. `null` bila skor seri atau belum lengkap; pemenang lalu wajib
//     * dipilih manual (Referensi UI hal. 11).
//     */
//    fun winnerByScore(score1: Int?, score2: Int?): MatchSlot? = when {
//        score1 == null || score2 == null || score1 == score2 -> null
//        score1 > score2 -> MatchSlot.ONE
//        else -> MatchSlot.TWO
//    }

//    /**
//     * Validasi isian hasil sebelum dikirim; `null` bila valid. Backend tidak memeriksa kecocokan
//     * skor dan pemenang, sehingga pemeriksaan itu dilakukan di sini.
//     */
//    fun validateResult(score1: Int?, score2: Int?, winner: MatchSlot?): ResultError? {
//        val byScore = winnerByScore(score1, score2)
//        return when {
//            (score1 != null && score1 < 0) || (score2 != null && score2 < 0) -> ResultError.NEGATIVE_SCORE
//            winner == null -> ResultError.WINNER_REQUIRED
//            byScore != null && byScore != winner -> ResultError.WINNER_CONTRADICTS_SCORE
//            else -> null
//        }
//    }
//}

///** Kesalahan isian hasil match. */
//enum class ResultError {
//    /** Skor di bawah 0. */
//    NEGATIVE_SCORE,

//    /** Pemenang belum dipilih. */
//    WINNER_REQUIRED,

//    /** Pemenang bukan pemilik skor tertinggi. */
//    WINNER_CONTRADICTS_SCORE,
//}
