package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Penghitung kecil berbentuk pil, misalnya "2/3" di samping label tab babak (Referensi UI hal. 11).
 * [highlighted] dipakai untuk tab yang sedang aktif.
 */
@Composable
fun CountPill(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (highlighted) colors.primaryContainer else colors.surfaceContainerHighest,
        contentColor = if (highlighted) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@ComponentPreviews
@Composable
private fun CountPillPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            CountPill(text = "2/3", highlighted = true)
            CountPill(text = "0/1")
        }
    }
}
