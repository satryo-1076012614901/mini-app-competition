package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.ui.component.organism.AppTopBar
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Detail kompetisi, wadah tab Peserta dan Bracket (Referensi UI hal. 2 dan 8). Data dimuat
 * berdasarkan [competitionId]; [onNavigateBack] kembali ke daftar.
 *
 * Masih kerangka agar routing dapat dijalankan. Judul, status, menu Ubah dan Hapus, serta tab dibuat
 * pada tahap layar Competition.
 */
@Composable
fun CompetitionDetailScreen(
    competitionId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTopBar(title = "", onNavigateBack = onNavigateBack)
    }
}
