package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.atom.AppIconButton
import ac.sfj.miniappcompetition.ui.component.atom.AppTextField
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.RemovableTextField
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Form panjang di dialog layar penuh (Referensi UI hal. 6: `DialogProperties
 * usePlatformDefaultWidth = false`), misalnya tambah/ubah peserta dengan anggota dinamis.
 * Isi dapat digulir dan tetap terlihat saat keyboard muncul.
 */
@Composable
fun FullScreenFormDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        FullScreenFormContent(
            title = title,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = modifier,
            confirmEnabled = confirmEnabled,
            confirmLoading = confirmLoading,
            content = content,
        )
    }
}

/** Isi [FullScreenFormDialog]; dipisah agar dapat di-preview tanpa jendela dialog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FullScreenFormContent(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    AppIconButton(
                        icon = AppIcons.Close,
                        contentDescription = stringResource(R.string.action_close),
                        onClick = onDismiss,
                        enabled = !confirmLoading,
                    )
                },
                actions = {
                    AppButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        variant = AppButtonVariant.Text,
                        enabled = confirmEnabled,
                        loading = confirmLoading,
                    )
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceSm),
                content = content,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun FullScreenFormContentPreview() {
    PreviewSurface {
        FullScreenFormContent(
            title = "Tambah tim",
            confirmLabel = "Simpan",
            onConfirm = {},
            onDismiss = {},
        ) {
            AppTextField(
                value = "Tim Garuda",
                onValueChange = {},
                label = "Nama tim",
                modifier = Modifier.fillMaxWidth(),
                errorText = "Nama ini sudah dipakai di kompetisi ini.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                RemovableTextField(
                    value = "Fajar Nugraha",
                    onValueChange = {},
                    label = "Anggota 1",
                    onRemove = {},
                    removeContentDescription = "Hapus Anggota 1",
                )
                RemovableTextField(
                    value = "",
                    onValueChange = {},
                    label = "Anggota 2",
                    onRemove = {},
                    removeContentDescription = "Hapus Anggota 2",
                    placeholder = "Nama anggota",
                )
            }
            AppButton(
                text = "Tambah anggota",
                onClick = {},
                variant = AppButtonVariant.Text,
                leadingIcon = AppIcons.Add,
            )
        }
    }
}
