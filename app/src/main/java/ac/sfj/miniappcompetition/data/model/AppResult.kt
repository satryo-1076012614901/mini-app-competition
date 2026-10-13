package ac.sfj.miniappcompetition.data.model

/** Hasil operasi data: berhasil dengan [Success.data] atau gagal dengan [Failure.error]. */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

/** Mengubah data hasil yang berhasil; kegagalan diteruskan apa adanya. */
inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

/** Menjalankan [action] bila berhasil, lalu mengembalikan hasil yang sama. */
inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

/** Menjalankan [action] bila gagal, lalu mengembalikan hasil yang sama. */
inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

/** Data hasil, atau `null` bila gagal. */
fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data
