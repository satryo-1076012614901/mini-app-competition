package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Warna [HintText]. */
enum class HintTone { Neutral, Error }

/**
 * Teks keterangan kecil dengan ikon opsional, misalnya "Terkunci karena kompetisi sudah memiliki
 * peserta" (Referensi UI hal. 7) atau "Skor seri. Pilih pemenang secara manual." (hal. 11).
 */
@Composable
fun HintText(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: HintTone = HintTone.Neutral,
) {
    val color = when (tone) {
        HintTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        HintTone.Error -> MaterialTheme.colorScheme.error
    }
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color,
            )
            Spacer(Modifier.width(Dimens.SpaceSm))
        }
        Text(text = text, color = color, style = MaterialTheme.typography.bodySmall)
    }
}

@ComponentPreviews
@Composable
private fun HintTextPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            HintText(text = "Terkunci karena kompetisi sudah memiliki peserta", icon = AppIcons.Lock)
            HintText(
                text = "Skor seri. Pilih pemenang secara manual.",
                icon = AppIcons.Info,
                tone = HintTone.Error,
            )
        }
    }
}
