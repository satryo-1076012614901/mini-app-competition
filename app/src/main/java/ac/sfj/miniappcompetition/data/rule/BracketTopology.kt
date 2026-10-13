//package ac.sfj.miniappcompetition.data.rule

//import ac.sfj.miniappcompetition.data.model.CompetitionStatus
//import ac.sfj.miniappcompetition.data.model.Match
//import ac.sfj.miniappcompetition.data.model.MatchSlot
//import ac.sfj.miniappcompetition.data.model.MatchStatus

///**
// * Struktur bracket yang diturunkan dari daftar match satu kompetisi (`GET …/matches`).
// *
// * `MatchResponse` hanya berisi `nextMatchId`, sehingga asal slot dan bye dihitung dari topology
// * dengan logika yang sama dengan `MatchService` backend (Referensi UI hal. 11–14): match sumber
// * sebuah target diurutkan menurut `matchNumber`, sumber pertama mengisi slot 1 dan sumber kedua
// * mengisi slot 2.
// */
//class BracketTopology(matches: List<Match>) {
//    /** Seluruh match, urut babak lalu nomor match. */
//    val matches: List<Match> = matches.sortedWith(compareBy({ it.round }, { it.matchNumber }))

//    private val matchesById: Map<Long, Match> = this.matches.associateBy { it.id }

//    private val sourcesByTarget: Map<Long, List<Match>> = this.matches
//        .mapNotNull { match -> match.nextMatchId?.let { targetId -> targetId to match } }
//        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
//        .mapValues { (_, sources) -> sources.sortedBy { it.matchNumber } }

//    /** Jumlah babak; 0 bila bracket belum dibuat. */
//    val roundCount: Int = this.matches.maxOfOrNull { it.round } ?: 0

//    /** Match final (tanpa `nextMatchId`), atau `null` bila bracket belum dibuat. */
//    val finalMatch: Match? = this.matches.lastOrNull { it.nextMatchId == null }

//    /** ID juara: pemenang final yang sudah selesai (Referensi UI hal. 14). */
//    val championId: Long?
//        get() = finalMatch?.takeIf { it.status == MatchStatus.COMPLETED }?.winnerId

//    /** Nama babak ke-[round] pada bracket ini. */
//    fun roundName(round: Int): RoundName = RoundName.of(round, roundCount)

//    /** Match pada babak ke-[round], urut nomor match. */
//    fun matchesInRound(round: Int): List<Match> = matches.filter { it.round == round }

//    fun findMatch(matchId: Long): Match? = matchesById[matchId]

//    /** Match tujuan pemenang [match], atau `null` untuk final. */
//    fun nextMatch(match: Match): Match? = match.nextMatchId?.let { matchesById[it] }

//    /** Match yang pemenangnya diteruskan ke [match], urut nomor match. */
//    fun sourcesOf(match: Match): List<Match> = sourcesByTarget[match.id].orEmpty()

//    /**
//     * Node bye: di babak 1 slot 2 kosong, di babak berikutnya hanya memiliki satu match sumber.
//     * Bye tidak dimainkan, pemenangnya diteruskan otomatis, dan hasilnya tidak dapat diurungkan.
//     */
//    fun isBye(match: Match): Boolean =
//        if (match.round == 1) match.participant2Id == null else sourcesOf(match).size == 1

//    /**
//     * Match sumber yang pemenangnya mengisi [slot], untuk teks "Pemenang Babak 1 Match 1" pada slot
//     * yang masih kosong. `null` untuk babak 1 dan untuk slot 2 pada bye ("Lolos otomatis").
//     */
//    fun slotSource(match: Match, slot: MatchSlot): Match? = sourcesOf(match).getOrNull(slot.ordinal)

//    /** Progres babak untuk penghitung tab; bye tidak dihitung (Referensi UI hal. 11). */
//    fun roundProgress(round: Int): RoundProgress {
//        val played = matchesInRound(round).filterNot(::isBye)
//        return RoundProgress(
//            completed = played.count { it.status == MatchStatus.COMPLETED },
//            total = played.size,
//        )
//    }

//    /**
//     * Babak yang dibuka pertama kali: Final bila kompetisi Selesai, selain itu babak pertama yang
//     * masih memiliki match Siap (Referensi UI hal. 11 dan 14). `null` bila bracket belum dibuat.
//     */
//    fun initialRound(competitionStatus: CompetitionStatus): Int? {
//        if (roundCount == 0) return null
//        if (competitionStatus == CompetitionStatus.COMPLETED) return roundCount
//        return matches.firstOrNull { it.status == MatchStatus.READY }?.round ?: 1
//    }
//}

///** Jumlah match yang dimainkan pada satu babak dan berapa yang sudah selesai. */
//data class RoundProgress(val completed: Int, val total: Int)
