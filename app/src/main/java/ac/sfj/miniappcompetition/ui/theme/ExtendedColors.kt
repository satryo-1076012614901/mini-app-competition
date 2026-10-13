package ac.sfj.miniappcompetition.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Warna tambahan di luar skema Material 3.
 *
 * Akses dari composable: `MaterialTheme.extendedColors.championContainer`.
 */
@Immutable
data class ExtendedColors(
    /** Aksen Juara, misalnya ikon piala. */
    val champion: Color,
    val onChampion: Color,
    /** Latar kartu Juara. */
    val championContainer: Color,
    /** Teks dan ikon di atas [championContainer]. */
    val onChampionContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    champion = championLight,
    onChampion = onChampionLight,
    championContainer = championContainerLight,
    onChampionContainer = onChampionContainerLight,
)

internal val DarkExtendedColors = ExtendedColors(
    champion = championDark,
    onChampion = onChampionDark,
    championContainer = championContainerDark,
    onChampionContainer = onChampionContainerDark,
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
