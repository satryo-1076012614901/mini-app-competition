//package ac.sfj.miniappcompetition.data.dto

//import ac.sfj.miniappcompetition.data.model.Match
//import ac.sfj.miniappcompetition.data.model.MatchStatus
//import kotlinx.serialization.Serializable

///** `MatchResponse`; hanya berisi ID peserta, nama dipetakan dari data peserta. */
//@Serializable
//data class MatchResponseDto(
//    val id: Long,
//    val competitionId: Long,
//    val participant1Id: Long? = null,
//    val participant2Id: Long? = null,
//    val winnerId: Long? = null,
//    val nextMatchId: Long? = null,
//    val round: Int,
//    val matchNumber: Int,
//    val score1: Int? = null,
//    val score2: Int? = null,
//    val status: String,
//    val createdAt: String,
//    val updatedAt: String,
//)

///**
// * Body `POST …/matches/{matchId}/result`. [score1] milik `participant1Id` dan [score2] milik
// * `participant2Id`; skor `null` tidak ikut dikirim.
// */
//@Serializable
//data class SubmitMatchResultRequestDto(
//    val winnerId: Long,
//    val score1: Int? = null,
//    val score2: Int? = null,
//)

///** Melempar [IllegalArgumentException] bila status tidak sesuai kontrak. */
//fun MatchResponseDto.toModel(): Match = Match(
//    id = id,
//    competitionId = competitionId,
//    round = round,
//    matchNumber = matchNumber,
//    participant1Id = participant1Id,
//    participant2Id = participant2Id,
//    winnerId = winnerId,
//    nextMatchId = nextMatchId,
//    score1 = score1,
//    score2 = score2,
//    status = MatchStatus.valueOf(status),
//)
