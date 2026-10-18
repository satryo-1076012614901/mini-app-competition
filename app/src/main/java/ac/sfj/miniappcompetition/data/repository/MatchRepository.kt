package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.dto.SubmitMatchResultRequestDto
import ac.sfj.miniappcompetition.data.dto.toModel
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Match
import ac.sfj.miniappcompetition.data.model.apiCode
import ac.sfj.miniappcompetition.data.model.onFailure
import ac.sfj.miniappcompetition.data.model.onSuccess
import ac.sfj.miniappcompetition.data.network.safeApiCall
import ac.sfj.miniappcompetition.data.service.MatchApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Sumber data bracket per kompetisi, dengan cache sebagai satu sumber kebenaran.
 *
 * Setiap aksi match dapat mengubah status kompetisi (Referensi UI hal. 2). Repository ini menjaga
 * daftar match tetap mutakhir; pemanggil (ViewModel) bertugas memuat ulang detail kompetisi melalui
 * `CompetitionRepository.refreshCompetition` setelah aksi berhasil.
 */
interface MatchRepository {
    /**
     * Match satu kompetisi urut babak lalu nomor match. Daftar kosong berarti bracket belum dibuat;
     * `null` berarti daftar belum pernah dimuat.
     */
    fun observeMatches(competitionId: Long): Flow<List<Match>?>

    suspend fun refreshMatches(competitionId: Long): AppResult<List<Match>>

    /** Membuat bracket; status kompetisi berubah menjadi IN_MATCH. */
    suspend fun generateBracket(competitionId: Long): AppResult<List<Match>>

    /**
     * Menyimpan hasil match. Backend ikut memajukan pemenang ke match berikutnya, termasuk melewati
     * bye, sehingga pemanggil wajib memuat ulang daftar match setelah berhasil. Hasil final mengubah status kompetisi
     * menjadi COMPLETED. [score1] milik `participant1Id` dan [score2] milik `participant2Id`.
     */
    suspend fun submitResult(
        competitionId: Long,
        matchId: Long,
        winnerId: Long,
        score1: Int?,
        score2: Int?,
    ): AppResult<Match>

    /**
     * Mengurungkan hasil match; slot di match berikutnya ikut dikosongkan sehingga daftar match dimuat
     * ulang setelah berhasil. Mengurungkan final mengembalikan status kompetisi ke IN_MATCH.
     */
    suspend fun undoResult(competitionId: Long, matchId: Long): AppResult<Match>

    /** Menghapus seluruh match; status kompetisi kembali OPEN. */
    suspend fun resetBracket(competitionId: Long): AppResult<Unit>
}

/** Implementasi [MatchRepository] yang memanggil API melalui [MatchApiService]. */
class NetworkMatchRepository(
    private val matchApiService: MatchApiService,
) : MatchRepository {

    /** Daftar match per ID kompetisi; kunci yang belum ada berarti daftar belum dimuat. */
    private val cache = MutableStateFlow<Map<Long, List<Match>>>(emptyMap())

    override fun observeMatches(competitionId: Long): Flow<List<Match>?> =
        cache.map { it[competitionId] }.distinctUntilChanged()

    override suspend fun refreshMatches(competitionId: Long): AppResult<List<Match>> =
        safeApiCall { matchApiService.getMatches(competitionId).map { it.toModel() } }
            .onSuccess { setMatches(competitionId, it) }
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun generateBracket(competitionId: Long): AppResult<List<Match>> =
        safeApiCall { matchApiService.generateBracket(competitionId).map { it.toModel() } }
            .onSuccess { setMatches(competitionId, it) }
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun submitResult(
        competitionId: Long,
        matchId: Long,
        winnerId: Long,
        score1: Int?,
        score2: Int?,
    ): AppResult<Match> =
        safeApiCall {
            matchApiService
                .submitResult(competitionId, matchId, SubmitMatchResultRequestDto(winnerId, score1, score2))
                .toModel()
        }
            .onSuccess { afterMatchChanged(competitionId, it) }
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun undoResult(competitionId: Long, matchId: Long): AppResult<Match> =
        safeApiCall { matchApiService.undoResult(competitionId, matchId).toModel() }
            .onSuccess { afterMatchChanged(competitionId, it) }
            .onFailure { removeIfNotFound(competitionId, it) }

    override suspend fun resetBracket(competitionId: Long): AppResult<Unit> =
        safeApiCall { matchApiService.resetBracket(competitionId) }
            .onSuccess { setMatches(competitionId, emptyList()) }
            .onFailure { removeIfNotFound(competitionId, it) }

    /**
     * Menyimpan respons mutasi ke cache. ViewModel memuat ulang seluruh bracket
     * dan status kompetisi, serta melaporkan kegagalan sinkronisasi secara terpisah.
     */
    private fun afterMatchChanged(competitionId: Long, changed: Match) {
        cache.update { current ->
            val list = current[competitionId] ?: return@update current
            current + (competitionId to list.map { if (it.id == changed.id) changed else it })
        }
    }

    private fun setMatches(competitionId: Long, matches: List<Match>) {
        val sorted = matches.sortedWith(compareBy({ it.round }, { it.matchNumber }))
        cache.update { it + (competitionId to sorted) }
    }

    /** Kompetisi yang sudah dihapus dari perangkat lain dikeluarkan dari cache. */
    private fun removeIfNotFound(competitionId: Long, error: AppError) {
        if (error.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND) cache.update { it - competitionId }
    }
}
