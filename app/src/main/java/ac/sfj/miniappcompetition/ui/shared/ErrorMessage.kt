package ac.sfj.miniappcompetition.ui.shared

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import androidx.annotation.StringRes

/**
 * Pesan error untuk pengguna (Referensi UI hal. 15): apa yang terjadi, lalu apa yang perlu dilakukan.
 *
 * @property textRes teks pesan.
 * @property retryable `true` bila pesan disertai aksi Coba lagi (`R.string.action_retry`).
 */
data class ErrorMessage(
    @get:StringRes val textRes: Int,
    val retryable: Boolean = false,
)

/**
 * Pesan untuk [AppError], dipetakan dari jenis error dan [ApiErrorCode], bukan dari `message` backend
 * yang bahasanya bercampur.
 *
 * Tempat pesan tampil (snackbar, inline di field, atau error state) dan tindak lanjutnya (muat ulang
 * atau kembali ke layar sebelumnya) ditentukan oleh layar, mengikuti tabel di HELP.md bagian 6.
 */
fun AppError.toErrorMessage(): ErrorMessage = when (this) {
    is AppError.Api -> code.toErrorMessage()
    is AppError.Http -> if (httpStatus >= 500) ServerError else RequestFailed
    AppError.Network -> ErrorMessage(R.string.error_network, retryable = true)
    AppError.InvalidResponse -> ServerError
}

/**
 * Pemilik modul Participant dan Match menambahkan cabang untuk kode error modulnya di sini, bukan
 * cabang `else`. Selama ada kode di [ApiErrorCode] yang belum dipetakan, build gagal di fungsi ini.
 */
private fun ApiErrorCode.toErrorMessage(): ErrorMessage = when (this) {
    ApiErrorCode.VALIDATION_ERROR -> ErrorMessage(R.string.error_validation)
    ApiErrorCode.COMPETITION_NOT_FOUND -> ErrorMessage(R.string.error_not_found)
    ApiErrorCode.COMPETITION_NOT_OPEN -> ErrorMessage(R.string.error_competition_not_open)
    ApiErrorCode.PARTICIPANT_TYPE_LOCKED -> ErrorMessage(R.string.participant_type_locked)
    ApiErrorCode.DATA_INTEGRITY_VIOLATION -> ErrorMessage(R.string.error_data_conflict)
    ApiErrorCode.INTERNAL_SERVER_ERROR -> ServerError
    ApiErrorCode.HTTP_ERROR, ApiErrorCode.UNKNOWN -> RequestFailed
}

/** "Terjadi kendala di server." dengan aksi Coba lagi. */
private val ServerError = ErrorMessage(R.string.error_server, retryable = true)

/** "Permintaan tidak dapat diproses." */
private val RequestFailed = ErrorMessage(R.string.error_request_failed)
