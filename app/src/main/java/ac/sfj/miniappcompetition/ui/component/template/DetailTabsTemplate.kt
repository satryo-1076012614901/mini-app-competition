package ac.sfj.miniappcompetition.ui.component.template

import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.AppListItem
import ac.sfj.miniappcompetition.ui.component.molecule.MenuAction
import ac.sfj.miniappcompetition.ui.component.molecule.OverflowMenu
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLabel
import ac.sfj.miniappcompetition.ui.component.molecule.StatusTone
import ac.sfj.miniappcompetition.ui.component.organism.AppTopBar
import ac.sfj.miniappcompetition.ui.component.organism.BarAction
import ac.sfj.miniappcompetition.ui.component.organism.BottomActionBar
import ac.sfj.miniappcompetition.ui.component.organism.PrimaryTabs
import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Kerangka layar detail dengan tab utama: app bar dengan tombol kembali dan aksi, slot header,
 * tab, konten tab, dan snackbar (detail kompetisi, Referensi UI hal. 2).
 *
 * Konten tab mengatur sendiri inset bawahnya: daftar memakai [content] `contentPadding`,
 * sedangkan tab yang memiliki `BottomActionBar` tidak perlu padding tambahan.
 */
@Composable
fun DetailTabsTemplate(
    title: String,
    tabs: List<String>,
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    actions: @Composable RowScope.() -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = title, onNavigateBack = onNavigateBack, actions = actions)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            if (header != null) {
                header()
            }
            PrimaryTabs(tabs = tabs, selectedIndex = selectedTabIndex, onSelect = onSelectTab)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                content(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun DetailTabsTemplatePreview() {
    MiniAppCompetitionTheme {
        DetailTabsTemplate(
            title = "Futsal Cup Fakultas",
            tabs = listOf("Peserta", "Bracket"),
            selectedTabIndex = 0,
            onSelectTab = {},
            onNavigateBack = {},
            actions = {
                OverflowMenu(
                    actions = listOf(
                        MenuAction(label = "Ubah", onClick = {}),
                        MenuAction(label = "Hapus", onClick = {}, destructive = true),
                    ),
                )
            },
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceSm),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusLabel(text = "Pendaftaran", tone = StatusTone.Primary)
                    Text(
                        text = "Tim",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(count = 3) { index ->
                        AppListItem(
                            headline = "Tim ${index + 1}",
                            supporting = "5 anggota",
                            leading = { AppAvatar(text = "${index + 1}") },
                        )
                    }
                }
                BottomActionBar(
                    primary = BarAction(label = "Buat bracket", onClick = {}),
                    secondary = BarAction(label = "Tambah tim", onClick = {}),
                )
            }
        }
    }
}
