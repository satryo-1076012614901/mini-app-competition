package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkCompetitionRepositoryTest {

    private val service = FakeCompetitionApiService().apply {
        competitions += competitionDto(1, day = 1)
        competitions += competitionDto(2, day = 2)
        competitions += competitionDto(3, day = 3)
    }
    private val repository = NetworkCompetitionRepository(service)

    private suspend fun listedIds(): List<Long>? = repository.competitions.first()?.map { it.id }

    @Test
    fun competitions_nullUntilLoadedThenNewestFirst() = runTest {
        assertNull(listedIds())

        val result = repository.refreshCompetitions()

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(3L, 2L, 1L), listedIds())
    }

    @Test
    fun refreshCompetition_updatesTheSameItemShownInList() = runTest {
        repository.refreshCompetitions()
        service.competitions[0] = competitionDto(1, status = "IN_MATCH", day = 1)

        repository.refreshCompetition(1)

        assertEquals(CompetitionStatus.IN_MATCH, repository.observeCompetition(1).first()?.status)
        assertEquals(CompetitionStatus.IN_MATCH, repository.competitions.first()?.single { it.id == 1L }?.status)
    }

    @Test
    fun createUpdateDelete_keepListInSync() = runTest {
        repository.refreshCompetitions()

        val created = repository.createCompetition("Lomba Catur", ParticipantType.TEAM)
        val createdId = (created as AppResult.Success).data.id
        assertEquals(listOf(createdId, 3L, 2L, 1L), listedIds())

        repository.updateCompetition(createdId, "Lomba Catur Antar Kelas", ParticipantType.TEAM)
        assertEquals("Lomba Catur Antar Kelas", repository.observeCompetition(createdId).first()?.name)

        assertEquals(AppResult.Success(Unit), repository.deleteCompetition(createdId))
        assertEquals(listOf(3L, 2L, 1L), listedIds())
        assertNull(repository.observeCompetition(createdId).first())
    }

    @Test
    fun notFound_removesCompetitionFromCache() = runTest {
        repository.refreshCompetitions()
        service.competitions.removeAll { it.id == 2L }

        val result = repository.refreshCompetition(2)

        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.COMPETITION_NOT_FOUND, 404)), result)
        assertEquals(listOf(3L, 1L), listedIds())
    }

    @Test
    fun failure_keepsCacheUnchanged() = runTest {
        repository.refreshCompetitions()
        service.failNext(IOException("offline"))
        service.failNext(apiError(409, "COMPETITION_NOT_OPEN"))

        assertEquals(AppResult.Failure(AppError.Network), repository.refreshCompetitions())
        assertEquals(
            AppResult.Failure(AppError.Api(ApiErrorCode.COMPETITION_NOT_OPEN, 409)),
            repository.deleteCompetition(3),
        )
        assertEquals(listOf(3L, 2L, 1L), listedIds())
    }
}
