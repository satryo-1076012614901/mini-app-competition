package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Placeholder baris saat data dimuat pertama kali, agar tidak langsung tampil empty state
 * (Referensi UI hal. 8). Dibacakan TalkBack sebagai "Memuat".
 */
@Composable
fun LoadingPlaceholder(
    modifier: Modifier = Modifier,
    rowCount: Int = 5,
    showLeading: Boolean = true,
) {
    val description = stringResource(R.string.state_loading)
    val transition = rememberInfiniteTransition(label = "loadingPlaceholder")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loadingPlaceholderAlpha",
    )
    val shade = MaterialTheme.colorScheme.surfaceContainerHighest
    val barShape = MaterialTheme.shapes.extraSmall

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
            .graphicsLayer { alpha = pulseAlpha },
    ) {
        repeat(rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showLeading) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = shade, shape = CircleShape),
                    )
                    Spacer(Modifier.width(Dimens.SpaceLg))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .background(color = shade, shape = barShape),
                    )
                    Spacer(Modifier.height(Dimens.SpaceSm))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .background(color = shade, shape = barShape),
                    )
                }
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun LoadingPlaceholderPreview() {
    PreviewSurface {
        LoadingPlaceholder(rowCount = 3)
    }
}
