package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

/**
 * Baris daftar berbasis `ListItem` (Referensi UI hal. 6), dipakai untuk baris kompetisi dan peserta.
 * Latar transparan agar mengikuti permukaan induknya. Baris dapat diketuk bila [onClick] diisi.
 */
@Composable
fun AppListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(text = headline, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        supportingContent = if (supporting != null) {
            { Text(text = supporting, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        } else {
            null
        },
        leadingContent = leading,
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@ComponentPreviews
@Composable
private fun AppListItemPreview() {
    PreviewSurface {
        Column {
            AppListItem(
                headline = "Futsal Cup Fakultas",
                supporting = "Tim, dibuat 6 Okt 2026",
                leading = { AppAvatar(icon = AppIcons.Groups) },
                trailing = {
                    StatusLabel(text = "Berlangsung", tone = StatusTone.Tertiary, leading = StatusLeading.Dot)
                },
                onClick = {},
            )
            AppListItem(
                headline = "Tim Garuda",
                supporting = "5 anggota",
                leading = { AppAvatar(text = "1") },
            )
        }
    }
}
