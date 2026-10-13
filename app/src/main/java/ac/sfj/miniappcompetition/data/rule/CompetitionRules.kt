package ac.sfj.miniappcompetition.data.rule

import ac.sfj.miniappcompetition.data.model.CompetitionStatus

/**
 * Aksi kompetisi yang tersedia per status (Referensi UI hal. 3), diturunkan dari `CompetitionService`,
 * `ParticipantService`, dan `MatchService` backend. Tombol dan menu ditampilkan berdasarkan aturan
 * ini, bukan dicoba lalu menunggu ditolak backend.
 *
 * Aturan input dan urungkan hasil bergantung pada match, lihat `MatchResultRules`.
 */
object CompetitionRules {
    /** Batas panjang nama kompetisi (`@Size(max = 255)` pada `CompetitionRequest`). */
    const val NAME_MAX_LENGTH = 255

    /** Ubah kompetisi: hanya saat Pendaftaran. */
    fun canEdit(status: CompetitionStatus): Boolean = status == CompetitionStatus.OPEN

    /** Hapus kompetisi: hanya saat Pendaftaran. Kompetisi Selesai harus diurungkan dan direset dahulu. */
    fun canDelete(status: CompetitionStatus): Boolean = status == CompetitionStatus.OPEN

//    /** Tambah, ubah, dan hapus peserta: hanya saat Pendaftaran. */
//    fun canManageParticipants(status: CompetitionStatus): Boolean = status == CompetitionStatus.OPEN

//    /** Buat bracket: saat Pendaftaran dengan 2–16 peserta (`INVALID_PARTICIPANT_COUNT`). */
//    fun canGenerateBracket(status: CompetitionStatus, participantCount: Int): Boolean =
//        status == CompetitionStatus.OPEN && BracketShape.isSupported(participantCount)

//    /** Reset bracket: hanya saat Berlangsung (`COMPETITION_NOT_IN_MATCH`). */
//    fun canResetBracket(status: CompetitionStatus): Boolean = status == CompetitionStatus.IN_MATCH

    /** Jenis peserta terkunci setelah kompetisi memiliki peserta (`PARTICIPANT_TYPE_LOCKED`). */
    fun isParticipantTypeLocked(participantCount: Int): Boolean = participantCount > 0

    /** Nama yang dikirim ke backend: spasi di awal dan akhir dipangkas. */
    fun normalizeName(name: String): String = name.trim()

    /** Validasi nama kompetisi sebelum dikirim; `null` bila valid. */
    fun validateName(name: String): NameError? =
        validateNormalizedName(normalizeName(name), NAME_MAX_LENGTH)
}
