package ac.sfj.miniappcompetition.ui.feature.match.viewmodel

import ac.sfj.miniappcompetition.data.model.*
import ac.sfj.miniappcompetition.data.repository.*
import ac.sfj.miniappcompetition.data.service.MatchApiService
import ac.sfj.miniappcompetition.data.dto.SubmitMatchResultRequestDto
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BracketViewModelTest {
    private val competitions = FakeCompetitionApiService()
    private val participants = FakeParticipantApiService()
    private val matches = FakeMatchApiService()
    private val ready = matchDto(1,1,1,1,participant1Id = 11,participant2Id = 12,status = "READY")
    @Before fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        competitions.competitions += competitionDto(1,status = "IN_MATCH")
        participants.participants += listOf(participantDto(11,1), participantDto(12,1))
        matches.matches += ready
    }
    @After fun teardown() { Dispatchers.resetMain() }
    private fun create(service: MatchApiService = matches) = BracketViewModel(1,
        NetworkCompetitionRepository(competitions), NetworkParticipantRepository(participants), NetworkMatchRepository(service))
    private fun completeFinal() {
        matches.onAction = {
            val completed = ready.copy(winnerId = 11, score1 = 2, score2 = 1, status = "COMPLETED")
            matches.matches = mutableListOf(completed)
            competitions.competitions[0] = competitions.competitions[0].copy(status = "COMPLETED")
            completed
        }
    }
    @Test fun init_loadsAllDataAndReadyRound() = runTest {
        val state = create().uiState.value
        assertTrue(state.canAct)
        assertEquals(2, state.participants?.size)
        assertEquals(1, state.selectedRound)
        assertFalse(state.canGenerate)
    }
    @Test fun submit_refreshesChampionAndCompetitionStatus() = runTest {
        completeFinal()
        val vm = create()
        vm.openResult(1)
        vm.selectWinner(MatchSlot.ONE)
        vm.submitResult()
        val state = vm.uiState.value
        assertNull(state.form)
        assertEquals(CompetitionStatus.COMPLETED, state.competition?.status)
        assertEquals(11L, state.topology.championId)
        assertEquals(1L, state.message?.savedMatchId)
        assertEquals(1, matches.submittedRequests.size)
    }
    @Test fun successfulMutationWithFailedRefresh_blocksActionsAndRetryOnlyReads() = runTest {
        completeFinal()
        val vm = create()
        vm.openResult(1)
        vm.selectWinner(MatchSlot.ONE)
        matches.getMatchesError = IOException("offline")
        vm.submitResult()
        assertEquals(AppError.Network, vm.uiState.value.syncError)
        assertNull(vm.uiState.value.form)
        assertFalse(vm.uiState.value.canAct)
        vm.submitResult()
        matches.getMatchesError = null
        vm.refresh()
        assertNull(vm.uiState.value.syncError)
        assertTrue(vm.uiState.value.canAct)
        assertEquals(1, matches.submittedRequests.size)
    }
    @Test fun networkFailure_preservesFormAndSelectedWinner() = runTest {
        val vm = create()
        vm.openResult(1)
        vm.selectWinner(MatchSlot.TWO)
        matches.failNext(IOException("offline"))
        vm.submitResult()
        assertEquals(AppError.Network, vm.uiState.value.form?.error)
        assertEquals(MatchSlot.TWO, vm.uiState.value.form?.winner)
        assertTrue(vm.uiState.value.canAct)
    }
    @Test fun staleMatch_closesSheetAndReloads() = runTest {
        val vm = create()
        vm.openResult(1)
        vm.selectWinner(MatchSlot.ONE)
        matches.matches[0] = ready.copy(status = "COMPLETED", winnerId = 12)
        matches.failNext(apiError(409,"MATCH_NOT_READY"))
        vm.submitResult()
        assertNull(vm.uiState.value.form)
        assertEquals(MatchStatus.COMPLETED, vm.uiState.value.matches?.single()?.status)
        assertEquals(ApiErrorCode.MATCH_NOT_READY, vm.uiState.value.message?.error?.apiCode)
    }
    @Test fun generationAndReset_navigateOnlyAfterSynchronization() = runTest {
        competitions.competitions[0] = competitionDto(1)
        matches.matches.clear()
        val generatingService = object : MatchApiService by matches {
            override suspend fun generateBracket(competitionId: Long) = listOf(ready).also {
                matches.matches = it.toMutableList()
                competitions.competitions[0] = competitionDto(1,status = "IN_MATCH")
            }
            override suspend fun resetBracket(competitionId: Long) {
                matches.matches.clear()
                competitions.competitions[0] = competitionDto(1)
            }
        }
        val vm = create(generatingService)
        assertTrue(vm.uiState.value.canGenerate)
        vm.requestGenerate()
        vm.confirm()
        assertEquals(BracketNavigation.Bracket, vm.uiState.value.navigation)
        assertTrue(vm.uiState.value.canReset)
        vm.navigationHandled()
        vm.requestReset()
        vm.confirm()
        assertEquals(BracketNavigation.Participants, vm.uiState.value.navigation)
        assertEquals(CompetitionStatus.OPEN, vm.uiState.value.competition?.status)
        assertTrue(vm.uiState.value.matches!!.isEmpty())
    }
    @Test fun duplicateSubmitAndEditingDuringMutation_areIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        var count = 0
        completeFinal()
        val service = object : MatchApiService by matches {
            override suspend fun submitResult(competitionId: Long, matchId: Long, request: SubmitMatchResultRequestDto): ac.sfj.miniappcompetition.data.dto.MatchResponseDto {
                count++
                gate.await()
                return matches.submitResult(competitionId, matchId, request)
            }
        }
        val vm = create(service)
        vm.openResult(1)
        vm.selectWinner(MatchSlot.ONE)
        vm.submitResult()
        vm.submitResult()
        vm.selectWinner(MatchSlot.TWO)
        vm.dismissForm()
        assertEquals(MatchSlot.ONE, vm.uiState.value.form?.winner)
        assertEquals(1, count)
        gate.complete(Unit)
        assertNull(vm.uiState.value.form)
    }
    @Test fun undoFinal_restoresInMatchAndClearsChampion() = runTest {
        completeFinal()
        val vm = create()
        vm.openResult(1)
        vm.selectWinner(MatchSlot.ONE)
        vm.submitResult()
        matches.onAction = {
            competitions.competitions[0] = competitionDto(1,status = "IN_MATCH")
            matches.matches = mutableListOf(ready)
            ready
        }
        vm.requestUndo(1)
        vm.confirm()
        assertEquals(CompetitionStatus.IN_MATCH, vm.uiState.value.competition?.status)
        assertNull(vm.uiState.value.topology.championId)
        assertTrue(vm.uiState.value.canReset)
    }
    @Test fun removedCompetition_signalsHostExit() = runTest {
        val vm = create()
        competitions.competitions.clear()
        vm.refresh()
        assertTrue(vm.uiState.value.competitionMissing)
        assertFalse(vm.uiState.value.canAct)
    }
    @Test fun missingNamesFailure_isRetryableWithoutMutating() = runTest {
        participants.failNext(IOException("offline"))
        val vm = create()
        assertNull(vm.uiState.value.participants)
        assertFalse(vm.uiState.value.canAct)
        vm.refresh()
        assertTrue(vm.uiState.value.canAct)
    }
    @Test fun readyRoundSelection_isPreservedUntilCompletionThenSelectsFinal() = runTest {
        val first = ready.copy(nextMatchId = 2)
        val final = matchDto(2,1,2,1,status = "PENDING")
        matches.matches = mutableListOf(first, final)
        val vm = create()
        vm.selectRound(2)
        vm.refresh()
        assertEquals(2, vm.uiState.value.selectedRound)
        vm.selectRound(1)
        competitions.competitions[0] = competitionDto(1,status = "COMPLETED")
        matches.matches[1] = final.copy(participant1Id = 11, participant2Id = 12, winnerId = 11, status = "COMPLETED")
        vm.refresh()
        assertEquals(2, vm.uiState.value.selectedRound)
    }

    @Test fun resetRefreshFailure_defersNavigationAndRetryDoesNotResetAgain() = runTest {
        var resets = 0
        val service = object : MatchApiService by matches {
            override suspend fun resetBracket(competitionId: Long) {
                resets++
                matches.matches.clear()
                competitions.competitions[0] = competitionDto(1)
                matches.getMatchesError = IOException("offline")
            }
        }
        val vm = create(service)
        vm.requestReset()
        vm.confirm()
        assertNull(vm.uiState.value.navigation)
        assertFalse(vm.uiState.value.canAct)
        matches.getMatchesError = null
        vm.refresh()
        assertEquals(BracketNavigation.Participants, vm.uiState.value.navigation)
        assertEquals(1, resets)
    }

    @Test fun rejectedGeneration_refreshesChangedParticipantCount() = runTest {
        competitions.competitions[0] = competitionDto(1)
        matches.matches.clear()
        val vm = create()
        assertTrue(vm.uiState.value.canGenerate)
        vm.requestGenerate()

        // Another device removes a participant after the confirmation was opened.
        participants.participants.removeLast()
        matches.failNext(apiError(409, "INVALID_PARTICIPANT_COUNT"))
        vm.confirm()

        assertEquals(1, vm.uiState.value.participants!!.size)
        assertFalse(vm.uiState.value.canGenerate)
        assertNull(vm.uiState.value.confirmation)
        assertEquals(ApiErrorCode.INVALID_PARTICIPANT_COUNT, vm.uiState.value.message?.error?.apiCode)
        assertTrue(matches.submittedRequests.isEmpty())
    }

}
