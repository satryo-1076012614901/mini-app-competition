//package ac.sfj.miniappcompetition.data.repository

//import ac.sfj.miniappcompetition.data.dto.TeamMemberDto
//import ac.sfj.miniappcompetition.data.model.ApiErrorCode
//import ac.sfj.miniappcompetition.data.model.AppError
//import ac.sfj.miniappcompetition.data.model.AppResult
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNull
//import org.junit.Test

//class NetworkParticipantRepositoryTest {

//    private val service = FakeParticipantApiService().apply {
//        participants += participantDto(1, competitionId = 10, name = "Tim Garuda", members = listOf("Fajar"))
//        participants += participantDto(2, competitionId = 10, name = "Tim Rajawali", members = emptyList())
//        participants += participantDto(3, competitionId = 20, name = "Andi Pratama")
//    }
//    private val repository = NetworkParticipantRepository(service)

//    private suspend fun names(competitionId: Long): List<String>? =
//        repository.observeParticipants(competitionId).first()?.map { it.name }

//    @Test
//    fun observeParticipants_nullUntilLoadedThenRegistrationOrder() = runTest {
//        assertNull(names(10))

//        repository.refreshParticipants(10)

//        assertEquals(listOf("Tim Garuda", "Tim Rajawali"), names(10))
//        assertEquals(listOf("Fajar"), repository.observeParticipants(10).first()?.first()?.members)
//        assertNull(names(20))
//    }

//    @Test
//    fun create_appendsOnlyToLoadedList() = runTest {
//        repository.createParticipant(20, "Bima Santoso", members = null)
//        assertNull(names(20))

//        repository.refreshParticipants(10)
//        repository.createParticipant(10, "Tim Elang", members = listOf("Gilang", "Hadi"))

//        assertEquals(listOf("Tim Garuda", "Tim Rajawali", "Tim Elang"), names(10))
//        assertNull(service.receivedRequests[0].members)
//        assertEquals(listOf(TeamMemberDto("Gilang"), TeamMemberDto("Hadi")), service.receivedRequests[1].members)
//    }

//    @Test
//    fun updateAndDelete_keepListInSync() = runTest {
//        repository.refreshParticipants(10)

//        repository.updateParticipant(10, 2, "Tim Rajawali Muda", members = emptyList())
//        assertEquals(listOf("Tim Garuda", "Tim Rajawali Muda"), names(10))

//        assertEquals(AppResult.Success(Unit), repository.deleteParticipant(10, 1))
//        assertEquals(listOf("Tim Rajawali Muda"), names(10))
//    }

//    @Test
//    fun notFound_removesStaleData() = runTest {
//        repository.refreshParticipants(10)
//        service.participants.removeAll { it.id == 1L }

//        val result = repository.deleteParticipant(10, 1)

//        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.PARTICIPANT_NOT_FOUND, 404)), result)
//        assertEquals(listOf("Tim Rajawali"), names(10))

//        service.failNext(apiError(404, "COMPETITION_NOT_FOUND"))
//        repository.refreshParticipants(10)
//        assertNull(names(10))
//    }

//    @Test
//    fun duplicateName_isReturnedAsApiErrorWithoutChangingCache() = runTest {
//        repository.refreshParticipants(10)
//        service.failNext(apiError(409, "DUPLICATE_PARTICIPANT_NAME"))

//        val result = repository.createParticipant(10, "Tim Garuda", members = emptyList())

//        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.DUPLICATE_PARTICIPANT_NAME, 409)), result)
//        assertEquals(listOf("Tim Garuda", "Tim Rajawali"), names(10))
//    }
//}
