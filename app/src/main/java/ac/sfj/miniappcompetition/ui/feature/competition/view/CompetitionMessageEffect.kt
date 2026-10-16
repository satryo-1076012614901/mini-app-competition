package ac.sfj.miniappcompetition.ui.feature.competition.view

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionMessage
import ac.sfj.miniappcompetition.ui.shared.toErrorMessage
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource

/**
 * Menampilkan [message] sekali sebagai snackbar, lalu memanggil [onShown]. Error pemuatan yang dapat
 * dicoba ulang diberi aksi Coba lagi yang memanggil [onRetry] (Referensi UI hal. 15).
 */
@Composable
internal fun CompetitionMessageEffect(
    message: CompetitionMessage?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
    onRetry: () -> Unit,
) {
    if (message == null) return

    val text: String
    val actionLabel: String?
    when (message) {
        is CompetitionMessage.Failure -> {
            val errorMessage = message.error.toErrorMessage()
            val retryable = message.canRetry && errorMessage.retryable
            text = stringResource(errorMessage.textRes)
            actionLabel = if (retryable) stringResource(R.string.action_retry) else null
        }

        CompetitionMessage.Updated -> {
            text = stringResource(R.string.competition_updated)
            actionLabel = null
        }

        CompetitionMessage.Deleted -> {
            text = stringResource(R.string.competition_deleted)
            actionLabel = null
        }

        CompetitionMessage.NotFound -> {
            text = stringResource(R.string.error_not_found)
            actionLabel = null
        }
    }

    val currentOnShown by rememberUpdatedState(onShown)
    val currentOnRetry by rememberUpdatedState(onRetry)
    LaunchedEffect(message) {
        val result = snackbarHostState.showSnackbar(
            message = text,
            actionLabel = actionLabel,
            duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        currentOnShown()
        if (result == SnackbarResult.ActionPerformed) currentOnRetry()
    }
}
