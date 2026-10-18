package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.dto.ParticipantRequestDto
import ac.sfj.miniappcompetition.data.dto.TeamMemberDto
import ac.sfj.miniappcompetition.data.dto.toModel
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.data.model.apiCode
import ac.sfj.miniappcompetition.data.model.onFailure
import ac.sfj.miniappcompetition.data.model.onSuccess
import ac.sfj.miniappcompetition.data.network.safeApiCall
import ac.sfj.miniappcompetition.data.service.ParticipantApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Sumber data peserta per kompetisi, dengan cache sebagai satu sumber kebenaran. */
interface ParticipantRepository {
    /** Peserta satu kompetisi urut pendaftaran; `null` sebelum daftarnya pernah dimuat. */
    fun observeParticipants(competitionId: Long): Flow<List<Participant>?>

    suspend fun refreshParticipants(competitionId: Long): AppResult<List<Participant>>

    /**
     * [name] dikirim apa adanya. [members] wajib `null` untuk kompetisi Individu; gunakan
     * `ParticipantRules.membersForRequest` untuk menyiapkannya.
     */
    suspend fun createParticipant(
        competitionId: Long,
        name: String,
        members: List<String>?,
    ): AppResult<Participant>

    suspend fun updateParticipant(
        competitionId: Long,
        participantId: Long,
        name: String,
        members: List<String>?,
    ): AppResult<Participant>

    suspend fun deleteParticipant(competitionId: Long, participantId: Long): AppResult<Unit>
}

/** Implementasi [ParticipantRepository] yang memanggil API melalui [ParticipantApiService]. */
class NetworkParticipantRepository(
    private val participantApiService: ParticipantApiService,
) : ParticipantRepository {

    /** Daftar peserta per ID kompetisi; kunci yang belum ada berarti daftar belum dimuat. */
    private val cache = MutableStateFlow<Map<Long, List<Participant>>>(emptyMap())

    override fun observeParticipants(competitionId: Long): Flow<List<Participant>?> =
        cache.map { it[competitionId] }.distinctUntilChanged()

    override suspend fun refreshParticipants(competitionId: Long): AppResult<List<Participant>> =
        safeApiCall { participantApiService.getParticipants(competitionId).map { it.toModel() } }
            .onSuccess { participants -> cache.update { it + (competitionId to participants) } }
            .onFailure { removeIfNotFound(competitionId, participantId = null, error = it) }

    override suspend fun createParticipant(
        competitionId: Long,
        name: String,
        members: List<String>?,
    ): AppResult<Participant> =
        safeApiCall {
            participantApiService.createParticipant(competitionId, participantRequest(name, members)).toModel()
        }
            .onSuccess { created -> updateLoadedList(competitionId) { it + created } }
            .onFailure { removeIfNotFound(competitionId, participantId = null, error = it) }

    override suspend fun updateParticipant(
        competitionId: Long,
        participantId: Long,
        name: String,
        members: List<String>?,
    ): AppResult<Participant> =
        safeApiCall {
            participantApiService
                .updateParticipant(competitionId, participantId, participantRequest(name, members))
                .toModel()
        }
            .onSuccess { updated ->
                updateLoadedList(competitionId) { list -> list.map { if (it.id == updated.id) updated else it } }
            }
            .onFailure { removeIfNotFound(competitionId, participantId, it) }

    override suspend fun deleteParticipant(competitionId: Long, participantId: Long): AppResult<Unit> =
        safeApiCall { participantApiService.deleteParticipant(competitionId, participantId) }
            .onSuccess { updateLoadedList(competitionId) { list -> list.filterNot { it.id == participantId } } }
            .onFailure { removeIfNotFound(competitionId, participantId, it) }

    /** Mengubah daftar yang sudah dimuat; daftar yang belum dimuat dibiarkan agar tidak tampil sebagian. */
    private fun updateLoadedList(competitionId: Long, transform: (List<Participant>) -> List<Participant>) {
        cache.update { current ->
            val list = current[competitionId] ?: return@update current
            current + (competitionId to transform(list))
        }
    }

    /** Data yang sudah dihapus dari perangkat lain dikeluarkan dari cache. */
    private fun removeIfNotFound(competitionId: Long, participantId: Long?, error: AppError) {
        when (error.apiCode) {
            ApiErrorCode.COMPETITION_NOT_FOUND -> cache.update { it - competitionId }
            ApiErrorCode.PARTICIPANT_NOT_FOUND -> if (participantId != null) {
                updateLoadedList(competitionId) { list -> list.filterNot { it.id == participantId } }
            }
            else -> Unit
        }
    }
}

private fun participantRequest(name: String, members: List<String>?): ParticipantRequestDto =
    ParticipantRequestDto(name = name, members = members?.map { TeamMemberDto(name = it) })
