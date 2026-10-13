package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Satu segmen di [SegmentedSelector]. */
data class SegmentOption<T>(val value: T, val label: String)

/**
 * Pilihan tunggal berbasis `SingleChoiceSegmentedButtonRow` (Referensi UI hal. 6), misalnya jenis
 * peserta. [selected] boleh null agar belum ada segmen terpilih (pengguna memilih secara sadar).
 * Penjelasan saat terkunci ditampilkan pemanggil dengan [HintText].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SegmentedSelector(
    options: List<SegmentOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.value == selected,
                onClick = { onSelect(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                label = { Text(text = option.label) },
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun SegmentedSelectorPreview() {
    val options = listOf(
        SegmentOption(value = "INDIVIDUAL", label = "Individu"),
        SegmentOption(value = "TEAM", label = "Tim"),
    )
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            SegmentedSelector(options = options, selected = null, onSelect = {})
            SegmentedSelector(options = options, selected = "INDIVIDUAL", onSelect = {}, enabled = false)
        }
    }
}
