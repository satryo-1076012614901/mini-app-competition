package ac.sfj.miniappcompetition.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Klien HTTP tunggal yang dipakai bersama oleh ketiga container.
 *
 * [baseUrl] berasal dari `BuildConfig.API_BASE_URL` (debug ke `/develop/`, release ke
 * `/production/`) dan wajib diakhiri "/" karena path endpoint ditulis relatif. Body request dan
 * response hanya dicatat ke Logcat bila [enableLogging] aktif, yaitu pada build debug.
 */
class ApiClient(
    private val baseUrl: String,
    private val enableLogging: Boolean,
) {
    private val retrofit: Retrofit by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .apply {
                if (enableLogging) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
                    )
                }
            }
            .build()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /** Membuat implementasi interface ApiService Retrofit. */
    fun <T> create(service: Class<T>): T = retrofit.create(service)
}

/** Membuat implementasi interface ApiService, misalnya `apiClient.create<CompetitionApiService>()`. */
inline fun <reified T> ApiClient.create(): T = create(T::class.java)
