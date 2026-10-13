package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** Satu aksi di [BottomActionBar]. */
data class BarAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val icon: ImageVector? = null,
)

/**
 * Bar bawah berisi satu aksi utama dan satu aksi sekunder opsional, misalnya "Tambah tim" dan
 * "Buat bracket" (Referensi UI hal. 9). Padding navigation bar ditangani di dalam bar ini.
 */
@Composable
fun BottomActionBar(
    primary: BarAction,
    modifier: Modifier = Modifier,
    secondary: BarAction? = null,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            if (secondary != null) {
                AppButton(
                    text = secondary.label,
                    onClick = secondary.onClick,
                    modifier = Modifier.weight(1f),
                    variant = AppButtonVariant.Outlined,
                    enabled = secondary.enabled,
                    loading = secondary.loading,
                    leadingIcon = secondary.icon,
                )
            }
            AppButton(
                text = primary.label,
                onClick = primary.onClick,
                modifier = Modifier.weight(1f),
                variant = AppButtonVariant.Filled,
                enabled = primary.enabled,
                loading = primary.loading,
                leadingIcon = primary.icon,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun BottomActionBarPreview() {
    PreviewSurface {
        BottomActionBar(
            primary = BarAction(label = "Buat bracket", onClick = {}),
            secondary = BarAction(label = "Tambah tim", onClick = {}, icon = AppIcons.Add),
        )
    }
}
