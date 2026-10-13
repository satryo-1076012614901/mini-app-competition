package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppIconButton
import ac.sfj.miniappcompetition.ui.component.atom.AppIconButtonVariant
import ac.sfj.miniappcompetition.ui.component.atom.ScoreText
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Baris label + stepper skor (−, nilai, +) untuk input hasil (Referensi UI hal. 11).
 *
 * Komponen ini tidak menyimpan state; aturan nilai ditentukan pemanggil. Secara default tombol
 * kurang nonaktif saat nilai kosong atau 0, sehingga skor tidak pernah di bawah 0.
 */
@Composable
fun ScoreStepper(
    label: String,
    value: Int?,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    decrementEnabled: Boolean = value != null && value > 0,
    incrementEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AppIconButton(
            icon = AppIcons.Remove,
            contentDescription = stringResource(R.string.cd_score_decrease, label),
            onClick = onDecrement,
            variant = AppIconButtonVariant.Outlined,
            enabled = decrementEnabled,
        )
        ScoreText(
            score = value,
            modifier = Modifier
                .widthIn(min = 40.dp)
                .padding(horizontal = Dimens.SpaceXs),
            emphasized = true,
            textAlign = TextAlign.Center,
        )
        AppIconButton(
            icon = AppIcons.Add,
            contentDescription = stringResource(R.string.cd_score_increase, label),
            onClick = onIncrement,
            variant = AppIconButtonVariant.Outlined,
            enabled = incrementEnabled,
        )
    }
}

@ComponentPreviews
@Composable
private fun ScoreStepperPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            ScoreStepper(label = "Tim Kenari", value = 1, onDecrement = {}, onIncrement = {})
            ScoreStepper(label = "Tim Cendrawasih", value = null, onDecrement = {}, onIncrement = {})
        }
    }
}
