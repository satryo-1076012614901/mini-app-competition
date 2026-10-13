package ac.sfj.miniappcompetition.data.service

import ac.sfj.miniappcompetition.data.dto.CompetitionRequestDto
import ac.sfj.miniappcompetition.data.dto.CompetitionResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoint modul Competition. Path ditulis relatif (tanpa "/" di depan) agar prefix environment
 * pada base URL (`/develop/` atau `/production/`) tetap dipakai.
 */
interface CompetitionApiService {
    /** Daftar kompetisi, terbaru lebih dulu. */
    @GET("api/competitions")
    suspend fun getCompetitions(): List<CompetitionResponseDto>

    @GET("api/competitions/{competitionId}")
    suspend fun getCompetition(@Path("competitionId") competitionId: Long): CompetitionResponseDto

    /** HTTP 201; kompetisi baru selalu berstatus OPEN. */
    @POST("api/competitions")
    suspend fun createCompetition(@Body request: CompetitionRequestDto): CompetitionResponseDto

    @PUT("api/competitions/{competitionId}")
    suspend fun updateCompetition(
        @Path("competitionId") competitionId: Long,
        @Body request: CompetitionRequestDto,
    ): CompetitionResponseDto

    /** HTTP 204; peserta ikut terhapus. */
    @DELETE("api/competitions/{competitionId}")
    suspend fun deleteCompetition(@Path("competitionId") competitionId: Long)
}
