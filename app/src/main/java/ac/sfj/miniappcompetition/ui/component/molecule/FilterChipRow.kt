package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Satu pilihan di [FilterChipRow]. */
data class FilterOption<T>(val value: T, val label: String)

/**
 * Deretan `FilterChip` yang dapat digeser horizontal, satu pilihan aktif
 * (filter status daftar kompetisi, Referensi UI hal. 7).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterChipRow(
    options: List<FilterOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.ScreenPadding),
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option.value) },
                label = { Text(text = option.label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = AppIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun FilterChipRowPreview() {
    PreviewSurface {
        FilterChipRow(
            options = listOf(
                FilterOption(value = 0, label = "Semua"),
                FilterOption(value = 1, label = "Pendaftaran"),
                FilterOption(value = 2, label = "Berlangsung"),
                FilterOption(value = 3, label = "Selesai"),
            ),
            selected = 0,
            onSelect = {},
            contentPadding = PaddingValues(),
        )
    }
}
