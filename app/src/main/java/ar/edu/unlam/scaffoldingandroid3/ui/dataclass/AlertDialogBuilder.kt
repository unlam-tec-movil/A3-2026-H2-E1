package ar.edu.unlam.scaffoldingandroid3.ui.dataclass

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

data class AlertDialogBuilder(
    val onDismiss: () -> Unit,
    val title: String,
    val text: String,
    val buttonText: String,
    val onConfirm: (() -> Unit)? = null,
) {
    @Composable
    fun Build(
        onDismissRequest: () -> Unit = onDismiss,
        confirmButton: Boolean = true,
    ) = AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton =
            {
                if (confirmButton) {
                    TextButton(onClick = onConfirm ?: onDismiss) { Text(buttonText) }
                }
            },
    )
}
