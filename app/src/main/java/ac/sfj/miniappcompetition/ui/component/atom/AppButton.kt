package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Varian [AppButton]; masing-masing dipetakan ke tombol Material 3. */
enum class AppButtonVariant {
    /** Aksi utama layar, misalnya Simpan atau Buat bracket. */
    Filled,

    /** Aksi utama di dalam kartu, misalnya Input hasil. */
    Tonal,

    /** Aksi sekunder, misalnya Tambah tim. */
    Outlined,

    /** Aksi ringan, misalnya Batal, Urungkan, atau Lihat bracket. */
    Text,

    /** Aksi permanen di dialog, misalnya Hapus atau Reset (warna error). */
    Destructive,
}

/**
 * Tombol teks dengan ikon opsional dan state memuat.
 *
 * Saat [loading] bernilai true, tombol dinonaktifkan dan ikon diganti indikator progres
 * sehingga aksi tidak terkirim dua kali.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    enabled: Boolean = true,
    loading: Boolean = false,
    @DrawableRes leadingIcon: Int? = null,
) {
    val isEnabled = enabled && !loading
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        } else if (leadingIcon != null) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text = text)
    }

    when (variant) {
        AppButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = content,
        )

        AppButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = content,
        )

        AppButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = content,
        )

        AppButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = content,
        )

        AppButtonVariant.Destructive -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            content = content,
        )
    }
}

@ComponentPreviews
@Composable
private fun AppButtonPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            AppButton(text = "Simpan", onClick = {})
            AppButton(text = "Input hasil", onClick = {}, variant = AppButtonVariant.Tonal)
            AppButton(
                text = "Tambah tim",
                onClick = {},
                variant = AppButtonVariant.Outlined,
                leadingIcon = AppIcons.Add,
            )
            AppButton(
                text = "Urungkan",
                onClick = {},
                variant = AppButtonVariant.Text,
                leadingIcon = AppIcons.Undo,
            )
            AppButton(text = "Hapus", onClick = {}, variant = AppButtonVariant.Destructive)
            AppButton(text = "Menyimpan", onClick = {}, loading = true)
            AppButton(text = "Buat bracket", onClick = {}, enabled = false)
        }
    }
}
