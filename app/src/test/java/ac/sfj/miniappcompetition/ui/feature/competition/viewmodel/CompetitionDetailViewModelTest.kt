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

/** Detail kompetisi: muat, ubah, dan hapus (Referensi UI hal. 7, 8, dan 15). */
@OptIn(ExperimentalCoroutinesApi::class)
class CompetitionDetailViewModelTest {

    private val api = FakeCompetitionApiService()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        api.competitions += competitionDto(1, name = "Turnamen Catur", participantType = "INDIVIDUAL")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(competitionId: Long = 1) =
        CompetitionDetailViewModel(competitionId, NetworkCompetitionRepository(api))

    private fun setStatus(status: String) {
        api.competitions[0] = api.competitions[0].copy(status = status)
    }

    @Test
    fun init_loadsCompetitionOnParticipantsTab() = runTest {
        val state = createViewModel().uiState.value

        assertEquals("Turnamen Catur", state.competition?.name)
        assertEquals(CompetitionDetailTab.Participants, state.selectedTab)
        assertTrue(state.canEdit)
        assertTrue(state.canDelete)
    }

    @Test
    fun notFoundOnLoad_exitsWithNotFound() = runTest {
        val state = createViewModel(competitionId = 99).uiState.value

        assertEquals(CompetitionDetailExit.NotFound, state.exit)
        assertNull(state.loadError)
    }

    @Test
    fun loadFailureWithoutData_showsErrorState() = runTest {
        api.failNext(IOException("offline"))

        val state = createViewModel().uiState.value

        assertNull(state.competition)
        assertEquals(AppError.Network, state.loadError)
    }

    @Test
    fun editAndDelete_areHiddenOutsideRegistration() = runTest {
        setStatus("IN_MATCH")
        val viewModel = createViewModel()

        viewModel.openEditForm()
        viewModel.requestDelete()

        val state = viewModel.uiState.value
        assertFalse(state.canEdit)
        assertFalse(state.canDelete)
        assertNull(state.form)
        assertFalse(state.isDeleteDialogVisible)
    }

    @Test
    fun edit_savesAndShowsUpdatedMessage() = runTest {
        val viewModel = createViewModel()

        viewModel.openEditForm()
        viewModel.updateFormName("Turnamen Catur Antar Kelas")
        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertNull(state.form)
        assertEquals("Turnamen Catur Antar Kelas", state.competition?.name)
        assertEquals(CompetitionMessage.Updated, state.message)
    }

    @Test
    fun editRejectedBecauseParticipantTypeLocked_restoresAndLocksType() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditForm()
        viewModel.updateFormParticipantType(ParticipantType.TEAM)
        api.failNext(apiError(409, "PARTICIPANT_TYPE_LOCKED"))

        viewModel.submitForm()

        val form = viewModel.uiState.value.form
        assertEquals(ParticipantType.INDIVIDUAL, form?.participantType)
        assertEquals(true, form?.participantTypeLocked)
        assertEquals(false, form?.isSaving)
        assertNull(form?.error)
    }

    @Test
    fun editRejectedBecauseNotOpen_closesFormAndReloads() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditForm()
        viewModel.updateFormName("Nama baru")
        setStatus("IN_MATCH")
        api.failNext(apiError(409, "COMPETITION_NOT_OPEN"))

        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertNull(state.form)
        assertEquals(
            CompetitionMessage.Failure(AppError.Api(ApiErrorCode.COMPETITION_NOT_OPEN, 409)),
            state.message,
        )
        assertEquals(CompetitionStatus.IN_MATCH, state.competition?.status)
        assertFalse(state.canEdit)
    }

    @Test
    fun editNetworkFailure_keepsFormOpenWithError() = runTest {
        val viewModel = createViewModel()
        viewModel.openEditForm()
        viewModel.updateFormName("Nama baru")
        api.failNext(IOException("offline"))

        viewModel.submitForm()

        val form = viewModel.uiState.value.form
        assertEquals(AppError.Network, form?.error)
        assertEquals("Nama baru", form?.name)
    }

    @Test
    fun delete_exitsWithDeleted() = runTest {
        val viewModel = createViewModel()

        viewModel.requestDelete()
        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)
        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertEquals(CompetitionDetailExit.Deleted, state.exit)
        assertFalse(state.isDeleteDialogVisible)
        assertTrue(api.competitions.isEmpty())
    }

    @Test
    fun deleteRejectedBecauseNotOpen_closesDialogAndReloads() = runTest {
        val viewModel = createViewModel()
        viewModel.requestDelete()
        setStatus("IN_MATCH")
        api.failNext(apiError(409, "COMPETITION_NOT_OPEN"))

        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertNull(state.exit)
        assertFalse(state.isDeleteDialogVisible)
        assertEquals(
            CompetitionMessage.Failure(AppError.Api(ApiErrorCode.COMPETITION_NOT_OPEN, 409)),
            state.message,
        )
        assertEquals(CompetitionStatus.IN_MATCH, state.competition?.status)
    }

    @Test
    fun deleteOfAlreadyRemovedCompetition_exitsWithNotFound() = runTest {
        val viewModel = createViewModel()
        viewModel.requestDelete()
        api.competitions.clear()

        viewModel.confirmDelete()

        assertEquals(CompetitionDetailExit.NotFound, viewModel.uiState.value.exit)
    }
}
