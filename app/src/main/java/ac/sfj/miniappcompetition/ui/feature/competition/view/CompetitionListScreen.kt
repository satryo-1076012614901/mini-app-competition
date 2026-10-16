package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.AppListItem
import ac.sfj.miniappcompetition.ui.component.molecule.FilterChipRow
import ac.sfj.miniappcompetition.ui.component.molecule.FilterOption
import ac.sfj.miniappcompetition.ui.component.organism.EmptyState
import ac.sfj.miniappcompetition.ui.component.organism.ErrorState
import ac.sfj.miniappcompetition.ui.component.organism.LoadingPlaceholder
import ac.sfj.miniappcompetition.ui.component.template.ListScreenTemplate
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailExit
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionListUiState
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionListViewModel
import ac.sfj.miniappcompetition.ui.shared.CompetitionStatusLabel
import ac.sfj.miniappcompetition.ui.shared.formatDisplayDate
import ac.sfj.miniappcompetition.ui.shared.icon
import ac.sfj.miniappcompetition.ui.shared.labelRes
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Daftar kompetisi, layar awal aplikasi (Referensi UI hal. 7–8).
 *
 * @param onOpenCompetition membuka detail kompetisi; juga dipanggil setelah Buat kompetisi berhasil.
 * @param detailExit alasan layar detail ditutup otomatis (misalnya kompetisi dihapus), ditampilkan
 *   sebagai snackbar lalu dikonsumsi lewat [onDetailExitShown].
 */
@Composable
fun CompetitionListScreen(
    onOpenCompetition: (competitionId: Long) -> Unit,
    modifier: Modifier = Modifier,
    detailExit: CompetitionDetailExit? = null,
    onDetailExitShown: () -> Unit = {},
    viewModel: CompetitionListViewModel = viewModel(factory = CompetitionListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    CompetitionMessageEffect(
        message = uiState.message,
        snackbarHostState = snackbarHostState,
        onShown = viewModel::onMessageShown,
        onRetry = viewModel::refresh,
    )

    val currentOnDetailExitShown by rememberUpdatedState(onDetailExitShown)
    LaunchedEffect(detailExit) {
        if (detailExit != null) {
            viewModel.showDetailExit(detailExit)
            currentOnDetailExitShown()
        }
    }

    val createdCompetitionId = uiState.createdCompetitionId
    val currentOnOpenCompetition by rememberUpdatedState(onOpenCompetition)
    LaunchedEffect(createdCompetitionId) {
        if (createdCompetitionId != null) {
            currentOnOpenCompetition(createdCompetitionId)
            viewModel.onCreatedCompetitionOpened()
        }
    }

    CompetitionListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onSelectStatusFilter = viewModel::selectStatusFilter,
        onOpenCompetition = onOpenCompetition,
        onCreate = viewModel::openCreateForm,
        modifier = modifier,
    )

    uiState.form?.let { form ->
        CompetitionFormSheet(
            form = form,
            onNameChange = viewModel::updateFormName,
            onParticipantTypeChange = viewModel::updateFormParticipantType,
            onSave = viewModel::submitForm,
            onDismiss = viewModel::closeForm,
        )
    }
}

/**
 * Isi layar daftar tanpa ViewModel, agar dapat di-preview. Urutan state mengikuti hal. 8: memuat
 * pertama kali, gagal memuat, kosong, lalu berisi.
 */
@Composable
internal fun CompetitionListContent(
    uiState: CompetitionListUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onSelectStatusFilter: (CompetitionStatus?) -> Unit,
    onOpenCompetition: (competitionId: Long) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val competitions = uiState.competitions
    // Filter dan FAB disembunyikan selama daftar kosong, agar tidak ada dua tombol untuk aksi yang sama.
    val hasCompetitions = !competitions.isNullOrEmpty()

    ListScreenTemplate(
        title = stringResource(R.string.competition_list_title),
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        isRefreshing = uiState.isRefreshing && competitions != null,
        onRefresh = if (competitions != null) onRefresh else null,
        filters = if (hasCompetitions) {
            { StatusFilterRow(selected = uiState.statusFilter, onSelect = onSelectStatusFilter) }
        } else {
            null
        },
        floatingActionButton = {
            if (hasCompetitions) {
                ExtendedFloatingActionButton(
                    text = { Text(text = stringResource(R.string.competition_create)) },
                    icon = { Icon(imageVector = AppIcons.Add, contentDescription = null) },
                    onClick = onCreate,
                )
            }
        },
    ) { contentPadding ->
        val loadError = uiState.loadError
        val visibleCompetitions = uiState.visibleCompetitions
        when {
            competitions == null && loadError != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = loadErrorText(loadError), onRetry = onRefresh)
            }

            competitions == null -> LoadingPlaceholder()
            competitions.isEmpty() -> FullHeightMessage(contentPadding) {
                EmptyState(
                    title = stringResource(R.string.competition_empty_title),
                    message = stringResource(R.string.competition_empty_message),
                    icon = AppIcons.Trophy,
                    actionLabel = stringResource(R.string.competition_create),
                    actionIcon = AppIcons.Add,
                    onAction = onCreate,
                )
            }

            visibleCompetitions.isNullOrEmpty() -> FullHeightMessage(contentPadding) {
                val status = uiState.statusFilter
                if (status != null) {
                    Text(
                        text = stringResource(R.string.competition_filter_empty, stringResource(status.labelRes)),
                        modifier = Modifier.padding(horizontal = Dimens.SpaceXxl),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
                items(visibleCompetitions, key = { it.id }) { competition ->
                    CompetitionListItem(competition = competition, onClick = { onOpenCompetition(competition.id) })
                }
            }
        }
    }
}

/** Baris kompetisi: jenis peserta, tanggal dibuat, dan status sebagai teks (hal. 7). */
@Composable
private fun CompetitionListItem(competition: Competition, onClick: () -> Unit) {
    AppListItem(
        headline = competition.name,
        supporting = stringResource(
            R.string.competition_item_supporting,
            stringResource(competition.participantType.labelRes),
            formatDisplayDate(competition.createdAt),
        ),
        leading = { AppAvatar(icon = competition.participantType.icon) },
        trailing = { CompetitionStatusLabel(status = competition.status) },
        onClick = onClick,
    )
}

/** Filter status di client: Semua, Pendaftaran, Berlangsung, dan Selesai (hal. 7). */
@Composable
private fun StatusFilterRow(selected: CompetitionStatus?, onSelect: (CompetitionStatus?) -> Unit) {
    val all = FilterOption<CompetitionStatus?>(value = null, label = stringResource(R.string.competition_filter_all))
    val byStatus = CompetitionStatus.entries.map { status ->
        FilterOption<CompetitionStatus?>(value = status, label = stringResource(status.labelRes))
    }
    FilterChipRow(options = listOf(all) + byStatus, selected = selected, onSelect = onSelect)
}

/**
 * Pesan di tengah area konten yang tetap dapat ditarik untuk pull-to-refresh, karena berada di dalam
 * daftar yang dapat digulir.
 */
@Composable
private fun FullHeightMessage(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}

/** Teks gagal memuat: hal. 8 untuk masalah koneksi, selain itu pesan dari pemetaan error. */
@Composable
private fun loadErrorText(error: AppError): String =
    if (error == AppError.Network) {
        stringResource(R.string.competition_list_load_failed)
    } else {
        stringResource(error.toErrorMessage().textRes)
    }

private val PreviewCompetitions = listOf(
    previewCompetition(3, "Futsal Cup Fakultas", ParticipantType.TEAM, CompetitionStatus.IN_MATCH, day = 6),
    previewCompetition(2, "Turnamen Catur Antar Kelas", ParticipantType.INDIVIDUAL, CompetitionStatus.OPEN, day = 5),
    previewCompetition(1, "Lomba Tenis Meja", ParticipantType.INDIVIDUAL, CompetitionStatus.COMPLETED, day = 1),
)

/** Data contoh preview, mengikuti mockup hal. 7. */
internal fun previewCompetition(
    id: Long,
    name: String,
    participantType: ParticipantType,
    status: CompetitionStatus,
    day: Int,
): Competition = Competition(
    id = id,
    name = name,
    participantType = participantType,
    status = status,
    createdAt = LocalDateTime.of(2026, 10, day, 10, 0).toInstant(ZoneOffset.ofHours(7)),
)

@ComponentPreviews
@Composable
private fun CompetitionListContentPreview() {
    MiniAppCompetitionTheme {
        CompetitionListContent(
            uiState = CompetitionListUiState(competitions = PreviewCompetitions),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onSelectStatusFilter = {},
            onOpenCompetition = {},
            onCreate = {},
        )
    }
}

@ComponentPreviews
@Composable
private fun CompetitionListEmptyPreview() {
    MiniAppCompetitionTheme {
        CompetitionListContent(
            uiState = CompetitionListUiState(competitions = emptyList()),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onSelectStatusFilter = {},
            onOpenCompetition = {},
            onCreate = {},
        )
    }
}
