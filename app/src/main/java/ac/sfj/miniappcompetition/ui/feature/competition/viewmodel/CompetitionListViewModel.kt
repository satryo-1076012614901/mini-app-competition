package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.MiniCompetitionApplication
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.model.apiCode
import ac.sfj.miniappcompetition.data.repository.CompetitionRepository
import ac.sfj.miniappcompetition.data.rule.CompetitionRules
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State layar daftar kompetisi (Referensi UI hal. 7–8). */
data class CompetitionListUiState(
    /** Seluruh kompetisi, terbaru lebih dulu; `null` sebelum daftar pernah dimuat. */
    val competitions: List<Competition>? = null,
    /** Filter status di client karena `GET /api/competitions` belum memiliki parameter status; `null` = Semua. */
    val statusFilter: CompetitionStatus? = null,
    val isRefreshing: Boolean = false,
    /** Gagal memuat saat daftar belum pernah dimuat; ditampilkan sebagai error state. */
    val loadError: AppError? = null,
    /** Sheet Buat kompetisi terbuka bila tidak `null`. */
    val form: CompetitionFormState? = null,
    val message: CompetitionMessage? = null,
    /** Kompetisi yang baru dibuat. Layar membuka detailnya, lalu memanggil `onCreatedCompetitionOpened`. */
    val createdCompetitionId: Long? = null,
) {
    /** Kompetisi yang sesuai filter status. */
    val visibleCompetitions: List<Competition>?
        get() = if (statusFilter == null) competitions else competitions?.filter { it.status == statusFilter }
}

/** ViewModel daftar kompetisi: memuat, memfilter, dan membuat kompetisi. */
class CompetitionListViewModel(
    private val competitionRepository: CompetitionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionListUiState())
    val uiState: StateFlow<CompetitionListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            competitionRepository.competitions.collect { competitions ->
                _uiState.update { it.copy(competitions = competitions) }
            }
        }
        refresh()
    }

    /** Memuat ulang daftar: saat layar dibuka, pull-to-refresh, dan Coba lagi. */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, loadError = null) }
        viewModelScope.launch {
            val error = (competitionRepository.refreshCompetitions() as? AppResult.Failure)?.error
            _uiState.update { state ->
                when {
                    error == null -> state.copy(isRefreshing = false)
                    state.competitions == null -> state.copy(isRefreshing = false, loadError = error)
                    else -> state.copy(
                        isRefreshing = false,
                        message = CompetitionMessage.Failure(error, canRetry = true),
                    )
                }
            }
        }
    }

    fun selectStatusFilter(status: CompetitionStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun openCreateForm() {
        _uiState.update { it.copy(form = CompetitionFormState.create()) }
    }

    fun closeForm() {
        _uiState.update { it.copy(form = null) }
    }

    fun updateFormName(name: String) {
        _uiState.update { it.copy(form = it.form?.withName(name)) }
    }

    fun updateFormParticipantType(type: ParticipantType) {
        _uiState.update { it.copy(form = it.form?.withParticipantType(type)) }
    }

    /** Membuat kompetisi; bila berhasil, layar membuka detail kompetisi baru. */
    fun submitForm() {
        val form = _uiState.value.form ?: return
        val participantType = form.participantType
        if (!form.canSave || participantType == null) return

        _uiState.update { it.copy(form = form.copy(isSaving = true, error = null)) }
        viewModelScope.launch {
            val result = competitionRepository.createCompetition(
                name = CompetitionRules.normalizeName(form.name),
                participantType = participantType,
            )
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(form = null, createdCompetitionId = result.data.id)
                }

                is AppResult.Failure -> {
                    _uiState.update { it.withCreateFailure(result.error) }
                    if (result.error.apiCode == ApiErrorCode.DATA_INTEGRITY_VIOLATION) refresh()
                }
            }
        }
    }

    /** Pesan dari layar detail yang ditutup otomatis, misalnya setelah kompetisi dihapus. */
    fun showDetailExit(exit: CompetitionDetailExit) {
        val message = when (exit) {
            CompetitionDetailExit.Deleted -> CompetitionMessage.Deleted
            CompetitionDetailExit.NotFound -> CompetitionMessage.NotFound
        }
        _uiState.update { it.copy(message = message) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onCreatedCompetitionOpened() {
        _uiState.update { it.copy(createdCompetitionId = null) }
    }

    /**
     * Error simpan ditampilkan di sheet. Data bentrok (hal. 15) menutup sheet lalu memuat ulang daftar.
     * Bila sheet sudah ditutup selama menyimpan, error ditampilkan sebagai snackbar.
     */
    private fun CompetitionListUiState.withCreateFailure(error: AppError): CompetitionListUiState =
        if (form == null || error.apiCode == ApiErrorCode.DATA_INTEGRITY_VIOLATION) {
            copy(form = null, message = CompetitionMessage.Failure(error))
        } else {
            copy(form = form.copy(isSaving = false, error = error))
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MiniCompetitionApplication
                CompetitionListViewModel(application.competitionContainer.competitionRepository)
            }
        }
    }
}
