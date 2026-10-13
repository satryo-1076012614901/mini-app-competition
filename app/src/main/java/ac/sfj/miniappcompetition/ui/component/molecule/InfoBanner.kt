package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Warna [InfoBanner]. */
enum class BannerTone {
    /** secondaryContainer, kartu info (Referensi UI hal. 5), misalnya ringkasan bracket. */
    Info,

    /** Latar netral, misalnya penjelasan peserta terkunci. */
    Neutral,
}

/**
 * Kotak keterangan dengan ikon dan aksi opsional, misalnya ringkasan bracket (Referensi UI hal. 9)
 * atau banner peserta terkunci dengan pintasan "Lihat bracket" (hal. 10).
 */
@Composable
fun InfoBanner(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val hasAction = actionLabel != null && onAction != null
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (tone == BannerTone.Info) colors.secondaryContainer else colors.surfaceContainerHigh,
        contentColor = if (tone == BannerTone.Info) colors.onSecondaryContainer else colors.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(
                start = Dimens.SpaceLg,
                top = Dimens.SpaceMd,
                end = Dimens.SpaceLg,
                bottom = if (hasAction) Dimens.SpaceXs else Dimens.SpaceMd,
            ),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Dimens.SpaceMd))
                Text(
                    text = text,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (actionLabel != null && onAction != null) {
                AppButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.align(Alignment.End),
                    variant = AppButtonVariant.Text,
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun InfoBannerPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            InfoBanner(
                text = "6 tim: 3 babak, 5 pertandingan, 1 bye di Semifinal. Pasangan Babak 1 mengikuti urutan daftar.",
                icon = AppIcons.Info,
            )
            InfoBanner(
                text = "Peserta terkunci karena bracket sudah dibuat. Reset bracket dari menu untuk mengubah peserta.",
                icon = AppIcons.Lock,
                tone = BannerTone.Neutral,
                actionLabel = "Lihat bracket",
                onAction = {},
            )
        }
    }
}
