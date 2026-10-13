package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ukuran standar [AppAvatar]. */
object AppAvatarDefaults {
    /** Avatar baris daftar. */
    val Size: Dp = 40.dp

    /** Avatar ilustrasi empty state. */
    val LargeSize: Dp = 80.dp
}

/** Avatar lingkaran berisi teks pendek, misalnya nomor urut peserta. */
@Composable
fun AppAvatar(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = AppAvatarDefaults.Size,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    AvatarContainer(
        modifier = modifier,
        size = size,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

/** Avatar lingkaran berisi ikon dekoratif, misalnya jenis peserta atau ilustrasi empty state. */
@Composable
fun AppAvatar(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    size: Dp = AppAvatarDefaults.Size,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    AvatarContainer(
        modifier = modifier,
        size = size,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
private fun AvatarContainer(
    modifier: Modifier,
    size: Dp,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@ComponentPreviews
@Composable
private fun AppAvatarPreview() {
    PreviewSurface {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(text = "1")
            AppAvatar(text = "16")
            AppAvatar(icon = AppIcons.Groups)
            AppAvatar(icon = AppIcons.Person)
            AppAvatar(
                icon = AppIcons.Trophy,
                size = AppAvatarDefaults.LargeSize,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
