package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.dto.CompetitionRequestDto
import ac.sfj.miniappcompetition.data.dto.toModel
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.model.apiCode
import ac.sfj.miniappcompetition.data.model.onFailure
import ac.sfj.miniappcompetition.data.model.onSuccess
import ac.sfj.miniappcompetition.data.network.safeApiCall
import ac.sfj.miniappcompetition.data.service.CompetitionApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Sumber data kompetisi. Cache di repository menjadi satu sumber kebenaran: daftar kompetisi dan
 * detail kompetisi membaca data yang sama, sehingga status yang dimuat ulang di layar detail ikut
 * terlihat di daftar.
 */
interface CompetitionRepository {
    /** Daftar kompetisi, terbaru lebih dulu; `null` sebelum daftar pernah dimuat. */
    val competitions: Flow<List<Competition>?>

    /** Satu kompetisi dari cache; `null` bila belum dimuat atau sudah dihapus. */
    fun observeCompetition(competitionId: Long): Flow<Competition?>

    /** Memuat ulang daftar kompetisi (`GET /api/competitions`). */
    suspend fun refreshCompetitions(): AppResult<List<Competition>>

    /** Memuat ulang satu kompetisi, misalnya setelah aksi match mengubah statusnya. */
    suspend fun refreshCompetition(competitionId: Long): AppResult<Competition>

    /** [name] dikirim apa adanya; normalisasi dan validasi ada di `CompetitionRules`. */
    suspend fun createCompetition(name: String, participantType: ParticipantType): AppResult<Competition>

    suspend fun updateCompetition(
        competitionId: Long,
        name: String,
        participantType: ParticipantType,
    ): AppResult<Competition>

    suspend fun deleteCompetition(competitionId: Long): AppResult<Unit>
}

/** Implementasi [CompetitionRepository] yang memanggil API melalui [CompetitionApiService]. */
class NetworkCompetitionRepository(
    private val competitionApiService: CompetitionApiService,
) : CompetitionRepository {

    private val cache = MutableStateFlow(CompetitionCache())

    override val competitions: Flow<List<Competition>?> =
        cache.map { it.sortedList() }.distinctUntilChanged()

    override fun observeCompetition(competitionId: Long): Flow<Competition?> =
        cache.map { it.byId[competitionId] }.distinctUntilChanged()

    override suspend fun refreshCompetitions(): AppResult<List<Competition>> =
        safeApiCall { competitionApiService.getCompetitions().map { it.toModel() } }
            .onSuccess { list ->
                cache.value = CompetitionCache(byId = list.associateBy { it.id }, listLoaded = true)
            }

    override suspend fun refreshCompetition(competitionId: Long): AppResult<Competition> =
        safeApiCall { competitionApiService.getCompetition(competitionId).toModel() }
            .onSuccess(::put)
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun createCompetition(
        name: String,
        participantType: ParticipantType,
    ): AppResult<Competition> =
        safeApiCall {
            competitionApiService.createCompetition(CompetitionRequestDto(name, participantType.name)).toModel()
        }.onSuccess(::put)

    override suspend fun updateCompetition(
        competitionId: Long,
        name: String,
        participantType: ParticipantType,
    ): AppResult<Competition> =
        safeApiCall {
            competitionApiService
                .updateCompetition(competitionId, CompetitionRequestDto(name, participantType.name))
                .toModel()
        }
            .onSuccess(::put)
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun deleteCompetition(competitionId: Long): AppResult<Unit> =
        safeApiCall { competitionApiService.deleteCompetition(competitionId) }
            .onSuccess { remove(competitionId) }
            .onFailure { removeIfNotFound(competitionId, it) }

    private fun put(competition: Competition) {
        cache.update { it.copy(byId = it.byId + (competition.id to competition)) }
    }

    private fun remove(competitionId: Long) {
        cache.update { it.copy(byId = it.byId - competitionId) }
    }

    /** Kompetisi yang sudah dihapus dari perangkat lain dikeluarkan dari cache. */
    private fun removeIfNotFound(competitionId: Long, error: AppError) {
        if (error.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND) remove(competitionId)
    }
}

/** Isi cache: kompetisi per ID dan penanda apakah daftar lengkap sudah pernah dimuat. */
private data class CompetitionCache(
    val byId: Map<Long, Competition> = emptyMap(),
    val listLoaded: Boolean = false,
) {
    fun sortedList(): List<Competition>? =
        if (listLoaded) byId.values.sortedWith(NewestFirst) else null
}

/** Urutan yang sama dengan backend: `createdAt` menurun, lalu `id` menurun. */
private val NewestFirst: Comparator<Competition> =
    compareByDescending<Competition> { it.createdAt }.thenByDescending { it.id }
