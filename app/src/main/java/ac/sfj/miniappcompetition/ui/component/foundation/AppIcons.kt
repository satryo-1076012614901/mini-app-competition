package ac.sfj.miniappcompetition.ui.component.foundation

import ac.sfj.miniappcompetition.R
import androidx.annotation.DrawableRes

/**
 * Ikon Material Symbols Outlined (weight 400) yang dipakai aplikasi (Referensi UI hal. 6).
 *
 * Nilainya ID vector drawable di `res/drawable`; tampilkan dengan `painterResource(AppIcons.X)`.
 * Catatan: ikon `emoji_events` pada Referensi UI kini bernama `trophy` di Material Symbols.
 */
object AppIcons {
    @DrawableRes val AccountTree: Int = R.drawable.ic_account_tree
    @DrawableRes val Add: Int = R.drawable.ic_add
    @DrawableRes val ArrowBack: Int = R.drawable.ic_arrow_back
    @DrawableRes val Check: Int = R.drawable.ic_check

    /** Varian filled, penanda pemenang. */
    @DrawableRes val CheckCircle: Int = R.drawable.ic_check_circle
    @DrawableRes val Close: Int = R.drawable.ic_close
    @DrawableRes val Delete: Int = R.drawable.ic_delete

    /** Varian filled, ikon error pada field. */
    @DrawableRes val Error: Int = R.drawable.ic_error
    @DrawableRes val Groups: Int = R.drawable.ic_groups
    @DrawableRes val Info: Int = R.drawable.ic_info
    @DrawableRes val Lock: Int = R.drawable.ic_lock
    @DrawableRes val MoreVert: Int = R.drawable.ic_more_vert
    @DrawableRes val Person: Int = R.drawable.ic_person
    @DrawableRes val Remove: Int = R.drawable.ic_remove

    /** Lingkaran dengan tanda minus (Material Symbols `do_not_disturb_on`). */
    @DrawableRes val RemoveCircle: Int = R.drawable.ic_remove_circle
    @DrawableRes val RestartAlt: Int = R.drawable.ic_restart_alt
    @DrawableRes val Trophy: Int = R.drawable.ic_trophy
    @DrawableRes val Undo: Int = R.drawable.ic_undo
}
