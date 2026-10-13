package ac.sfj.miniappcompetition.data.model

/**
 * Kegagalan operasi data. Pesan untuk pengguna dipetakan di `ui/shared` berdasarkan jenis error dan
 * [ApiErrorCode], bukan dari `message` backend yang bahasanya bercampur (Referensi UI hal. 15).
 */
sealed interface AppError {
    /** Backend membalas body error `{code, message}`. */
    data class Api(val code: ApiErrorCode, val httpStatus: Int) : AppError

    /** Respons HTTP gagal tanpa body error dari backend, misalnya 502 dari reverse proxy. */
    data class Http(val httpStatus: Int) : AppError

    /** Tidak ada respons: perangkat offline, timeout, atau server tidak dapat dihubungi. */
    data object Network : AppError

    /** Respons tidak sesuai kontrak API, misalnya JSON rusak atau nilai enum yang belum dikenal. */
    data object InvalidResponse : AppError
}

/** Kode error backend bila error berasal dari [AppError.Api], selain itu `null`. */
val AppError.apiCode: ApiErrorCode?
    get() = (this as? AppError.Api)?.code

/** Kode error backend (`ApiErrorCode` pada repositori `afl3-miniapp`). */
enum class ApiErrorCode {
    VALIDATION_ERROR,
//    INVALID_WINNER,
//    MEMBERS_NOT_ALLOWED,
    COMPETITION_NOT_FOUND,
//    PARTICIPANT_NOT_FOUND,
//    MATCH_NOT_FOUND,
    COMPETITION_NOT_OPEN,
//    COMPETITION_NOT_IN_MATCH,
    PARTICIPANT_TYPE_LOCKED,
//    DUPLICATE_PARTICIPANT_NAME,
//    INVALID_PARTICIPANT_COUNT,
//    MATCH_NOT_READY,
//    MATCH_UNDO_NOT_ALLOWED,
    DATA_INTEGRITY_VIOLATION,
    HTTP_ERROR,
    INTERNAL_SERVER_ERROR,

    /** Kode yang belum dikenal aplikasi, misalnya kode baru dari backend. */
    UNKNOWN,
    ;

    companion object {
        /** Kode dari nilai `code` pada body error; nilai yang tidak dikenal menjadi [UNKNOWN]. */
        fun fromValue(value: String): ApiErrorCode = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
