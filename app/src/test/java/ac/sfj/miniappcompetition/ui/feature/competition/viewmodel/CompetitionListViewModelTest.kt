package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.repository.FakeCompetitionApiService
import ac.sfj.miniappcompetition.data.repository.NetworkCompetitionRepository
import ac.sfj.miniappcompetition.data.repository.apiError
import ac.sfj.miniappcompetition.data.repository.competitionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Daftar kompetisi (Referensi UI hal. 7–8). Repository asli dipakai dengan fake service, sehingga
 * cache ikut teruji. `Dispatchers.Main` diganti `UnconfinedTestDispatcher`, sehingga coroutine di
 * `viewModelScope` selesai sebelum assertion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompetitionListViewModelTest {

    private val api = FakeCompetitionApiService()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CompetitionListViewModel(NetworkCompetitionRepository(api))

    @Test
    fun init_loadsCompetitionsNewestFirst() = runTest {
        api.competitions += listOf(competitionDto(1), competitionDto(2))

        val state = createViewModel().uiState.value

        assertEquals(listOf(2L, 1L), state.competitions?.map { it.id })
        assertFalse(state.isRefreshing)
        assertNull(state.loadError)
    }

    @Test
    fun initialLoadFailure_showsErrorStateUntilRetrySucceeds() = runTest {
        api.failNext(IOException("offline"))
        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.competitions)
        assertEquals(AppError.Network, viewModel.uiState.value.loadError)

        api.competitions += competitionDto(1)
        viewModel.refresh()

        assertEquals(listOf(1L), viewModel.uiState.value.competitions?.map { it.id })
        assertNull(viewModel.uiState.value.loadError)
    }

    @Test
    fun refreshFailureWithData_keepsListAndShowsRetryableMessage() = runTest {
        api.competitions += competitionDto(1)
        val viewModel = createViewModel()
        api.failNext(apiError(500, "INTERNAL_SERVER_ERROR"))

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L), state.competitions?.map { it.id })
        assertNull(state.loadError)
        assertEquals(
            CompetitionMessage.Failure(AppError.Api(ApiErrorCode.INTERNAL_SERVER_ERROR, 500), canRetry = true),
            state.message,
        )

        viewModel.onMessageShown()
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun statusFilter_showsMatchingCompetitionsOnly() = runTest {
        api.competitions += listOf(
            competitionDto(1, status = "OPEN"),
            competitionDto(2, status = "IN_MATCH"),
            competitionDto(3, status = "COMPLETED"),
        )
        val viewModel = createViewModel()

        viewModel.selectStatusFilter(CompetitionStatus.IN_MATCH)
        assertEquals(listOf(2L), viewModel.uiState.value.visibleCompetitions?.map { it.id })

        viewModel.selectStatusFilter(null)
        assertEquals(listOf(3L, 2L, 1L), viewModel.uiState.value.visibleCompetitions?.map { it.id })
    }

    @Test
    fun create_closesFormAndOpensNewCompetition() = runTest {
        val viewModel = createViewModel()

        viewModel.openCreateForm()
        viewModel.updateFormName("  Futsal Cup  ")
        viewModel.updateFormParticipantType(ParticipantType.TEAM)
        viewModel.submitForm()

        val state = viewModel.uiState.value
        val created = state.competitions.orEmpty().single()
        assertNull(state.form)
        assertEquals("Futsal Cup", created.name)
        assertEquals(ParticipantType.TEAM, created.participantType)
        assertEquals(created.id, state.createdCompetitionId)

        viewModel.onCreatedCompetitionOpened()
        assertNull(viewModel.uiState.value.createdCompetitionId)
    }

    @Test
    fun create_isIgnoredUntilParticipantTypeIsChosen() = runTest {
        val viewModel = createViewModel()

        viewModel.openCreateForm()
        viewModel.updateFormName("Futsal Cup")
        viewModel.submitForm()

        assertTrue(api.competitions.isEmpty())
        assertEquals(false, viewModel.uiState.value.form?.isSaving)
    }

    @Test
    fun createFailure_keepsFormOpenWithError() = runTest {
        val viewModel = createViewModel()
        viewModel.openCreateForm()
        viewModel.updateFormName("Futsal Cup")
        viewModel.updateFormParticipantType(ParticipantType.TEAM)
        api.failNext(IOException("offline"))

        viewModel.submitForm()

        val form = viewModel.uiState.value.form
        assertEquals(AppError.Network, form?.error)
        assertEquals(false, form?.isSaving)
        assertEquals("Futsal Cup", form?.name)
        assertNull(viewModel.uiState.value.createdCompetitionId)
    }

    @Test
    fun detailExit_isShownAsMessage() = runTest {
        val viewModel = createViewModel()

        viewModel.showDetailExit(CompetitionDetailExit.Deleted)
        assertEquals(CompetitionMessage.Deleted, viewModel.uiState.value.message)

        viewModel.showDetailExit(CompetitionDetailExit.NotFound)
        assertEquals(CompetitionMessage.NotFound, viewModel.uiState.value.message)
    }
}
