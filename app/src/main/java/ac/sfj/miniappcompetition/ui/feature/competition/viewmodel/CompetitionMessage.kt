package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.data.model.AppError

/** Pesan snackbar sekali tampil di layar Competition. Teksnya dipetakan oleh View. */
sealed interface CompetitionMessage {
    /**
     * Operasi gagal; teks dari `toErrorMessage()`. [canRetry] hanya `true` untuk pemuatan data, yang aman
     * diulang lewat aksi Coba lagi. Aksi seperti hapus tidak diulang dari snackbar.
     */
    data class Failure(val error: AppError, val canRetry: Boolean = false) : CompetitionMessage

    /** "Kompetisi diperbarui" setelah Ubah kompetisi berhasil (Referensi UI hal. 7). */
    data object Updated : CompetitionMessage

    /** "Kompetisi dihapus", ditampilkan di daftar setelah kembali dari detail. */
    data object Deleted : CompetitionMessage

    /** "Data tidak ditemukan, mungkin sudah dihapus.", ditampilkan di daftar setelah kembali dari detail. */
    data object NotFound : CompetitionMessage
}
