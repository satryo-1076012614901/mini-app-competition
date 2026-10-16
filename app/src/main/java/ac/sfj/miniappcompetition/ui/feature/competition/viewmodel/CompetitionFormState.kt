package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.CompetitionRules
import ac.sfj.miniappcompetition.data.rule.NameError

/**
 * Isian sheet Buat dan Ubah kompetisi (Referensi UI hal. 7). Sheet yang sama dipakai daftar kompetisi
 * (buat) dan detail kompetisi (ubah).
 */
data class CompetitionFormState(
    /** ID kompetisi yang diubah; `null` untuk Buat kompetisi. */
    val competitionId: Long? = null,
    val name: String = "",
    /** `null` selama belum dipilih. Pada mode buat, panitia memilih jenis peserta secara sadar. */
    val participantType: ParticipantType? = null,
    /** Jenis peserta yang tersimpan di backend, dipakai untuk mengembalikan pilihan saat terkunci. */
    val originalParticipantType: ParticipantType? = null,
    /** Jenis peserta tidak dapat diubah karena kompetisi sudah memiliki peserta. */
    val participantTypeLocked: Boolean = false,
    val isSaving: Boolean = false,
    /** Error simpan. Ditampilkan di dalam sheet karena snackbar layar tertutup oleh sheet. */
    val error: AppError? = null,
) {
    val isEdit: Boolean get() = competitionId != null

    /** Validasi yang sama dengan backend: wajib diisi dan maksimal 255 karakter setelah dipangkas. */
    val nameError: NameError? get() = CompetitionRules.validateName(name)

    val canSave: Boolean get() = nameError == null && participantType != null && !isSaving

    fun withName(name: String): CompetitionFormState = copy(name = name, error = null)

    fun withParticipantType(type: ParticipantType): CompetitionFormState =
        if (participantTypeLocked) this else copy(participantType = type, error = null)

    /** Backend menolak perubahan jenis peserta: pilihan dikembalikan ke nilai tersimpan, lalu dikunci. */
    fun lockParticipantType(): CompetitionFormState = copy(
        participantType = originalParticipantType,
        participantTypeLocked = true,
        isSaving = false,
        error = null,
    )

    companion object {
        /** Form kosong untuk Buat kompetisi; jenis peserta belum terpilih. */
        fun create(): CompetitionFormState = CompetitionFormState()

        /**
         * Form Ubah kompetisi berisi data saat ini. Jenis peserta dikunci bila [participantCount] lebih dari
         * nol. Selama modul Participant belum aktif, jumlah peserta tidak diketahui (`null`), sehingga kunci
         * baru diterapkan setelah backend menolak perubahan dengan `PARTICIPANT_TYPE_LOCKED`.
         */
        fun edit(competition: Competition, participantCount: Int? = null): CompetitionFormState =
            CompetitionFormState(
                competitionId = competition.id,
                name = competition.name,
                participantType = competition.participantType,
                originalParticipantType = competition.participantType,
                participantTypeLocked = participantCount != null &&
                    CompetitionRules.isParticipantTypeLocked(participantCount),
            )
    }
}
