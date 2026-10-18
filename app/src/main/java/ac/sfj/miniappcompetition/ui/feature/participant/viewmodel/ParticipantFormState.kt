package ac.sfj.miniappcompetition.ui.feature.participant.viewmodel

import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.Participant
import ac.sfj.miniappcompetition.data.model.ParticipantType
import ac.sfj.miniappcompetition.data.rule.NameError
import ac.sfj.miniappcompetition.data.rule.ParticipantRules

data class ParticipantFormState(
    val participantId: Long? = null,
    val participantType: ParticipantType,
    val name: String = "",
    val members: List<String> = listOf(""),
    val isSaving: Boolean = false,
    val error: AppError? = null,
) {
    val isEdit: Boolean
        get() = participantId != null

    val nameError: NameError?
        get() = ParticipantRules.validateName(name)

    val canSave: Boolean
        get() = name.isNotBlank() && nameError == null && !isSaving

    companion object {
        fun create(participantType: ParticipantType) =
            ParticipantFormState(
                participantType = participantType,
                members = if (participantType == ParticipantType.TEAM) {
                    listOf("")
                } else {
                    emptyList()
                },
            )

        fun edit(
            participantType: ParticipantType,
            participant: Participant,
        ) = ParticipantFormState(
            participantId = participant.id,
            participantType = participantType,
            name = participant.name,
            members = if (participantType == ParticipantType.TEAM) {
                participant.members.ifEmpty { listOf("") }
            } else {
                emptyList()
            },
        )
    }
}