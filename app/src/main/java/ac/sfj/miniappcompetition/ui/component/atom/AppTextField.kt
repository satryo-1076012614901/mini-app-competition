package ac.sfj.miniappcompetition.ui.component.atom

import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Field input satu baris berbasis `OutlinedTextField` (Referensi UI hal. 6).
 *
 * - [errorText] mengaktifkan state error dan ikon error, serta menggantikan [supportingText].
 * - [maxLength] hanya menampilkan penghitung karakter; validasi panjang dilakukan form pemanggil.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isError = errorText != null
    val helperText = errorText ?: supportingText

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(text = label) },
        placeholder = if (placeholder != null) {
            { Text(text = placeholder) }
        } else {
            null
        },
        trailingIcon = if (isError) {
            { Icon(imageVector = AppIcons.Error, contentDescription = null) }
        } else {
            null
        },
        supportingText = if (helperText != null || maxLength != null) {
            {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = helperText.orEmpty(), modifier = Modifier.weight(1f))
                    if (maxLength != null) {
                        Text(text = "${value.length}/$maxLength")
                    }
                }
            }
        } else {
            null
        },
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
    )
}

@ComponentPreviews
@Composable
private fun AppTextFieldPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            AppTextField(
                value = "Turnamen Catur Antar Kelas",
                onValueChange = {},
                label = "Nama kompetisi",
                supportingText = "Wajib diisi",
                maxLength = 255,
            )
            AppTextField(
                value = "Tim Garuda",
                onValueChange = {},
                label = "Nama tim",
                errorText = "Nama ini sudah dipakai di kompetisi ini.",
            )
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Anggota 3",
                placeholder = "Nama anggota",
            )
        }
    }
}
