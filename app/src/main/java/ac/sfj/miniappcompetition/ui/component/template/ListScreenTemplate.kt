package ac.sfj.miniappcompetition.ui.component.template

import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.AppListItem
import ac.sfj.miniappcompetition.ui.component.molecule.FilterChipRow
import ac.sfj.miniappcompetition.ui.component.molecule.FilterOption
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLabel
import ac.sfj.miniappcompetition.ui.component.molecule.StatusTone
import ac.sfj.miniappcompetition.ui.component.organism.AppTopBar
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

/**
 * Kerangka layar daftar: app bar, slot filter, konten dengan pull-to-refresh, FAB, dan snackbar
 * (daftar kompetisi, Referensi UI hal. 7).
 *
 * [content] menerima padding bawah (inset navigation bar) yang perlu dipasang sebagai
 * `contentPadding` daftar agar item terakhir tidak tertutup. Pull-to-refresh aktif bila
 * [onRefresh] diisi dan konten dapat digulir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenTemplate(
    title: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    filters: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = title) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        val contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        val body: @Composable BoxScope.() -> Unit = { content(contentPadding) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            if (filters != null) {
                filters()
            }
            if (onRefresh != null) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    content = body,
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    content = body,
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun ListScreenTemplatePreview() {
    MiniAppCompetitionTheme {
        ListScreenTemplate(
            title = "Kompetisi",
            onRefresh = {},
            filters = {
                FilterChipRow(
                    options = listOf(
                        FilterOption(value = 0, label = "Semua"),
                        FilterOption(value = 1, label = "Pendaftaran"),
                        FilterOption(value = 2, label = "Berlangsung"),
                    ),
                    selected = 0,
                    onSelect = {},
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Buat kompetisi") },
                    icon = { Icon(painter = painterResource(AppIcons.Add), contentDescription = null) },
                    onClick = {},
                )
            },
        ) { contentPadding ->
            LazyColumn(contentPadding = contentPadding) {
                items(count = 3) { index ->
                    AppListItem(
                        headline = "Kompetisi ${index + 1}",
                        supporting = "Individu, dibuat 6 Okt 2026",
                        leading = { AppAvatar(icon = AppIcons.Person) },
                        trailing = { StatusLabel(text = "Pendaftaran", tone = StatusTone.Primary) },
                        onClick = {},
                    )
                }
            }
        }
    }
}
