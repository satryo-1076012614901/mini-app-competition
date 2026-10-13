package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.foundation.dashedBorder
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Warna [StatusLabel] (Referensi UI hal. 5). */
enum class StatusTone {
    /** primaryContainer, misalnya Pendaftaran dan Siap. */
    Primary,

    /** tertiaryContainer, misalnya Berlangsung. */
    Tertiary,

    /** Netral, misalnya Selesai. */
    Neutral,

    /** Tanpa latar dengan garis tepi, misalnya Menunggu dan Bye. */
    Outline,
}

/** Penanda di depan teks [StatusLabel], agar status tidak hanya dibedakan lewat warna. */
enum class StatusLeading { None, Dot, Check }

/**
 * Label status non-interaktif berbasis `Surface` kecil, bukan chip, karena chip menyiratkan
 * dapat diketuk (Referensi UI hal. 6). Status selalu ditulis sebagai teks; warna hanya penguat.
 */
@Composable
fun StatusLabel(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    leading: StatusLeading = StatusLeading.None,
    dashed: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = when (tone) {
        StatusTone.Primary -> colors.primaryContainer
        StatusTone.Tertiary -> colors.tertiaryContainer
        StatusTone.Neutral -> colors.surfaceContainerHighest
        StatusTone.Outline -> Color.Transparent
    }
    val contentColor = when (tone) {
        StatusTone.Primary -> colors.onPrimaryContainer
        StatusTone.Tertiary -> colors.onTertiaryContainer
        StatusTone.Neutral, StatusTone.Outline -> colors.onSurfaceVariant
    }
    val shape = MaterialTheme.shapes.small
    val solidBorder = if (tone == StatusTone.Outline && !dashed) BorderStroke(1.dp, colors.outline) else null
    val dashedModifier = if (dashed) Modifier.dashedBorder(color = colors.outline, shape = shape) else Modifier

    Surface(
        modifier = modifier.then(dashedModifier),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = solidBorder,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (leading) {
                StatusLeading.Dot -> {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(color = contentColor, shape = CircleShape),
                    )
                    Spacer(Modifier.width(6.dp))
                }

                StatusLeading.Check -> {
                    Icon(
                        painter = painterResource(AppIcons.Check),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(Dimens.SpaceXs))
                }

                StatusLeading.None -> Unit
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@ComponentPreviews
@Composable
private fun StatusLabelPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                StatusLabel(text = "Pendaftaran", tone = StatusTone.Primary)
                StatusLabel(text = "Berlangsung", tone = StatusTone.Tertiary, leading = StatusLeading.Dot)
                StatusLabel(text = "Selesai", leading = StatusLeading.Check)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                StatusLabel(text = "Siap", tone = StatusTone.Primary)
                StatusLabel(text = "Menunggu", tone = StatusTone.Outline)
                StatusLabel(text = "Selesai", leading = StatusLeading.Check)
                StatusLabel(text = "Bye", tone = StatusTone.Outline, dashed = true)
            }
        }
    }
}
