package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.CompetitionRules
import ac.sfj.miniappcompetition.data.rule.NameError
import ac.sfj.miniappcompetition.ui.component.atom.AppTextField
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.HintText
import ac.sfj.miniappcompetition.ui.component.molecule.HintTone
import ac.sfj.miniappcompetition.ui.component.molecule.SegmentOption
import ac.sfj.miniappcompetition.ui.component.molecule.SegmentedSelector
import ac.sfj.miniappcompetition.ui.component.organism.FormBottomSheet
import ac.sfj.miniappcompetition.ui.component.organism.FormSheetContent
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionFormState
import ac.sfj.miniappcompetition.ui.shared.labelRes
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

/**
 * Sheet Buat dan Ubah kompetisi (Referensi UI hal. 7). Judul mengikuti [CompetitionFormState.isEdit].
 * Tombol Simpan aktif setelah nama valid dan jenis peserta dipilih.
 */
@Composable
internal fun CompetitionFormSheet(
    form: CompetitionFormState,
    onNameChange: (String) -> Unit,
    onParticipantTypeChange: (ParticipantType) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(form.titleRes()),
        confirmLabel = stringResource(R.string.action_save),
        onConfirm = onSave,
        onDismiss = onDismiss,
        confirmEnabled = form.canSave,
        confirmLoading = form.isSaving,
    ) {
        CompetitionFormFields(
            form = form,
            onNameChange = onNameChange,
            onParticipantTypeChange = onParticipantTypeChange,
        )
    }
}

/** Field nama dan jenis peserta, beserta keterangan kunci dan error simpan. */
@Composable
private fun CompetitionFormFields(
    form: CompetitionFormState,
    onNameChange: (String) -> Unit,
    onParticipantTypeChange: (ParticipantType) -> Unit,
) {
    AppTextField(
        value = form.name,
        onValueChange = onNameChange,
        label = stringResource(R.string.competition_name_label),
        modifier = Modifier.fillMaxWidth(),
        supportingText = stringResource(R.string.competition_name_required),
        errorText = if (form.nameError == NameError.TOO_LONG) {
            stringResource(R.string.competition_name_too_long, CompetitionRules.NAME_MAX_LENGTH)
        } else {
            null
        },
        maxLength = CompetitionRules.NAME_MAX_LENGTH,
        enabled = !form.isSaving,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
    )
    Spacer(Modifier.height(Dimens.SpaceLg))
    Text(text = stringResource(R.string.participant_type_label), style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(Dimens.SpaceSm))
    SegmentedSelector(
        options = ParticipantType.entries.map { type ->
            SegmentOption(value = type, label = stringResource(type.labelRes))
        },
        selected = form.participantType,
        onSelect = onParticipantTypeChange,
        enabled = !form.participantTypeLocked && !form.isSaving,
    )
    if (form.participantTypeLocked) {
        Spacer(Modifier.height(Dimens.SpaceSm))
        HintText(text = stringResource(R.string.participant_type_locked), icon = AppIcons.Lock)
    }
    val error = form.error
    if (error != null) {
        Spacer(Modifier.height(Dimens.SpaceLg))
        HintText(
            text = stringResource(error.toErrorMessage().textRes),
            icon = AppIcons.Error,
            tone = HintTone.Error,
        )
    }
}

private fun CompetitionFormState.titleRes(): Int =
    if (isEdit) R.string.competition_edit else R.string.competition_create

@ComponentPreviews
@Composable
private fun CompetitionFormSheetPreview() {
    val form = CompetitionFormState(
        competitionId = 1,
        name = "Turnamen Catur Antar Kelas",
        participantType = ParticipantType.INDIVIDUAL,
        originalParticipantType = ParticipantType.INDIVIDUAL,
        participantTypeLocked = true,
        error = AppError.Network,
    )
    PreviewSurface {
        FormSheetContent(
            title = stringResource(form.titleRes()),
            subtitle = null,
            confirmLabel = stringResource(R.string.action_save),
            onConfirm = {},
            dismissLabel = stringResource(R.string.action_cancel),
            onDismiss = {},
            confirmEnabled = form.canSave,
            confirmLoading = false,
        ) {
            CompetitionFormFields(form = form, onNameChange = {}, onParticipantTypeChange = {})
        }
    }
}
