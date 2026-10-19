package ac.sfj.miniappcompetition

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import ac.sfj.miniappcompetition.data.model.*
import ac.sfj.miniappcompetition.ui.feature.match.view.BracketContent
import ac.sfj.miniappcompetition.ui.feature.match.view.MatchResultFields
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.MatchResultForm
import ac.sfj.miniappcompetition.ui.component.organism.FormSheetContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ac.sfj.miniappcompetition.ui.feature.match.viewmodel.BracketUiState
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import java.io.File
import java.time.Instant

/** Exercises stateless Match content without touching the shared backend. */
class MatchContentTest {
    @get:Rule val compose = createComposeRule()
    private val people = (1L..6L).map { Participant(it, 1, "Tim ${listOf("Garuda", "Rajawali", "Elang", "Merpati", "Kenari", "Cendrawasih")[it.toInt()-1]}", emptyList(), Instant.EPOCH) }
    private val nodes = listOf(
        Match(1,1,1,1,1,2,1,4,3,1,MatchStatus.COMPLETED),
        Match(2,1,1,2,3,4,4,4,0,2,MatchStatus.COMPLETED),
        Match(3,1,1,3,5,6,null,5,null,null,MatchStatus.READY),
        Match(4,1,2,1,1,4,null,6,null,null,MatchStatus.READY),
        Match(5,1,2,2,null,null,null,6,null,null,MatchStatus.PENDING),
        Match(6,1,3,1,null,null,null,null,null,null,MatchStatus.PENDING))
    private fun state(round: Int = 1) = BracketUiState(
        competition = Competition(1, "Futsal Cup Fakultas", ParticipantType.TEAM, CompetitionStatus.IN_MATCH, Instant.EPOCH),
        participants = people, matches = nodes, selectedRound = round)
    private fun render(state: BracketUiState, dark: Boolean = false, fontScale: Float = 1f, onInput: (Long) -> Unit = {}) {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                MiniAppCompetitionTheme(darkTheme = dark) {
                    Surface {
                        BracketContent(state, PaddingValues(), {}, onInput, {}, {}, {})
                    }
                }
            }
        }
    }
    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
    @Test fun readyMatch_hasInputActionWithCorrectId() {
        var selected: Long? = null
        render(state(), onInput = { selected = it })
        compose.onNodeWithText("Input hasil").performScrollTo().performClick()
        assertEquals(3L, selected)
        screenshot("match-light")
    }
    @Test fun laterRoundBye_showsSourceAndHasNoUndo() {
        render(state(2), dark = true)
        compose.onNodeWithText("Bye").assertExists()
        compose.onNodeWithText("Pemenang Babak 1 Match 3").assertExists()
        compose.onAllNodesWithText("Urungkan").assertCountEquals(0)
        screenshot("match-dark-bye")
    }
    @Test fun champion_largeFont_showsWinnerAndUndoFinal() {
        val old = state(3)
        val final = nodes.last().copy(participant1Id = 1, participant2Id = 5, winnerId = 1,
            score1 = 2, score2 = 1, status = MatchStatus.COMPLETED)
        render(old.copy(competition = old.competition!!.copy(status = CompetitionStatus.COMPLETED),
            matches = nodes.dropLast(1) + final), fontScale = 2f)
        compose.onNodeWithText("Juara").assertIsDisplayed()
        compose.onNodeWithText("Menang 2–1 atas Tim Kenari di Final").assertExists()
        compose.onNodeWithText("Urungkan").performScrollTo().assertIsDisplayed()
        screenshot("match-champion-font200")
    }
    @Test fun emptyBracket_hasParticipantShortcut() {
        render(state().copy(matches = emptyList()))
        compose.onNodeWithText("Bracket belum dibuat").assertIsDisplayed()
        compose.onNodeWithText("Lihat peserta").assertHasClickAction()
    }
    @Test fun tiedResult_requiresSelectionAndClearRestoresEmptyScores() {
        compose.setContent {
            var form by remember { mutableStateOf(MatchResultForm(3, 1, 1)) }
            MiniAppCompetitionTheme {
                Surface {
                    FormSheetContent("Hasil Match 3", "Babak 1", "Simpan hasil", {}, "Batal", {}, form.canSave, false) {
                        MatchResultFields(form, nodes[2], people, true,
                            { slot, increment ->
                                val score = if (slot == MatchSlot.ONE) form.score1 else form.score2
                                form = form.withScore(slot, if (increment) (score ?: 0) + 1 else ((score ?: 0) - 1).coerceAtLeast(0))
                            }, { form = MatchResultForm(3) }, { form = form.copy(manualWinner = it) })
                    }
                }
            }
        }
        compose.onNodeWithText("Simpan hasil").assertIsNotEnabled()
        compose.onAllNodesWithText("Tim Kenari").onLast().performClick()
        compose.onNodeWithText("Simpan hasil").assertIsEnabled()
        screenshot("match-result-tied")
        compose.onNodeWithText("Kosongkan skor").performClick()
        compose.onNodeWithText("Simpan hasil").assertIsNotEnabled()
    }

    @Test fun longChampionAndOpponentNames_keepFinalUndoReachableAtLargeFont() {
        val longPeople = people.map { participant ->
            participant.copy(name = "${participant.name} Fakultas Teknik Informatika Universitas Ciputra Surabaya ".repeat(4).take(255))
        }
        val final = nodes.last().copy(participant1Id = 1, participant2Id = 5, winnerId = 1,
            score1 = 2, score2 = 1, status = MatchStatus.COMPLETED)
        val old = state(3)
        render(old.copy(competition = old.competition!!.copy(status = CompetitionStatus.COMPLETED),
            participants = longPeople, matches = nodes.dropLast(1) + final), fontScale = 2f)

        compose.onNodeWithText("Juara").assertIsDisplayed()
        compose.onNodeWithTag("bracket-match-list").performScrollToNode(hasText("Urungkan"))
        compose.onNodeWithText("Urungkan").assertIsDisplayed().assertIsEnabled()
        screenshot("match-long-names-font200")
        compose.onNodeWithTag("bracket-match-list").performScrollToIndex(1)
        compose.onNodeWithText("Final").assertIsDisplayed().assertHasClickAction()
    }

}
