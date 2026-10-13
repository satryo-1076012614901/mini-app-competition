//package ac.sfj.miniappcompetition.data.service

//import ac.sfj.miniappcompetition.data.dto.ParticipantRequestDto
//import ac.sfj.miniappcompetition.data.dto.ParticipantResponseDto
//import retrofit2.http.Body
//import retrofit2.http.DELETE
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.PUT
//import retrofit2.http.Path

///** Endpoint modul Participant. Path relatif terhadap base URL environment. */
//interface ParticipantApiService {
//    /** Peserta urut pendaftaran (`createdAt` naik). */
//    @GET("api/competitions/{competitionId}/participants")
//    suspend fun getParticipants(@Path("competitionId") competitionId: Long): List<ParticipantResponseDto>

//    /** HTTP 201. */
//    @POST("api/competitions/{competitionId}/participants")
//    suspend fun createParticipant(
//        @Path("competitionId") competitionId: Long,
//        @Body request: ParticipantRequestDto,
//    ): ParticipantResponseDto

//    @PUT("api/competitions/{competitionId}/participants/{participantId}")
//    suspend fun updateParticipant(
//        @Path("competitionId") competitionId: Long,
//        @Path("participantId") participantId: Long,
//        @Body request: ParticipantRequestDto,
//    ): ParticipantResponseDto

//    /** HTTP 204. */
//    @DELETE("api/competitions/{competitionId}/participants/{participantId}")
//    suspend fun deleteParticipant(
//        @Path("competitionId") competitionId: Long,
//        @Path("participantId") participantId: Long,
//    )
//}
