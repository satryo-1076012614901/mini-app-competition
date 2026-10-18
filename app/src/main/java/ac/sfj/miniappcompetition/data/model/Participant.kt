package ac.sfj.miniappcompetition.data.model

import java.time.Instant

/**
 * Peserta kompetisi (`ParticipantResponse` di backend).
 *
 * [members] berisi nama anggota untuk kompetisi [ParticipantType.TEAM] dan selalu kosong untuk
 * kompetisi [ParticipantType.INDIVIDUAL]. Urutan pendaftaran ([createdAt] naik) menentukan
 * pasangan Babak 1 saat bracket dibuat.
 */
data class Participant(
    val id: Long,
    val competitionId: Long,
    val name: String,
    val members: List<String>,
    val createdAt: Instant,
)
