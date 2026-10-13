package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.dto.CompetitionRequestDto
import ac.sfj.miniappcompetition.data.dto.CompetitionResponseDto
//import ac.sfj.miniappcompetition.data.dto.MatchResponseDto
//import ac.sfj.miniappcompetition.data.dto.ParticipantRequestDto
//import ac.sfj.miniappcompetition.data.dto.ParticipantResponseDto
//import ac.sfj.miniappcompetition.data.dto.SubmitMatchResultRequestDto
//import ac.sfj.miniappcompetition.data.dto.TeamMemberDto
import ac.sfj.miniappcompetition.data.service.CompetitionApiService
//import ac.sfj.miniappcompetition.data.service.MatchApiService
//import ac.sfj.miniappcompetition.data.service.ParticipantApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Error backend dengan body `{code, message}`. */
fun apiError(status: Int, code: String): HttpException =
    HttpException(
        Response.error<Any>(
            status,
            """{"code":"$code","message":"$code"}""".toResponseBody("application/json".toMediaType()),
        ),
    )

fun competitionDto(
    id: Long,
    name: String = "Kompetisi $id",
    participantType: String = "INDIVIDUAL",
    status: String = "OPEN",
    day: Int = id.toInt(),
): CompetitionResponseDto = CompetitionResponseDto(
    id = id,
    name = name,
    participantType = participantType,
    status = status,
    createdAt = "2026-10-%02dT08:00:00Z".format(day),
    updatedAt = "2026-10-%02dT08:00:00Z".format(day),
)

//fun participantDto(
//    id: Long,
//    competitionId: Long,
//    name: String = "Peserta $id",
//    members: List<String>? = null,
//): ParticipantResponseDto = ParticipantResponseDto(
//    id = id,
//    competitionId = competitionId,
//    name = name,
//    members = members?.map { TeamMemberDto(it) },
//    createdAt = "2026-10-06T08:%02d:00Z".format(id % 60),
//    updatedAt = "2026-10-06T08:%02d:00Z".format(id % 60),
//)

//fun matchDto(
//    id: Long,
//    competitionId: Long,
//    round: Int,
//    matchNumber: Int,
//    participant1Id: Long? = null,
//    participant2Id: Long? = null,
//    winnerId: Long? = null,
//    nextMatchId: Long? = null,
//    status: String = "PENDING",
//): MatchResponseDto = MatchResponseDto(
//    id = id,
//    competitionId = competitionId,
//    participant1Id = participant1Id,
//    participant2Id = participant2Id,
//    winnerId = winnerId,
//    nextMatchId = nextMatchId,
//    round = round,
//    matchNumber = matchNumber,
//    status = status,
//    createdAt = "2026-10-06T09:00:00Z",
//    updatedAt = "2026-10-06T09:00:00Z",
//)

/** Antrean error: setiap pemanggilan berikutnya mengambil satu error bila ada. */
open class FailureQueue {
    private val failures = ArrayDeque<Throwable>()

    fun failNext(error: Throwable) {
        failures.addLast(error)
    }

    protected fun throwIfQueued() {
        failures.removeFirstOrNull()?.let { throw it }
    }
}

class FakeCompetitionApiService : FailureQueue(), CompetitionApiService {
    val competitions = mutableListOf<CompetitionResponseDto>()
    var nextId = 100L

    override suspend fun getCompetitions(): List<CompetitionResponseDto> {
        throwIfQueued()
        return competitions.sortedByDescending { it.createdAt }
    }

    override suspend fun getCompetition(competitionId: Long): CompetitionResponseDto {
        throwIfQueued()
        return competitions.firstOrNull { it.id == competitionId } ?: throw apiError(404, "COMPETITION_NOT_FOUND")
    }

    override suspend fun createCompetition(request: CompetitionRequestDto): CompetitionResponseDto {
        throwIfQueued()
        val created = competitionDto(nextId++, request.name.trim(), request.participantType, day = 28)
        competitions += created
        return created
    }

    override suspend fun updateCompetition(
        competitionId: Long,
        request: CompetitionRequestDto,
    ): CompetitionResponseDto {
        throwIfQueued()
        val index = competitions.indexOfFirst { it.id == competitionId }
        if (index < 0) throw apiError(404, "COMPETITION_NOT_FOUND")
        val updated = competitions[index].copy(name = request.name.trim(), participantType = request.participantType)
        competitions[index] = updated
        return updated
    }

    override suspend fun deleteCompetition(competitionId: Long) {
        throwIfQueued()
        if (!competitions.removeAll { it.id == competitionId }) throw apiError(404, "COMPETITION_NOT_FOUND")
    }
}

//class FakeParticipantApiService : FailureQueue(), ParticipantApiService {
//    val participants = mutableListOf<ParticipantResponseDto>()
//    val receivedRequests = mutableListOf<ParticipantRequestDto>()
//    var nextId = 500L

//    override suspend fun getParticipants(competitionId: Long): List<ParticipantResponseDto> {
//        throwIfQueued()
//        return participants.filter { it.competitionId == competitionId }
//    }

//    override suspend fun createParticipant(
//        competitionId: Long,
//        request: ParticipantRequestDto,
//    ): ParticipantResponseDto {
//        throwIfQueued()
//        receivedRequests += request
//        val created = participantDto(nextId++, competitionId, request.name, request.members?.map { it.name })
//        participants += created
//        return created
//    }

//    override suspend fun updateParticipant(
//        competitionId: Long,
//        participantId: Long,
//        request: ParticipantRequestDto,
//    ): ParticipantResponseDto {
//        throwIfQueued()
//        receivedRequests += request
//        val index = participants.indexOfFirst { it.id == participantId && it.competitionId == competitionId }
//        if (index < 0) throw apiError(404, "PARTICIPANT_NOT_FOUND")
//        val updated = participants[index].copy(name = request.name, members = request.members)
//        participants[index] = updated
//        return updated
//    }

//    override suspend fun deleteParticipant(competitionId: Long, participantId: Long) {
//        throwIfQueued()
//        if (!participants.removeAll { it.id == participantId && it.competitionId == competitionId }) {
//            throw apiError(404, "PARTICIPANT_NOT_FOUND")
//        }
//    }
//}

///**
// * Fake modul Match dengan respons yang diatur test: [matches] adalah state server, dan aksi submit
// * atau undo menerapkan [onAction] untuk meniru perubahan match lain di backend.
// */
//class FakeMatchApiService : FailureQueue(), MatchApiService {
//    var matches = mutableListOf<MatchResponseDto>()
//    var generated: List<MatchResponseDto> = emptyList()
//    var onAction: (matchId: Long) -> MatchResponseDto = { error("onAction belum diatur") }
//    val submittedRequests = mutableListOf<SubmitMatchResultRequestDto>()

//    /** Error yang selalu dilempar `getMatches`, untuk menguji pemuatan ulang yang gagal. */
//    var getMatchesError: Throwable? = null

//    override suspend fun generateBracket(competitionId: Long): List<MatchResponseDto> {
//        throwIfQueued()
//        matches = generated.toMutableList()
//        return generated
//    }

//    override suspend fun getMatches(competitionId: Long): List<MatchResponseDto> {
//        throwIfQueued()
//        getMatchesError?.let { throw it }
//        return matches.filter { it.competitionId == competitionId }
//    }

//    override suspend fun submitResult(
//        competitionId: Long,
//        matchId: Long,
//        request: SubmitMatchResultRequestDto,
//    ): MatchResponseDto {
//        throwIfQueued()
//        submittedRequests += request
//        return onAction(matchId)
//    }

//    override suspend fun undoResult(competitionId: Long, matchId: Long): MatchResponseDto {
//        throwIfQueued()
//        return onAction(matchId)
//    }

//    override suspend fun resetBracket(competitionId: Long) {
//        throwIfQueued()
//        matches.removeAll { it.competitionId == competitionId }
//    }
//}
