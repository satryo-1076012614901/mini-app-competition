package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.MiniCompetitionApplication
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Competition
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tab layar detail kompetisi (Referensi UI hal. 2). */
enum class CompetitionDetailTab { Participants, Bracket }

/** Alasan layar detail ditutup otomatis. Layar daftar menampilkan pesannya. */
enum class CompetitionDetailExit {
    /** Kompetisi berhasil dihapus dari layar detail. */
    Deleted,

    /** Kompetisi sudah tidak ada di backend (`COMPETITION_NOT_FOUND`, Referensi UI hal. 15). */
    NotFound,
}

/** State layar detail kompetisi (Referensi UI hal. 7–8). */
data class CompetitionDetailUiState(
    /** `null` selama kompetisi belum pernah dimuat. */
    val competition: Competition? = null,
    val isRefreshing: Boolean = false,
    /** Gagal memuat saat kompetisi belum pernah dimuat; ditampilkan sebagai error state. */
    val loadError: AppError? = null,
    val selectedTab: CompetitionDetailTab = CompetitionDetailTab.Participants,
    /** Sheet Ubah kompetisi terbuka bila tidak `null`. */
    val form: CompetitionFormState? = null,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleting: Boolean = false,
    val message: CompetitionMessage? = null,
    /** Layar detail perlu ditutup; View meneruskannya ke routing. */
    val exit: CompetitionDetailExit? = null,
) {
    /** Menu Ubah hanya tampil saat Pendaftaran (Referensi UI hal. 3 dan 8). */
    val canEdit: Boolean get() = competition != null && CompetitionRules.canEdit(competition.status)

    /** Menu Hapus hanya tampil saat Pendaftaran (Referensi UI hal. 3 dan 8). */
    val canDelete: Boolean get() = competition != null && CompetitionRules.canDelete(competition.status)
}

/**
 * ViewModel detail kompetisi: memuat kompetisi, memilih tab, serta mengubah dan menghapus kompetisi.
 * Isi tab Peserta dan Bracket menjadi tanggung jawab modul Participant dan Match.
 */
class CompetitionDetailViewModel(
    private val competitionId: Long,
    private val competitionRepository: CompetitionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionDetailUiState())
    val uiState: StateFlow<CompetitionDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Nilai null (kompetisi keluar dari cache) diabaikan; penutupan layar diatur lewat exit.
            competitionRepository.observeCompetition(competitionId)
                .filterNotNull()
                .collect { competition -> _uiState.update { it.copy(competition = competition) } }
        }
        refresh()
    }

    /** Memuat ulang kompetisi. Dipanggil setiap kali detail dibuka (Referensi UI hal. 10). */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, loadError = null) }
        viewModelScope.launch {
            val error = (competitionRepository.refreshCompetition(competitionId) as? AppResult.Failure)?.error
            _uiState.update { state ->
                when {
                    error == null -> state.copy(isRefreshing = false)
                    error.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND ->
                        state.copy(isRefreshing = false, exit = CompetitionDetailExit.NotFound)

                    state.competition == null -> state.copy(isRefreshing = false, loadError = error)
                    else -> state.copy(
                        isRefreshing = false,
                        message = CompetitionMessage.Failure(error, canRetry = true),
                    )
                }
            }
        }
    }

    fun selectTab(tab: CompetitionDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openEditForm() {
        _uiState.update { state ->
            val competition = state.competition
            if (competition == null || !state.canEdit) {
                state
            } else {
                state.copy(form = CompetitionFormState.edit(competition))
            }
        }
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

    /** Menyimpan perubahan; bila berhasil, sheet ditutup dan snackbar "Kompetisi diperbarui" tampil. */
    fun submitForm() {
        val form = _uiState.value.form ?: return
        val participantType = form.participantType
        if (!form.canSave || participantType == null) return

        _uiState.update { it.copy(form = form.copy(isSaving = true, error = null)) }
        viewModelScope.launch {
            val result = competitionRepository.updateCompetition(
                competitionId = competitionId,
                name = CompetitionRules.normalizeName(form.name),
                participantType = participantType,
            )
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(form = null, message = CompetitionMessage.Updated)
                }

                is AppResult.Failure -> {
                    _uiState.update { it.withUpdateFailure(result.error) }
                    if (result.error.isStaleData()) refresh()
                }
            }
        }
    }

    fun requestDelete() {
        _uiState.update { state -> if (state.canDelete) state.copy(isDeleteDialogVisible = true) else state }
    }

    fun dismissDeleteDialog() {
        _uiState.update { state -> if (state.isDeleting) state else state.copy(isDeleteDialogVisible = false) }
    }

    /** Menghapus kompetisi; bila berhasil, layar detail ditutup dan daftar menampilkan "Kompetisi dihapus". */
    fun confirmDelete() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            val error = (competitionRepository.deleteCompetition(competitionId) as? AppResult.Failure)?.error
            _uiState.update { state ->
                val closed = state.copy(isDeleting = false, isDeleteDialogVisible = false)
                when {
                    error == null -> closed.copy(exit = CompetitionDetailExit.Deleted)
                    error.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND ->
                        closed.copy(exit = CompetitionDetailExit.NotFound)

                    else -> closed.copy(message = CompetitionMessage.Failure(error))
                }
            }
            if (error?.isStaleData() == true) refresh()
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    /**
     * Penanganan error ubah kompetisi (Referensi UI hal. 7 dan 15):
     * - `PARTICIPANT_TYPE_LOCKED`: jenis peserta dikembalikan dan dikunci, sheet tetap terbuka.
     * - `COMPETITION_NOT_FOUND`: detail ditutup.
     * - `COMPETITION_NOT_OPEN` dan `DATA_INTEGRITY_VIOLATION`: sheet ditutup, snackbar, lalu muat ulang.
     * - Error lain ditampilkan di sheet agar isian tidak hilang.
     */
    private fun CompetitionDetailUiState.withUpdateFailure(error: AppError): CompetitionDetailUiState =
        when {
            error.apiCode == ApiErrorCode.COMPETITION_NOT_FOUND ->
                copy(form = null, exit = CompetitionDetailExit.NotFound)

            form == null || error.isStaleData() -> copy(form = null, message = CompetitionMessage.Failure(error))
            error.apiCode == ApiErrorCode.PARTICIPANT_TYPE_LOCKED -> copy(form = form.lockParticipantType())
            else -> copy(form = form.copy(isSaving = false, error = error))
        }

    /** Error 409 yang berarti data di layar sudah basi, sehingga perlu dimuat ulang (Referensi UI hal. 15). */
    private fun AppError.isStaleData(): Boolean =
        apiCode == ApiErrorCode.COMPETITION_NOT_OPEN || apiCode == ApiErrorCode.DATA_INTEGRITY_VIOLATION

    companion object {
        fun factory(competitionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MiniCompetitionApplication
                CompetitionDetailViewModel(competitionId, application.competitionContainer.competitionRepository)
            }
        }
    }
}
