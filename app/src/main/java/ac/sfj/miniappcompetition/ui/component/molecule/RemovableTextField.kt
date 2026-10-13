package ac.sfj.miniappcompetition.ui.component.molecule

import ac.sfj.miniappcompetition.ui.component.atom.AppIconButton
import ac.sfj.miniappcompetition.ui.component.atom.AppTextField
import ac.sfj.miniappcompetition.ui.component.foundation.AppIcons
import ac.sfj.miniappcompetition.ui.component.foundation.ComponentPreviews
import ac.sfj.miniappcompetition.ui.component.foundation.PreviewSurface
import ac.sfj.miniappcompetition.ui.theme.Dimens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Field input dengan tombol hapus di sampingnya, untuk daftar anggota tim yang dinamis
 * (Referensi UI hal. 9). [removeContentDescription] wajib karena tombolnya hanya berupa ikon.
 */
@Composable
fun RemovableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onRemove: () -> Unit,
    removeContentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorText: String? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier.weight(1f),
            placeholder = placeholder,
            errorText = errorText,
        )
        // Menyejajarkan tombol 48 dp dengan bagian tengah field, bukan dengan teks bantuannya.
        AppIconButton(
            icon = AppIcons.RemoveCircle,
            contentDescription = removeContentDescription,
            onClick = onRemove,
            modifier = Modifier.padding(start = Dimens.SpaceXs, top = 12.dp),
        )
    }
}

@ComponentPreviews
@Composable
private fun RemovableTextFieldPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            RemovableTextField(
                value = "Fajar Nugraha",
                onValueChange = {},
                label = "Anggota 1",
                onRemove = {},
                removeContentDescription = "Hapus Anggota 1",
            )
            RemovableTextField(
                value = "",
                onValueChange = {},
                label = "Anggota 2",
                onRemove = {},
                removeContentDescription = "Hapus Anggota 2",
                placeholder = "Nama anggota",
            )
        }
    }
}
