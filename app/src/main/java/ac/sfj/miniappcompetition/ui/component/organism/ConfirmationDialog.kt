package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import androidx.annotation.DrawableRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

/**
 * Dialog konfirmasi berbasis `AlertDialog` (Referensi UI hal. 6), untuk hapus, buat bracket,
 * urungkan hasil, dan reset. Aksi permanen memakai [destructive] sehingga tombol konfirmasi
 * berwarna error (hal. 8). [text] sebaiknya menyebut akibat aksi, bukan sekadar pertanyaan.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    destructive: Boolean = false,
    confirmLoading: Boolean = false,
    dismissLabel: String = stringResource(R.string.action_cancel),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = if (destructive) AppButtonVariant.Destructive else AppButtonVariant.Text,
                loading = confirmLoading,
            )
        },
        modifier = modifier,
        dismissButton = {
            AppButton(
                text = dismissLabel,
                onClick = onDismiss,
                variant = AppButtonVariant.Text,
                enabled = !confirmLoading,
            )
        },
        icon = if (icon != null) {
            { Icon(painter = painterResource(icon), contentDescription = null) }
        } else {
            null
        },
        title = { Text(text = title) },
        text = { Text(text = text) },
    )
}

@ComponentPreviews
@Composable
private fun ConfirmationDialogPreview() {
    PreviewSurface {
        ConfirmationDialog(
            title = "Hapus kompetisi?",
            text = "Turnamen Catur Antar Kelas beserta seluruh pesertanya akan dihapus permanen.",
            confirmLabel = "Hapus",
            onConfirm = {},
            onDismiss = {},
            icon = AppIcons.Delete,
            destructive = true,
        )
    }
}
