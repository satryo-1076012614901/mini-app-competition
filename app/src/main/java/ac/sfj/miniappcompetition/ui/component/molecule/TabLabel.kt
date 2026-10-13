package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.atom.CountPill
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Label tab dengan penghitung opsional, misalnya "Babak 1 [2/3]" (Referensi UI hal. 11).
 * [highlighted] menandai tab aktif.
 */
@Composable
fun TabLabel(
    text: String,
    modifier: Modifier = Modifier,
    count: String? = null,
    highlighted: Boolean = false,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, maxLines = 1)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            CountPill(text = count, highlighted = highlighted)
        }
    }
}

@ComponentPreviews
@Composable
private fun TabLabelPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
            TabLabel(text = "Babak 1", count = "2/3", highlighted = true)
            TabLabel(text = "Semifinal", count = "0/1")
            TabLabel(text = "Peserta")
        }
    }
}
