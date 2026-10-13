package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppIconButton
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.MenuAction
import ac.sfj.miniappcompetition.ui.component.molecule.OverflowMenu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

/**
 * App bar layar (Referensi UI hal. 6). Judul panjang dipotong satu baris.
 * Tombol kembali hanya tampil bila [onNavigateBack] diisi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = modifier,
        navigationIcon = {
            if (onNavigateBack != null) {
                AppIconButton(
                    icon = AppIcons.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onNavigateBack,
                )
            }
        },
        actions = actions,
    )
}

@ComponentPreviews
@Composable
private fun AppTopBarPreview() {
    PreviewSurface {
        Column {
            AppTopBar(title = "Kompetisi")
            AppTopBar(
                title = "Turnamen Catur Antar Kelas Semester Ganjil",
                onNavigateBack = {},
                actions = {
                    OverflowMenu(
                        actions = listOf(
                            MenuAction(label = "Ubah", onClick = {}),
                            MenuAction(label = "Hapus", onClick = {}, destructive = true),
                        ),
                    )
                },
            )
        }
    }
}
