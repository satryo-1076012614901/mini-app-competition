package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.TabLabel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Satu tab di [ScrollableTabs]; [count] tampil sebagai pil penghitung, misalnya "2/3". */
data class TabItem(val label: String, val count: String? = null)

/**
 * Tab sekunder yang dapat digeser, misalnya tab per babak (Referensi UI hal. 6 dan 11).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollableTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        edgePadding = 0.dp,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                text = { TabLabel(text = tab.label, count = tab.count, highlighted = selected) },
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun ScrollableTabsPreview() {
    PreviewSurface {
        ScrollableTabs(
            tabs = listOf(
                TabItem(label = "Babak 1", count = "2/3"),
                TabItem(label = "Semifinal", count = "0/1"),
                TabItem(label = "Final", count = "0/1"),
            ),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}
