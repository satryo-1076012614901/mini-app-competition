package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * Satu opsi radio yang seluruh barisnya dapat diketuk (Referensi UI hal. 6), misalnya memilih
 * pemenang saat skor seri. Bungkus beberapa opsi dengan `Modifier.selectableGroup()`.
 */
@Composable
fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(Modifier.width(Dimens.SpaceLg))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@ComponentPreviews
@Composable
private fun RadioOptionRowPreview() {
    PreviewSurface {
        Column(modifier = Modifier.selectableGroup()) {
            RadioOptionRow(label = "Tim Kenari", selected = true, onClick = {})
            RadioOptionRow(label = "Tim Cendrawasih", selected = false, onClick = {})
        }
    }
}
