package ac.sfj.miniappcompetition.data.network

import ac.sfj.miniappcompetition.data.dto.ApiErrorResponseDto
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Menjalankan pemanggilan API dan memetakan kegagalannya ke [AppError] di satu tempat:
 *
 * - [HttpException] dengan body `{code, message}` menjadi [AppError.Api]; tanpa body yang dikenali
 *   menjadi [AppError.Http].
 * - [IOException] (offline, timeout, DNS) menjadi [AppError.Network].
 * - [IllegalArgumentException], termasuk `SerializationException` serta enum atau timestamp yang tidak
 *   dikenal saat pemetaan DTO, menjadi [AppError.InvalidResponse].
 *
 * Pemetaan DTO ke model sebaiknya dilakukan di dalam [call] agar kegagalannya ikut tertangani.
 * Pembatalan coroutine tetap diteruskan.
 */
internal suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        AppResult.Failure(e.toAppError())
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (e: IllegalArgumentException) {
        AppResult.Failure(AppError.InvalidResponse)
    }

private fun HttpException.toAppError(): AppError {
    val status = code()
    val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
    val code = body
        ?.let { runCatching { NetworkJson.decodeFromString(ApiErrorResponseDto.serializer(), it) }.getOrNull() }
        ?.code
        ?: return AppError.Http(status)
    return AppError.Api(code = ApiErrorCode.fromValue(code), httpStatus = status)
}
