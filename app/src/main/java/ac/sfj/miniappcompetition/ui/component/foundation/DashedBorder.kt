package ac.sfj.miniappcompetition.ui.component.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Garis tepi putus-putus mengikuti [shape], untuk kartu dan label bye (Referensi UI hal. 3 dan 5).
 * Garis digambar di dalam batas komponen sehingga tidak terpotong.
 */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    width: Dp = 1.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp,
): Modifier = drawWithContent {
    drawContent()
    val strokePx = width.toPx()
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()))
    inset(strokePx / 2f) {
        drawOutline(
            outline = shape.createOutline(size, layoutDirection, this),
            color = color,
            style = Stroke(width = strokePx, pathEffect = dashEffect),
        )
    }
}
