package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/** Tab utama berbasis `PrimaryTabRow`, misalnya Peserta dan Bracket (Referensi UI hal. 6). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                text = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun PrimaryTabsPreview() {
    PreviewSurface {
        PrimaryTabs(tabs = listOf("Peserta", "Bracket"), selectedIndex = 0, onSelect = {})
    }
}
