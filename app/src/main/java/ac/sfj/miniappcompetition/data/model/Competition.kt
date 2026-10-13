package ac.sfj.miniappcompetition.data.model

import java.time.Instant

/** Kompetisi single-elimination (`CompetitionResponse` di backend). */
data class Competition(
    val id: Long,
    val name: String,
    val participantType: ParticipantType,
    val status: CompetitionStatus,
    val createdAt: Instant,
)

/** Jenis peserta kompetisi; terkunci setelah kompetisi memiliki peserta. */
enum class ParticipantType {
    INDIVIDUAL,
    TEAM,
}

/**
 * Status kompetisi (Referensi UI hal. 3).
 *
 * - [OPEN]: Pendaftaran; kompetisi dan peserta masih dapat diubah.
 * - [IN_MATCH]: Berlangsung; bracket sudah dibuat dan hasil match dapat diinput.
 * - [COMPLETED]: Selesai; match final sudah memiliki pemenang.
 */
enum class CompetitionStatus {
    OPEN,
    IN_MATCH,
    COMPLETED,
}
