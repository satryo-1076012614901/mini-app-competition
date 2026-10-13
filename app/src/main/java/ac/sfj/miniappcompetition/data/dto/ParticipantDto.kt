//package ac.sfj.miniappcompetition.data.dto

//import ac.sfj.miniappcompetition.data.model.Participant
//import kotlinx.serialization.Serializable

//@Serializable
//data class TeamMemberDto(
//    val name: String,
//)

///**
// * Body `POST` dan `PUT …/participants`. [members] wajib `null` untuk kompetisi Individu; nilai
// * `null` tidak ikut dikirim dan dibaca backend sebagai `null`.
// */
//@Serializable
//data class ParticipantRequestDto(
//    val name: String,
//    val members: List<TeamMemberDto>? = null,
//)

///** `ParticipantResponse`; [members] bernilai `null` untuk peserta Individu. */
//@Serializable
//data class ParticipantResponseDto(
//    val id: Long,
//    val competitionId: Long,
//    val name: String,
//    val members: List<TeamMemberDto>? = null,
//    val createdAt: String,
//    val updatedAt: String,
//)

///** Melempar [IllegalArgumentException] bila timestamp tidak sesuai kontrak. */
//fun ParticipantResponseDto.toModel(): Participant = Participant(
//    id = id,
//    competitionId = competitionId,
//    name = name,
//    members = members.orEmpty().map { it.name },
//    createdAt = parseInstant(createdAt),
//)
