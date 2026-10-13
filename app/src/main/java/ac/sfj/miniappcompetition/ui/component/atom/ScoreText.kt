package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.ScoreTextStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Angka skor dengan digit tabular (Referensi UI hal. 5). Skor kosong ditampilkan sebagai "—"
 * dan dibacakan TalkBack sebagai "Belum ada skor".
 *
 * [emphasized] menebalkan skor pemenang, sehingga pemenang tidak hanya dibedakan lewat warna.
 */
@Composable
fun ScoreText(
    score: Int?,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
) {
    val emptyDescription = stringResource(R.string.cd_score_empty)
    Text(
        text = score?.toString() ?: "—",
        modifier = if (score == null) {
            modifier.semantics { contentDescription = emptyDescription }
        } else {
            modifier
        },
        color = if (emphasized) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = textAlign,
        style = ScoreTextStyle,
    )
}

@ComponentPreviews
@Composable
private fun ScoreTextPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
            ScoreText(score = 3, emphasized = true)
            ScoreText(score = 1)
            ScoreText(score = null)
        }
    }
}
