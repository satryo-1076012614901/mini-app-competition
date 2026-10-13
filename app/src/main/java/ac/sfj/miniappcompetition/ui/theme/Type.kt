package ac.sfj.miniappcompetition.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

/**
 * Type scale Material 3 bawaan dengan Roboto (font sistem Android), sesuai Referensi UI hal. 5:
 * titleLarge 22 sp, bodyLarge 16 sp, bodyMedium 14 sp, labelLarge 14 sp, labelMedium 12 sp.
 */
val AppTypography = Typography()

/** Angka skor 22 sp dengan digit tabular agar skor antarbaris sejajar (Referensi UI hal. 5). */
val ScoreTextStyle: TextStyle = AppTypography.titleLarge.copy(fontFeatureSettings = "tnum")
