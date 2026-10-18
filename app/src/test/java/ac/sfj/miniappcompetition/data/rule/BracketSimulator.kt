package ac.sfj.miniappcompetition.data.rule

import ac.sfj.miniappcompetition.data.model.CompetitionStatus
import ac.sfj.miniappcompetition.data.model.Match
import ac.sfj.miniappcompetition.data.model.MatchSlot
import ac.sfj.miniappcompetition.data.model.MatchStatus

/**
 * Simulasi bracket untuk unit test: port `BracketPlanner` serta alur submit dan undo `MatchService`
 * dari backend `afl3-miniapp` (commit 33efa16). Aturan client dibandingkan dengan simulasi ini
 * untuk semua jumlah peserta 2–16. Bila logika backend berubah, port ini perlu diperbarui.
 */
class BracketSimulator(participantCount: Int, private val competitionId: Long = 1L) {
    /** ID peserta urut pendaftaran. */
    val participantIds: List<Long> = (1..participantCount).map { 100L + it }

    var competitionStatus: CompetitionStatus = CompetitionStatus.IN_MATCH
        private set

    private val nodes: List<Node>

    /** ID match yang dibuat sebagai bye oleh planner (pasangan berisi satu peserta). */
    val plannedByeIds: Set<Long>

    init {
        val drafts = plan(participantIds)
        val idByPosition = drafts.mapIndexed { index, draft ->
            (draft.round to draft.matchNumber) to (competitionId * 1000 + index + 1)
        }.toMap()
        nodes = drafts.map { draft ->
            Node(
                id = idByPosition.getValue(draft.round to draft.matchNumber),
                round = draft.round,
                matchNumber = draft.matchNumber,
                participant1Id = draft.participant1Id,
                participant2Id = draft.participant2Id,
                winnerId = draft.winnerId,
                nextMatchId = draft.next?.let { idByPosition.getValue(it) },
                status = draft.status,
            )
        }
        plannedByeIds = drafts.filter { it.isBye }.map { idByPosition.getValue(it.round to it.matchNumber) }.toSet()
    }

    /** Snapshot seperti respons `GET …/matches`. */
    fun matches(): List<Match> = nodes.map { it.toMatch(competitionId) }

    fun match(round: Int, matchNumber: Int): Match =
        matches().single { it.round == round && it.matchNumber == matchNumber }

    /** Port `MatchService.submitResult`. */
    fun submit(matchId: Long, winnerSlot: MatchSlot, score1: Int? = null, score2: Int? = null) {
        check(competitionStatus == CompetitionStatus.IN_MATCH) { "COMPETITION_NOT_IN_MATCH" }
        val match = node(matchId)
        check(match.status == MatchStatus.READY) { "MATCH_NOT_READY" }
        val winnerId = checkNotNull(match.slot(winnerSlot)) { "INVALID_WINNER" }
        match.winnerId = winnerId
        match.score1 = score1
        match.score2 = score2
        match.status = MatchStatus.COMPLETED
        advanceWinner(match, winnerId)
    }

    /** Prasyarat `MatchService.undoResult` (tanpa melempar `MATCH_UNDO_NOT_ALLOWED`). */
    fun undoAllowed(matchId: Long): Boolean {
        val match = node(matchId)
        val isAutomaticBye = listOf(match.participant1Id, match.participant2Id).count { it != null } == 1
        if (match.status != MatchStatus.COMPLETED || isAutomaticBye) return false
        val nextMatchId = match.nextMatchId ?: return true
        val target = node(nextMatchId)
        val sources = sourcesOf(nextMatchId)
        return if (sources.size == 1) {
            val downstream = target.nextMatchId?.let(::node)
            downstream == null || downstream.status != MatchStatus.COMPLETED
        } else {
            target.status != MatchStatus.COMPLETED
        }
    }

    /** Port `MatchService.undoResult`. */
    fun undo(matchId: Long) {
        check(undoAllowed(matchId)) { "MATCH_UNDO_NOT_ALLOWED" }
        val match = node(matchId)
        if (match.nextMatchId == null) {
            competitionStatus = CompetitionStatus.IN_MATCH
        } else {
            clearProgression(match)
        }
        match.winnerId = null
        match.score1 = null
        match.score2 = null
        match.status = MatchStatus.READY
    }

    private fun advanceWinner(source: Node, winnerId: Long) {
        val nextMatchId = source.nextMatchId
        if (nextMatchId == null) {
            competitionStatus = CompetitionStatus.COMPLETED
            return
        }
        val target = node(nextMatchId)
        val sources = sourcesOf(nextMatchId)
        target.setSlot(sources.indexOfSource(source), winnerId)
        if (sources.size == 1) {
            target.winnerId = winnerId
            target.status = MatchStatus.COMPLETED
            advanceWinner(target, winnerId)
        } else if (target.participant1Id != null && target.participant2Id != null) {
            target.status = MatchStatus.READY
        }
    }

    private fun clearProgression(source: Node) {
        val target = node(checkNotNull(source.nextMatchId))
        val sources = sourcesOf(target.id)
        if (sources.size == 1) {
            target.nextMatchId?.let { downstreamId ->
                val downstream = node(downstreamId)
                check(downstream.status != MatchStatus.COMPLETED) { "MATCH_UNDO_NOT_ALLOWED" }
                downstream.setSlot(sourcesOf(downstreamId).indexOfSource(target), null)
                downstream.status = MatchStatus.PENDING
            }
            target.setSlot(sources.indexOfSource(source), null)
            target.winnerId = null
            target.status = MatchStatus.PENDING
        } else {
            check(target.status != MatchStatus.COMPLETED) { "MATCH_UNDO_NOT_ALLOWED" }
            target.setSlot(sources.indexOfSource(source), null)
            target.status = MatchStatus.PENDING
        }
    }

    private fun node(id: Long): Node = nodes.single { it.id == id }

    private fun sourcesOf(targetId: Long): List<Node> =
        nodes.filter { it.nextMatchId == targetId }.sortedBy { it.matchNumber }

    private fun List<Node>.indexOfSource(source: Node): Int {
        val index = indexOfFirst { it.id == source.id }
        check(index in 0..1) { "Match source is not linked to its target" }
        return index
    }

    private class Node(
        val id: Long,
        val round: Int,
        val matchNumber: Int,
        var participant1Id: Long?,
        var participant2Id: Long?,
        var winnerId: Long?,
        val nextMatchId: Long?,
        var status: MatchStatus,
        var score1: Int? = null,
        var score2: Int? = null,
    ) {
        fun slot(slot: MatchSlot): Long? = if (slot == MatchSlot.ONE) participant1Id else participant2Id

        fun setSlot(index: Int, participantId: Long?) {
            if (index == 0) participant1Id = participantId else participant2Id = participantId
        }

        fun toMatch(competitionId: Long) = Match(
            id = id,
            competitionId = competitionId,
            round = round,
            matchNumber = matchNumber,
            participant1Id = participant1Id,
            participant2Id = participant2Id,
            winnerId = winnerId,
            nextMatchId = nextMatchId,
            score1 = score1,
            score2 = score2,
            status = status,
        )
    }

    private class Entrant(
        val participantId: Long?,
        val source: Draft? = null,
        val receivedBye: Boolean = false,
    )

    private class Draft(
        val round: Int,
        val matchNumber: Int,
        val participant1Id: Long?,
        val participant2Id: Long?,
        val winnerId: Long?,
        val status: MatchStatus,
        val isBye: Boolean,
    ) {
        var next: Pair<Int, Int>? = null
    }

    private companion object {
        /** Port `BracketPlanner.create`. */
        fun plan(participantIds: List<Long>): List<Draft> {
            val drafts = mutableListOf<Draft>()
            var entrants = participantIds.map { Entrant(participantId = it) }
            var round = 1
            while (entrants.size > 1) {
                val arranged = arrangeBye(entrants)
                val roundMatches = arranged.chunked(2).mapIndexed { index, pair ->
                    val automaticWinner = pair.singleOrNull()?.participantId
                    val draft = Draft(
                        round = round,
                        matchNumber = index + 1,
                        participant1Id = pair[0].participantId,
                        participant2Id = pair.getOrNull(1)?.participantId,
                        winnerId = automaticWinner,
                        status = when {
                            automaticWinner != null -> MatchStatus.COMPLETED
                            pair.size == 2 && pair.all { it.participantId != null } -> MatchStatus.READY
                            else -> MatchStatus.PENDING
                        },
                        isBye = pair.size == 1,
                    )
                    pair.forEach { entrant -> entrant.source?.next = round to index + 1 }
                    draft
                }
                drafts += roundMatches
                entrants = roundMatches.map { Entrant(participantId = it.winnerId, source = it, receivedBye = it.isBye) }
                round += 1
            }
            return drafts
        }

        /** Port `BracketPlanner.arrangeBye`. */
        fun arrangeBye(entrants: List<Entrant>): List<Entrant> {
            if (entrants.size % 2 == 0 || !entrants.last().receivedBye) return entrants
            val replacementIndex = (entrants.lastIndex - 1 downTo 0).first { !entrants[it].receivedBye }
            return entrants.toMutableList().apply {
                val previousLast = this[lastIndex]
                this[lastIndex] = this[replacementIndex]
                this[replacementIndex] = previousLast
            }
        }
    }
}
