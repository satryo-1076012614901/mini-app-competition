package ac.sfj.miniappcompetition.ui.component.organism

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.ui.component.atom.AppAvatar
import ac.sfj.miniappcompetition.ui.component.atom.AppButton
import ac.sfj.miniappcompetition.ui.component.atom.AppButtonVariant
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

/**
 * State gagal memuat dengan tombol coba lagi, dibedakan dari empty state (Referensi UI hal. 8).
 * [message] menyebut apa yang terjadi lalu apa yang perlu dilakukan (hal. 15).
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.action_retry),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceXxl, vertical = Dimens.SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppAvatar(
            icon = AppIcons.Error,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(Modifier.height(Dimens.SpaceLg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpaceLg))
        AppButton(text = retryLabel, onClick = onRetry, variant = AppButtonVariant.Outlined)
    }
}

@ComponentPreviews
@Composable
private fun ErrorStatePreview() {
    PreviewSurface {
        ErrorState(
            message = "Daftar kompetisi tidak dapat dimuat. Periksa koneksi, lalu coba lagi.",
            onRetry = {},
        )
    }
}
