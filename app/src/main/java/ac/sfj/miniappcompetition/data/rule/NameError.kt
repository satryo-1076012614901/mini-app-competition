package ac.sfj.miniappcompetition.data.rule

/** Kesalahan isian nama kompetisi atau peserta. */
enum class NameError {
    /** Kosong setelah spasi di awal dan akhir dipangkas. */
    BLANK,

    /** Melebihi batas panjang setelah dipangkas. */
    TOO_LONG,
}

/** Validasi nama yang sudah dinormalisasi; `null` bila valid. */
internal fun validateNormalizedName(name: String, maxLength: Int): NameError? = when {
    name.isEmpty() -> NameError.BLANK
    name.length > maxLength -> NameError.TOO_LONG
    else -> null
}
