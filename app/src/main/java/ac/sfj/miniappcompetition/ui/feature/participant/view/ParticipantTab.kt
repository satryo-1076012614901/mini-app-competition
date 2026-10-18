package ac.sfj.miniappcompetition.ui.feature.participant.view

import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.CompetitionRules
import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.molecule.AppListItem
import ac.sfj.miniappcompetition.ui.component.molecule.MenuAction
import ac.sfj.miniappcompetition.ui.component.molecule.OverflowMenu
import ac.sfj.miniappcompetition.ui.component.organism.BarAction
import ac.sfj.miniappcompetition.ui.component.organism.BottomActionBar
import ac.sfj.miniappcompetition.ui.component.organism.ConfirmationDialog
import ac.sfj.miniappcompetition.ui.component.organism.EmptyState
import ac.sfj.miniappcompetition.ui.component.organism.ErrorState
import ac.sfj.miniappcompetition.ui.component.organism.LoadingPlaceholder
import ac.sfj.miniappcompetition.ui.feature.participant.viewmodel.ParticipantViewModel
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ParticipantTab(
    competitionId: Long,
    participantType: ParticipantType,
    competitionStatus: CompetitionStatus,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: ParticipantViewModel = viewModel(
        factory = ParticipantViewModel.factory(competitionId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val participants = uiState.participants
    val loadError = uiState.loadError

    val canManage =
        CompetitionRules.canManageParticipants(competitionStatus)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                participants == null && loadError != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        ErrorState(
                            message = stringResource(
                                loadError.toErrorMessage().textRes,
                            ),
                            onRetry = viewModel::refresh,
                        )
                    }
                }

                participants == null -> {
                    LoadingPlaceholder()
                }

                participants.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            title = "Belum ada peserta",
                            message = if (
                                participantType == ParticipantType.TEAM
                            ) {
                                "Tambahkan tim untuk mengikuti kompetisi ini."
                            } else {
                                "Tambahkan peserta untuk mengikuti kompetisi ini."
                            },
                            icon = if (
                                participantType == ParticipantType.TEAM
                            ) {
                                AppIcons.Groups
                            } else {
                                AppIcons.Person
                            },
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = if (canManage) {
                            PaddingValues()
                        } else {
                            contentPadding
                        },
                    ) {
                        items(
                            items = participants,
                            key = { participant -> participant.id },
                        ) { participant ->
                            AppListItem(
                                headline = participant.name,
                                supporting = if (
                                    participantType == ParticipantType.TEAM
                                ) {
                                    "${participant.members.size} anggota"
                                } else {
                                    null
                                },
                                leading = {
                                    AppAvatar(
                                        icon = if (
                                            participantType == ParticipantType.TEAM
                                        ) {
                                            AppIcons.Groups
                                        } else {
                                            AppIcons.Person
                                        },
                                    )
                                },
                                trailing = if (canManage) {
                                    {
                                        OverflowMenu(
                                            actions = listOf(
                                                MenuAction(
                                                    label = "Ubah",
                                                    onClick = {
                                                        viewModel.openEditForm(
                                                            participantType,
                                                            participant,
                                                        )
                                                    },
                                                ),
                                                MenuAction(
                                                    label = "Hapus",
                                                    onClick = {
                                                        viewModel.requestDelete(
                                                            participant,
                                                        )
                                                    },
                                                    destructive = true,
                                                ),
                                            ),
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }

        if (canManage) {
            BottomActionBar(
                primary = BarAction(
                    label = if (participantType == ParticipantType.TEAM) {
                        "Tambah tim"
                    } else {
                        "Tambah peserta"
                    },
                    onClick = {
                        viewModel.openCreateForm(participantType)
                    },
                    icon = AppIcons.Add,
                ),
            )
        }
    }

    uiState.form?.let { form ->
        ParticipantFormDialog(
            form = form,
            onNameChange = viewModel::updateFormName,
            onMemberChange = viewModel::updateMember,
            onAddMember = viewModel::addMember,
            onRemoveMember = viewModel::removeMember,
            onSave = viewModel::submitForm,
            onDismiss = viewModel::closeForm,
        )
    }

    uiState.participantToDelete?.let { participant ->
        ConfirmationDialog(
            title = if (participantType == ParticipantType.TEAM) {
                "Hapus tim?"
            } else {
                "Hapus peserta?"
            },
            text = "\"${participant.name}\" akan dihapus permanen dari kompetisi ini.",
            confirmLabel = "Hapus",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog,
            icon = AppIcons.Delete,
            destructive = true,
            confirmLoading = uiState.isDeleting,
        )
    }
}