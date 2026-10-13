package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppIconButton
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/** Satu aksi di [OverflowMenu]. [destructive] menampilkan teks berwarna error, misalnya Hapus. */
data class MenuAction(
    val label: String,
    val onClick: () -> Unit,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * Tombol `more_vert` yang membuka menu aksi (Referensi UI hal. 8 dan 9).
 * Bila [actions] kosong, tombol tidak ditampilkan, sesuai aturan "disembunyikan, bukan dinonaktifkan".
 */
@Composable
fun OverflowMenu(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.action_more_options),
) {
    if (actions.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        AppIconButton(
            icon = AppIcons.MoreVert,
            contentDescription = contentDescription,
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(text = action.label) },
                    onClick = {
                        expanded = false
                        action.onClick()
                    },
                    enabled = action.enabled,
                    colors = if (action.destructive) {
                        MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                    } else {
                        MenuDefaults.itemColors()
                    },
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun OverflowMenuPreview() {
    PreviewSurface {
        OverflowMenu(
            actions = listOf(
                MenuAction(label = "Ubah", onClick = {}),
                MenuAction(label = "Hapus", onClick = {}, destructive = true),
            ),
        )
    }
}
