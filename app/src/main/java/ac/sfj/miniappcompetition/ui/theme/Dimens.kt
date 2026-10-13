package ac.sfj.miniappcompetition.ui.theme

import androidx.compose.ui.unit.dp

/** Token ukuran bersama; komponen memakai nilai ini alih-alih angka dp langsung. */
object Dimens {
    /** Skala spacing berbasis grid 4 dp. */
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 12.dp
    val SpaceLg = 16.dp
    val SpaceXl = 24.dp
    val SpaceXxl = 32.dp

    /** Gutter horizontal layar. */
    val ScreenPadding = 16.dp

    /** Target sentuh minimum, termasuk tombol stepper skor dan ikon app bar (Referensi UI hal. 6). */
    val MinTouchTarget = 48.dp
}
