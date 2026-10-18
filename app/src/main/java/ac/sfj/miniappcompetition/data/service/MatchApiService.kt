package ac.sfj.miniappcompetition.data.service

import ac.sfj.miniappcompetition.data.dto.MatchResponseDto
import ac.sfj.miniappcompetition.data.dto.SubmitMatchResultRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoint modul Match. Path relatif terhadap base URL environment. */
interface MatchApiService {
    /** Membuat bracket (HTTP 201); status kompetisi menjadi IN_MATCH. */
    @POST("api/competitions/{competitionId}/matches")
    suspend fun generateBracket(@Path("competitionId") competitionId: Long): List<MatchResponseDto>

    /** Match urut babak lalu nomor match. */
    @GET("api/competitions/{competitionId}/matches")
    suspend fun getMatches(@Path("competitionId") competitionId: Long): List<MatchResponseDto>

    @POST("api/competitions/{competitionId}/matches/{matchId}/result")
    suspend fun submitResult(
        @Path("competitionId") competitionId: Long,
        @Path("matchId") matchId: Long,
        @Body request: SubmitMatchResultRequestDto,
    ): MatchResponseDto

    @POST("api/competitions/{competitionId}/matches/{matchId}/undo")
    suspend fun undoResult(
        @Path("competitionId") competitionId: Long,
        @Path("matchId") matchId: Long,
    ): MatchResponseDto

    /** Reset bracket (HTTP 204): seluruh match dihapus dan kompetisi kembali OPEN. */
    @DELETE("api/competitions/{competitionId}/matches")
    suspend fun resetBracket(@Path("competitionId") competitionId: Long)
}
