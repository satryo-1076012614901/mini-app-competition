package ac.sfj.miniappcompetition.data.network

import kotlinx.serialization.json.Json

/**
 * Konfigurasi JSON bersama untuk converter Retrofit dan pembacaan body error. Field baru dari
 * backend diabaikan agar aplikasi tidak gagal saat respons bertambah field.
 */
internal val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
}
