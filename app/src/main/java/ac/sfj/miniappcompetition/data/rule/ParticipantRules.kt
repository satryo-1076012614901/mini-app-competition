//package ac.sfj.miniappcompetition.data.rule

//import ac.sfj.miniappcompetition.data.model.ParticipantType

///**
// * Aturan isian peserta (Referensi UI hal. 9). Ketersediaan aksi peserta per status kompetisi ada di
// * [CompetitionRules.canManageParticipants].
// */
//object ParticipantRules {
//    /** Batas panjang nama peserta: kolom `participant.name` bertipe `VARCHAR(255)`. */
//    const val NAME_MAX_LENGTH = 255

//    /** Nama yang dikirim ke backend: spasi di awal dan akhir dipangkas, sama dengan backend. */
//    fun normalizeName(name: String): String = name.trim()

//    /** Validasi nama peserta sebelum dikirim; `null` bila valid. */
//    fun validateName(name: String): NameError? =
//        validateNormalizedName(normalizeName(name), NAME_MAX_LENGTH)

//    /**
//     * Nama anggota yang dikirim: setiap nama dipangkas dan field kosong dibuang, karena backend
//     * menolak nama anggota kosong sebagai `VALIDATION_ERROR` tanpa keterangan field.
//     */
//    fun normalizeMembers(members: List<String>): List<String> =
//        members.map { it.trim() }.filter { it.isNotEmpty() }

//    /**
//     * Nilai `members` untuk request: `null` untuk kompetisi Individu (backend menolak dengan
//     * `MEMBERS_NOT_ALLOWED` bila diisi) dan daftar anggota yang sudah dinormalisasi untuk kompetisi Tim.
//     */
//    fun membersForRequest(participantType: ParticipantType, members: List<String>): List<String>? =
//        when (participantType) {
//            ParticipantType.INDIVIDUAL -> null
//            ParticipantType.TEAM -> normalizeMembers(members)
//        }
//}
