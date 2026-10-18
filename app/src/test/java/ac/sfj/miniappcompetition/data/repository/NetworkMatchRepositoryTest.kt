package ac.sfj.miniappcompetition.data.repository

import ac.sfj.miniappcompetition.data.dto.SubmitMatchResultRequestDto
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.MatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkMatchRepositoryTest {

    private val competitionId = 7L

    /** Bracket 3 peserta: Semifinal Match 1 dimainkan, Semifinal Match 2 bye, lalu Final. */
    private val final = matchDto(
        id = 3, competitionId = competitionId, round = 2, matchNumber = 1, participant2Id = 13,
    )
    private val semifinal = matchDto(
        id = 1, competitionId = competitionId, round = 1, matchNumber = 1,
        participant1Id = 11, participant2Id = 12, nextMatchId = 3, status = "READY",
    )
    private val bye = matchDto(
        id = 2, competitionId = competitionId, round = 1, matchNumber = 2,
        participant1Id = 13, winnerId = 13, nextMatchId = 3, status = "COMPLETED",
    )
    private val completedSemifinal = semifinal.copy(winnerId = 11, score1 = 3, score2 = 1, status = "COMPLETED")

    private val service = FakeMatchApiService().apply { generated = listOf(final, semifinal, bye) }
    private val repository = NetworkMatchRepository(service)

    private suspend fun cached() = repository.observeMatches(competitionId).first()

    @Test
    fun generateBracket_cachesMatchesSortedByRoundThenNumber() = runTest {
        assertNull(cached())

        repository.generateBracket(competitionId)

        assertEquals(listOf(1L, 2L, 3L), cached()?.map { it.id })
    }

    @Test
    fun submitResult_cachesResponseAndCallerRefreshesDownstreamMatches() = runTest {
        repository.generateBracket(competitionId)
        service.onAction = {
            service.matches = mutableListOf(
                completedSemifinal,
                bye,
                final.copy(participant1Id = 11, status = "READY"),
            )
            completedSemifinal
        }

        val result = repository.submitResult(competitionId, matchId = 1, winnerId = 11, score1 = 3, score2 = 1)

        assertEquals(MatchStatus.COMPLETED, (result as AppResult.Success).data.status)
        assertEquals(SubmitMatchResultRequestDto(winnerId = 11, score1 = 3, score2 = 1), service.submittedRequests.single())
        assertEquals(MatchStatus.PENDING, cached()?.single { it.id == 3L }?.status)
        repository.refreshMatches(competitionId)
        val cachedFinal = cached()?.single { it.id == 3L }
        assertEquals(MatchStatus.READY, cachedFinal?.status)
        assertEquals(11L, cachedFinal?.participant1Id)
    }

    @Test
    fun submitResult_doesNotHideARefreshRequest() = runTest {
        repository.generateBracket(competitionId)
        service.onAction = { completedSemifinal }
        service.getMatchesError = IOException("offline")

        val result = repository.submitResult(competitionId, matchId = 1, winnerId = 11, score1 = 3, score2 = 1)

        assertTrue(result is AppResult.Success)
        assertEquals(AppResult.Failure(AppError.Network), repository.refreshMatches(competitionId))
        assertEquals(MatchStatus.COMPLETED, cached()?.single { it.id == 1L }?.status)
    }

    @Test
    fun resetBracket_leavesEmptyList() = runTest {
        repository.generateBracket(competitionId)

        assertEquals(AppResult.Success(Unit), repository.resetBracket(competitionId))
        assertEquals(emptyList<Any>(), cached())
    }

    @Test
    fun rejectedAction_returnsApiErrorAndKeepsCache() = runTest {
        repository.generateBracket(competitionId)
        service.failNext(apiError(409, "MATCH_NOT_READY"))

        val result = repository.submitResult(competitionId, matchId = 3, winnerId = 11, score1 = null, score2 = null)

        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.MATCH_NOT_READY, 409)), result)
        assertEquals(listOf(1L, 2L, 3L), cached()?.map { it.id })
    }
}
