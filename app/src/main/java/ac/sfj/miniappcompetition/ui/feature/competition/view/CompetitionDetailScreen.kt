package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.ui.feature.match.view.BracketContent
import ac.sfj.miniappcompetition.ui.feature.match.view.MatchOverlays
import ac.sfj.miniappcompetition.ui.feature.match.view.MatchSyncWarning
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketNavigation
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketViewModel
import androidx.compose.material3.SnackbarResult
import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.molecule.MenuAction
import ac.sfj.miniappcompetition.ui.component.molecule.OverflowMenu
import ac.sfj.miniappcompetition.ui.component.organism.ConfirmationDialog
import ac.sfj.miniappcompetition.ui.component.organism.EmptyState
import ac.sfj.miniappcompetition.ui.component.organism.ErrorState
import ac.sfj.miniappcompetition.ui.component.organism.LoadingPlaceholder
import ac.sfj.miniappcompetition.ui.component.template.DetailTabsTemplate
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailExit
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailTab
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailUiState
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailViewModel
import ac.sfj.miniappcompetition.ui.feature.participant.view.ParticipantTab
import ac.sfj.miniappcompetition.ui.shared.CompetitionStatusLabel
import ac.sfj.miniappcompetition.ui.shared.labelRes
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Detail kompetisi, wadah tab Peserta dan Bracket (Referensi UI hal. 2, 7, dan 8).
 *
 * @param onNavigateBack kembali ke daftar lewat tombol kembali.
 * @param onExit menutup detail secara otomatis, misalnya setelah kompetisi dihapus; daftar menampilkan
 *   pesannya.
 */
@Composable
fun CompetitionDetailScreen(
    competitionId: Long,
    onNavigateBack: () -> Unit,
    onExit: (CompetitionDetailExit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompetitionDetailViewModel = viewModel(factory = CompetitionDetailViewModel.factory(competitionId)),
) {
    val matchViewModel: BracketViewModel = viewModel(
        key = "bracket-$competitionId", factory = BracketViewModel.factory(competitionId))
    val matchState by matchViewModel.uiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    CompetitionMessageEffect(
        message = uiState.message,
        snackbarHostState = snackbarHostState,
        onShown = viewModel::onMessageShown,
        onRetry = viewModel::refresh,
    )

    val exit = uiState.exit
    val currentOnExit by rememberUpdatedState(onExit)
    LaunchedEffect(exit) {
        if (exit != null) currentOnExit(exit)
    }

    val message = matchState.message
    val messageText = message?.let {
        if (it.error != null) stringResource(it.error.toErrorMessage().textRes)
        else stringResource(if (it.savedMatchId != null) R.string.match_saved else R.string.match_action_done)
    }
    val undoLabel = stringResource(R.string.match_undo)
    val retryLabel = stringResource(R.string.action_retry)
    val canSnackbarUndo = message?.savedMatchId != null && matchState.syncError == null
    val canSnackbarRetry = message?.error?.toErrorMessage()?.retryable == true
    LaunchedEffect(message?.id) {
        if (message != null && messageText != null) {
            val result = snackbarHostState.showSnackbar(messageText,
                actionLabel = if (canSnackbarUndo) undoLabel else if (canSnackbarRetry) retryLabel else null)
            matchViewModel.messageShown(message.id)
            if (result == SnackbarResult.ActionPerformed) {
                if (canSnackbarUndo) message.savedMatchId?.let(matchViewModel::undo)
                else matchViewModel.refresh()
            }
        }
    }
    LaunchedEffect(matchState.navigation) {
        matchState.navigation?.let { target ->
            viewModel.selectTab(if (target == BracketNavigation.Bracket) CompetitionDetailTab.Bracket else CompetitionDetailTab.Participants)
            matchViewModel.navigationHandled()
        }
    }
    LaunchedEffect(matchState.competitionMissing) {
        if (matchState.competitionMissing) currentOnExit(CompetitionDetailExit.NotFound)
    }
    MatchOverlays(matchState, matchViewModel)

    CompetitionDetailContent(
        uiState = uiState,
        onResetBracket = matchViewModel::requestReset,
        resetEnabled = matchState.canReset,
        bracketContent = { _, padding ->
            BracketContent(matchState, padding, matchViewModel::selectRound, matchViewModel::openResult,
                matchViewModel::requestUndo, matchViewModel::refresh,
                { viewModel.selectTab(CompetitionDetailTab.Participants) })
        },
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onSelectTab = viewModel::selectTab,
        onEdit = viewModel::openEditForm,
        onDelete = viewModel::requestDelete,
        onRetry = viewModel::refresh,
        participantContent = { competition, contentPadding ->
            ParticipantTab(
                competitionId = competition.id,
                participantType = competition.participantType,
                competitionStatus = competition.status,
                contentPadding = contentPadding,
                onGenerateBracket = matchViewModel::requestGenerate,
                onOpenBracket = { viewModel.selectTab(CompetitionDetailTab.Bracket) },
                canGenerateBracket = matchState.canGenerate,
                matchBusy = matchState.isMutating || matchState.isRefreshing || matchState.confirmation != null,
                matchHeader = {
                    if (matchState.syncError != null) MatchSyncWarning(matchViewModel::refresh,
                        !matchState.isRefreshing && !matchState.isMutating)
                },
            )
        },
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

    val competition = uiState.competition
    if (uiState.isDeleteDialogVisible && competition != null) {
        DeleteCompetitionDialog(
            competitionName = competition.name,
            deleting = uiState.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog,
        )
    }
}

/** Isi layar detail tanpa ViewModel, agar dapat di-preview. */
@Composable
internal fun CompetitionDetailContent(
    uiState: CompetitionDetailUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onSelectTab: (CompetitionDetailTab) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    participantContent: @Composable (Competition, PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    bracketContent: @Composable (Competition, PaddingValues) -> Unit = { _, _ -> },
    onResetBracket: () -> Unit = {},
    resetEnabled: Boolean = false,
) {
    val competition = uiState.competition
    val editLabel = stringResource(R.string.action_edit)
    val deleteLabel = stringResource(R.string.action_delete)
    val resetLabel = stringResource(R.string.match_reset)

    DetailTabsTemplate(
        title = competition?.name.orEmpty(),
        tabs = CompetitionDetailTab.entries.map { tab -> stringResource(tab.titleRes()) },
        selectedTabIndex = uiState.selectedTab.ordinal,
        onSelectTab = { index -> onSelectTab(CompetitionDetailTab.entries[index]) },
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        actions = {
            // Aksi yang tidak berlaku pada status saat ini disembunyikan, bukan dinonaktifkan (hal. 8).
            OverflowMenu(
                actions = buildList {
                    if (competition?.status == CompetitionStatus.IN_MATCH) {
                        add(MenuAction(resetLabel, onResetBracket, destructive = true, enabled = resetEnabled))
                    }
                    if (uiState.canEdit) add(MenuAction(label = editLabel, onClick = onEdit))
                    if (uiState.canDelete) {
                        add(MenuAction(label = deleteLabel, onClick = onDelete, destructive = true))
                    }
                },
            )
        },
        header = if (competition != null) {
            { CompetitionHeader(competition) }
        } else {
            null
        },
    ) { contentPadding ->
        val loadError = uiState.loadError
        when {
            competition == null && loadError != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = stringResource(loadError.toErrorMessage().textRes), onRetry = onRetry)
            }

            competition == null -> LoadingPlaceholder(showLeading = false)

            // Titik sambung tab: ganti placeholder dengan tab dari modul Participant dan Match.
            else -> when (uiState.selectedTab) {
                CompetitionDetailTab.Participants ->
                    participantContent(competition, contentPadding)

                CompetitionDetailTab.Bracket -> bracketContent(competition, contentPadding)
            }
        }
    }
}

/** Status dan jenis peserta di bawah app bar (hal. 7). */
@Composable
private fun CompetitionHeader(competition: Competition) {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceSm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompetitionStatusLabel(status = competition.status)
        Text(
            text = stringResource(competition.participantType.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Placeholder sementara sampai tab dari modul Participant dan Match tersedia. */
@Composable
private fun TabPlaceholder(title: String, message: String, icon: ImageVector, contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(title = title, message = message, icon = icon)
    }
}

/** Konfirmasi hapus: menyebut bahwa peserta ikut terhapus karena `ON DELETE CASCADE` (hal. 8). */
@Composable
private fun DeleteCompetitionDialog(
    competitionName: String,
    deleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.competition_delete_title),
        text = stringResource(R.string.competition_delete_message, competitionName),
        confirmLabel = stringResource(R.string.action_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = AppIcons.Delete,
        destructive = true,
        confirmLoading = deleting,
    )
}

private fun CompetitionDetailTab.titleRes(): Int = when (this) {
    CompetitionDetailTab.Participants -> R.string.competition_tab_participants
    CompetitionDetailTab.Bracket -> R.string.competition_tab_bracket
}

@ComponentPreviews
@Composable
private fun CompetitionDetailContentPreview() {
    MiniAppCompetitionTheme {
        CompetitionDetailContent(
            uiState = CompetitionDetailUiState(
                competition = previewCompetition(
                    id = 2,
                    name = "Turnamen Catur Antar Kelas",
                    participantType = ParticipantType.INDIVIDUAL,
                    status = CompetitionStatus.OPEN,
                    day = 5,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            participantContent = { _, contentPadding ->
                TabPlaceholder(
                    title = stringResource(R.string.competition_tab_participants),
                    message = stringResource(R.string.competition_tab_participants_placeholder),
                    icon = AppIcons.Groups,
                    contentPadding = contentPadding,
                )
            },
            onNavigateBack = {},
            onSelectTab = {},
            onEdit = {},
            onDelete = {},
            onRetry = {},
        )
    }
}
