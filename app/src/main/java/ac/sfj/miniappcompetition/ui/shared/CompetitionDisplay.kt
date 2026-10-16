package ac.sfj.miniappcompetition.ui.shared

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLabel
import ac.sfj.miniappcompetition.ui.component.molecule.StatusLeading
import ac.sfj.miniappcompetition.ui.component.molecule.StatusTone
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

/** Teks status kompetisi: Pendaftaran, Berlangsung, atau Selesai (Referensi UI hal. 7). */
@get:StringRes
val CompetitionStatus.labelRes: Int
    get() = when (this) {
        CompetitionStatus.OPEN -> R.string.competition_status_open
        CompetitionStatus.IN_MATCH -> R.string.competition_status_in_match
        CompetitionStatus.COMPLETED -> R.string.competition_status_completed
    }

/**
 * Label status kompetisi pada daftar dan header detail (Referensi UI hal. 3 dan 5). Berlangsung diberi
 * titik dan Selesai diberi centang, agar status tidak hanya dibedakan lewat warna.
 */
@Composable
fun CompetitionStatusLabel(status: CompetitionStatus, modifier: Modifier = Modifier) {
    val (tone, leading) = when (status) {
        CompetitionStatus.OPEN -> StatusTone.Primary to StatusLeading.None
        CompetitionStatus.IN_MATCH -> StatusTone.Tertiary to StatusLeading.Dot
        CompetitionStatus.COMPLETED -> StatusTone.Neutral to StatusLeading.Check
    }
    StatusLabel(
        text = stringResource(status.labelRes),
        modifier = modifier,
        tone = tone,
        leading = leading,
    )
}

/** Teks jenis peserta: Individu atau Tim (Referensi UI hal. 7). */
@get:StringRes
val ParticipantType.labelRes: Int
    get() = when (this) {
        ParticipantType.INDIVIDUAL -> R.string.participant_type_individual
        ParticipantType.TEAM -> R.string.participant_type_team
    }

/** Ikon jenis peserta: `person` untuk Individu dan `groups` untuk Tim (Referensi UI hal. 6). */
val ParticipantType.icon: ImageVector
    get() = when (this) {
        ParticipantType.INDIVIDUAL -> AppIcons.Person
        ParticipantType.TEAM -> AppIcons.Groups
    }

@ComponentPreviews
@Composable
private fun CompetitionStatusLabelPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            CompetitionStatus.entries.forEach { status ->
                CompetitionStatusLabel(status = status)
            }
        }
    }
}
