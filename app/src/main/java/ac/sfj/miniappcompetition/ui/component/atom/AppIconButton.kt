package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector

/** Varian [AppIconButton]. */
enum class AppIconButtonVariant {
    /** Ikon tanpa bingkai, misalnya di app bar. */
    Standard,

    /** Ikon dengan garis tepi lingkaran, misalnya stepper skor. */
    Outlined,
}

/**
 * Tombol ikon dengan [contentDescription] wajib, agar ikon tanpa label tetap terbaca TalkBack
 * (Referensi UI hal. 6). Target sentuh minimum 48 dp sudah dijamin Material 3.
 *
 * Biarkan [tint] tidak ditentukan agar warna mengikuti state enabled/disabled tombol.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppIconButtonVariant = AppIconButtonVariant.Standard,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    val iconContent: @Composable () -> Unit = {
        if (tint.isSpecified) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
        } else {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }

    when (variant) {
        AppIconButtonVariant.Standard -> IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = iconContent,
        )

        AppIconButtonVariant.Outlined -> OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = iconContent,
        )
    }
}

@ComponentPreviews
@Composable
private fun AppIconButtonPreview() {
    PreviewSurface {
        Row {
            AppIconButton(icon = AppIcons.ArrowBack, contentDescription = "Kembali", onClick = {})
            AppIconButton(icon = AppIcons.MoreVert, contentDescription = "Menu lainnya", onClick = {})
            AppIconButton(
                icon = AppIcons.Remove,
                contentDescription = "Kurangi skor",
                onClick = {},
                variant = AppIconButtonVariant.Outlined,
                enabled = false,
            )
            AppIconButton(
                icon = AppIcons.Add,
                contentDescription = "Tambah skor",
                onClick = {},
                variant = AppIconButtonVariant.Outlined,
            )
        }
    }
}
