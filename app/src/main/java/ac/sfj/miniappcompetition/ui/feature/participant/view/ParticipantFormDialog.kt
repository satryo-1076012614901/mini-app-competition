package ac.sfj.miniappcompetition.ui.feature.participant.view

import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.NameError
import ac.sfj.miniappcompetition.data.rule.ParticipantRules
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.atom.AppTextField
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.molecule.HintText
import ac.sfj.miniappcompetition.ui.component.molecule.HintTone
import ac.sfj.miniappcompetition.ui.component.molecule.RemovableTextField
import ac.sfj.miniappcompetition.ui.component.organism.FullScreenFormDialog
import ac.sfj.miniappcompetition.ui.feature.participant.viewmodel.ParticipantFormState
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun ParticipantFormDialog(
    form: ParticipantFormState,
    onNameChange: (String) -> Unit,
    onMemberChange: (Int, String) -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isTeam = form.participantType == ParticipantType.TEAM

    FullScreenFormDialog(
        title = if (form.isEdit) {
            if (isTeam) "Ubah tim" else "Ubah peserta"
        } else {
            if (isTeam) "Tambah tim" else "Tambah peserta"
        },
        confirmLabel = "Simpan",
        onConfirm = onSave,
        onDismiss = onDismiss,
        confirmEnabled = form.canSave,
        confirmLoading = form.isSaving,
    ) {
        AppTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = if (isTeam) "Nama tim" else "Nama peserta",
            modifier = Modifier.fillMaxWidth(),
            supportingText = "Wajib diisi",
            errorText = if (form.nameError == NameError.TOO_LONG) {
                "Nama maksimal ${ParticipantRules.NAME_MAX_LENGTH} karakter"
            } else {
                null
            },
            maxLength = ParticipantRules.NAME_MAX_LENGTH,
            enabled = !form.isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
            ),
        )

        if (isTeam) {
            Spacer(Modifier.height(Dimens.SpaceLg))

            Text(
                text = "Anggota tim",
                style = MaterialTheme.typography.titleSmall,
            )

            Spacer(Modifier.height(Dimens.SpaceSm))

            form.members.forEachIndexed { index, member ->
                RemovableTextField(
                    value = member,
                    onValueChange = { value ->
                        onMemberChange(index, value)
                    },
                    label = "Anggota ${index + 1}",
                    placeholder = "Nama anggota",
                    onRemove = {
                        onRemoveMember(index)
                    },
                    removeContentDescription = "Hapus anggota ${index + 1}",
                )

                Spacer(Modifier.height(Dimens.SpaceSm))
            }

            AppButton(
                text = "Tambah anggota",
                onClick = onAddMember,
                variant = AppButtonVariant.Text,
                leadingIcon = AppIcons.Add,
                enabled = !form.isSaving,
            )
        }

        form.error?.let { error ->
            Spacer(Modifier.height(Dimens.SpaceLg))

            HintText(
                text = stringResource(error.toErrorMessage().textRes),
                icon = AppIcons.Error,
                tone = HintTone.Error,
            )
        }
    }
}