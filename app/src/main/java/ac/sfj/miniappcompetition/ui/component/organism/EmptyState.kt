package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.atom.AppAvatarDefaults
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

/**
 * State kosong berupa ajakan dengan satu aksi (Referensi UI hal. 8), misalnya
 * "Buat kompetisi pertama". Pemanggil menyembunyikan FAB selama state ini tampil.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceXxl, vertical = Dimens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppAvatar(
            icon = icon,
            size = AppAvatarDefaults.LargeSize,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.height(Dimens.SpaceXl))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpaceSm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Dimens.SpaceXl))
            AppButton(text = actionLabel, onClick = onAction, leadingIcon = actionIcon)
        }
    }
}

@ComponentPreviews
@Composable
private fun EmptyStatePreview() {
    PreviewSurface {
        EmptyState(
            title = "Buat kompetisi pertama",
            message = "Kompetisi menampung peserta, bracket, dan hasil pertandingan dalam satu tempat.",
            icon = AppIcons.Trophy,
            actionLabel = "Buat kompetisi",
            actionIcon = AppIcons.Add,
            onAction = {},
        )
    }
}
