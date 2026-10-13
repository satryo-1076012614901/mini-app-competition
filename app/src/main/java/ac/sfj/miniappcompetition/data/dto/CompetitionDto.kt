package ac.sfj.miniappcompetition.data.dto

import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import kotlinx.serialization.Serializable

/** Body `POST /api/competitions` dan `PUT /api/competitions/{competitionId}`. */
@Serializable
data class CompetitionRequestDto(
    val name: String,
    /** Nama [ParticipantType]: `INDIVIDUAL` atau `TEAM`. */
    val participantType: String,
)

/** `CompetitionResponse`; enum dan timestamp dibaca sebagai teks lalu dipetakan oleh [toModel]. */
@Serializable
data class CompetitionResponseDto(
    val id: Long,
    val name: String,
    val participantType: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)

/** Melempar [IllegalArgumentException] bila enum atau timestamp tidak sesuai kontrak. */
fun CompetitionResponseDto.toModel(): Competition = Competition(
    id = id,
    name = name,
    participantType = ParticipantType.valueOf(participantType),
    status = CompetitionStatus.valueOf(status),
    createdAt = parseInstant(createdAt),
)
