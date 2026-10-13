package ac.sfj.miniappcompetition.ui.component.foundation

import ac.sfj.miniappcompetition.ui.theme.Dimens
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview standar untuk komponen: tema terang, tema gelap, dan ukuran font 200%
 * (Referensi UI hal. 6: teks harus tetap terbaca saat ukuran font sistem diperbesar).
 */
@Preview(name = "Terang")
@Preview(name = "Gelap", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(name = "Font 200%", fontScale = 2f)
annotation class ComponentPreviews

/** Pembungkus preview: memasang theme aplikasi dan latar sesuai mode terang atau gelap. */
@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    MiniAppCompetitionTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(Dimens.SpaceLg)) {
                content()
            }
        }
    }
}
