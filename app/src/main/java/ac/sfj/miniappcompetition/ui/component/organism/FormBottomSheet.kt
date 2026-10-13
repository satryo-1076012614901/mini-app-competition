package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.atom.AppTextField
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.HintText
import ac.sfj.miniappcompetition.ui.component.molecule.SegmentOption
import ac.sfj.miniappcompetition.ui.component.molecule.SegmentedSelector
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

/**
 * Form singkat di `ModalBottomSheet` (Referensi UI hal. 6), misalnya buat/ubah kompetisi dan
 * input hasil. Tombol Batal menutup sheet dengan animasi lalu memanggil [onDismiss].
 * Saat [confirmLoading], tombol konfirmasi menampilkan progres dan tidak dapat diketuk.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBottomSheet(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    dismissLabel: String = stringResource(R.string.action_cancel),
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hideThenDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
    ) {
        FormSheetContent(
            title = title,
            subtitle = subtitle,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            dismissLabel = dismissLabel,
            onDismiss = hideThenDismiss,
            confirmEnabled = confirmEnabled,
            confirmLoading = confirmLoading,
            content = content,
        )
    }
}

/** Isi [FormBottomSheet]; dipisah agar dapat di-preview tanpa jendela sheet. */
@Composable
internal fun FormSheetContent(
    title: String,
    subtitle: String?,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean,
    confirmLoading: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = Dimens.SpaceXl, end = Dimens.SpaceXl, bottom = Dimens.SpaceXl),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Dimens.SpaceLg))
        content()
        Spacer(Modifier.height(Dimens.SpaceXl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm, Alignment.End),
        ) {
            AppButton(
                text = dismissLabel,
                onClick = onDismiss,
                variant = AppButtonVariant.Text,
                enabled = !confirmLoading,
            )
            AppButton(
                text = confirmLabel,
                onClick = onConfirm,
                enabled = confirmEnabled,
                loading = confirmLoading,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun FormSheetContentPreview() {
    PreviewSurface {
        FormSheetContent(
            title = "Ubah kompetisi",
            subtitle = null,
            confirmLabel = "Simpan",
            onConfirm = {},
            dismissLabel = "Batal",
            onDismiss = {},
            confirmEnabled = true,
            confirmLoading = false,
        ) {
            AppTextField(
                value = "Turnamen Catur Antar Kelas",
                onValueChange = {},
                label = "Nama kompetisi",
                modifier = Modifier.fillMaxWidth(),
                supportingText = "Wajib diisi",
                maxLength = 255,
            )
            Spacer(Modifier.height(Dimens.SpaceLg))
            Text(text = "Jenis peserta", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Dimens.SpaceSm))
            SegmentedSelector(
                options = listOf(
                    SegmentOption(value = "INDIVIDUAL", label = "Individu"),
                    SegmentOption(value = "TEAM", label = "Tim"),
                ),
                selected = "INDIVIDUAL",
                onSelect = {},
                enabled = false,
            )
            Spacer(Modifier.height(Dimens.SpaceSm))
            HintText(text = "Terkunci karena kompetisi sudah memiliki peserta", icon = AppIcons.Lock)
        }
    }
}
