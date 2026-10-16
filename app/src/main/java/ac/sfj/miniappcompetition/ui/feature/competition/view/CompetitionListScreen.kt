package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.template.ListScreenTemplate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Daftar kompetisi, layar awal aplikasi (Referensi UI hal. 7–8). [onOpenCompetition] dipanggil
 * dengan ID kompetisi saat item diketuk.
 *
 * Masih kerangka agar routing dapat dijalankan. Daftar, filter status, pull-to-refresh, empty state,
 * dan FAB Buat kompetisi dibuat pada tahap layar Competition.
 */
@Composable
fun CompetitionListScreen(
    onOpenCompetition: (competitionId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListScreenTemplate(
        title = stringResource(R.string.competition_list_title),
        modifier = modifier,
    ) {
        // Isi daftar dibuat pada tahap layar Competition.
    }
}
