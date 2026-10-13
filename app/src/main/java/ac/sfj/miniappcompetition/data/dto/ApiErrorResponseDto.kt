package ac.sfj.miniappcompetition.data.dto

import kotlinx.serialization.Serializable

/**
 * Body error backend `{code, message}`. [code] dibaca sebagai teks agar kode baru dari backend
 * tidak menggagalkan parsing; pemetaannya ada di `ApiErrorCode.fromValue`. [message] tidak
 * ditampilkan ke pengguna.
 */
@Serializable
data class ApiErrorResponseDto(
    val code: String? = null,
    val message: String? = null,
)
