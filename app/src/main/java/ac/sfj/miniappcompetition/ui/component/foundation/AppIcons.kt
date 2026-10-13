package ac.sfj.miniappcompetition.ui.component.foundation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Ikon Material Symbols Outlined (weight 400) yang dipakai aplikasi (Referensi UI hal. 6),
 * dibangun sebagai [ImageVector] di Kotlin, tanpa vector drawable XML dan tanpa library ikon.
 *
 * Tampilkan dengan `Icon(imageVector = AppIcons.X, contentDescription = ...)`.
 * Catatan: ikon `emoji_events` pada Referensi UI kini bernama `trophy` di Material Symbols.
 */
object AppIcons {
    val AccountTree: ImageVector by lazy { materialSymbol("AccountTree", MaterialSymbolPaths.AccountTree) }
    val Add: ImageVector by lazy { materialSymbol("Add", MaterialSymbolPaths.Add) }
    val ArrowBack: ImageVector by lazy {
        materialSymbol("ArrowBack", MaterialSymbolPaths.ArrowBack, autoMirror = true)
    }
    val Check: ImageVector by lazy { materialSymbol("Check", MaterialSymbolPaths.Check) }

    /** Varian filled, penanda pemenang. */
    val CheckCircle: ImageVector by lazy { materialSymbol("CheckCircle", MaterialSymbolPaths.CheckCircle) }
    val Close: ImageVector by lazy { materialSymbol("Close", MaterialSymbolPaths.Close) }
    val Delete: ImageVector by lazy { materialSymbol("Delete", MaterialSymbolPaths.Delete) }

    /** Varian filled, ikon error pada field. */
    val Error: ImageVector by lazy { materialSymbol("Error", MaterialSymbolPaths.Error) }
    val Groups: ImageVector by lazy { materialSymbol("Groups", MaterialSymbolPaths.Groups) }
    val Info: ImageVector by lazy { materialSymbol("Info", MaterialSymbolPaths.Info) }
    val Lock: ImageVector by lazy { materialSymbol("Lock", MaterialSymbolPaths.Lock) }
    val MoreVert: ImageVector by lazy { materialSymbol("MoreVert", MaterialSymbolPaths.MoreVert) }
    val Person: ImageVector by lazy { materialSymbol("Person", MaterialSymbolPaths.Person) }
    val Remove: ImageVector by lazy { materialSymbol("Remove", MaterialSymbolPaths.Remove) }

    /** Lingkaran dengan tanda minus (Material Symbols `do_not_disturb_on`). */
    val RemoveCircle: ImageVector by lazy { materialSymbol("RemoveCircle", MaterialSymbolPaths.RemoveCircle) }
    val RestartAlt: ImageVector by lazy { materialSymbol("RestartAlt", MaterialSymbolPaths.RestartAlt) }
    val Trophy: ImageVector by lazy { materialSymbol("Trophy", MaterialSymbolPaths.Trophy) }
    val Undo: ImageVector by lazy { materialSymbol("Undo", MaterialSymbolPaths.Undo, autoMirror = true) }
}
