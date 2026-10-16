package ac.sfj.miniappcompetition.routing

import kotlinx.serialization.Serializable

/**
 * Destinasi layar aplikasi (Referensi UI hal. 2). Setiap route adalah object atau data class
 * `@Serializable` untuk Navigation Compose type-safe; argumen route ditulis sebagai properti.
 *
 * Sheet dan dialog (form kompetisi, form peserta, input hasil, konfirmasi) adalah overlay di layar
 * asal, bukan route. Layar bracket penuh tidak digunakan.
 */
sealed interface AppRoute {
    /** Daftar kompetisi, layar awal aplikasi. */
    @Serializable
    data object CompetitionList : AppRoute

    /**
     * Detail kompetisi dengan tab Peserta dan Bracket. Tab aktif adalah state layar, bukan argumen
     * route, karena perpindahan tab terjadi di dalam layar (hal. 10).
     */
    @Serializable
    data class CompetitionDetail(val competitionId: Long) : AppRoute
}
