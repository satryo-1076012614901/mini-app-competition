package ac.sfj.miniappcompetition.data.dto

import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Membaca timestamp ISO-8601 dari backend (`Instant`, UTC). Format yang tidak valid dilempar sebagai
 * [IllegalArgumentException] agar `safeApiCall` memetakannya ke `AppError.InvalidResponse`.
 */
internal fun parseInstant(value: String): Instant =
    try {
        Instant.parse(value)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Timestamp tidak valid: $value", e)
    }
