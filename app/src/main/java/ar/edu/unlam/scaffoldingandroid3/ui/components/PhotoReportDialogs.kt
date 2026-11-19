package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.PhotoReportUiState

@Composable
fun PhotoReportDialogs(
    state: PhotoReportUiState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit,
) {
    when (state) {
        PhotoReportUiState.Idle -> Unit

        is PhotoReportUiState.PhotoTaken -> {
            var observation by rememberSaveable { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Registrar estado de la parada") },
                text = {
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        label = { Text("Observación") },
                        minLines = 3,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(observation) },
                        enabled = observation.isNotBlank(),
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                },
            )
        }

        PhotoReportUiState.PermissionDenied -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Permiso de cámara rechazado") },
                text = { Text("Se necesita la cámara para registrar el estado de la parada.") },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Entendido")
                    }
                },
            )
        }

        PhotoReportUiState.Saved -> {
            AlertDialog(
                onDismissRequest = onAcknowledge,
                title = { Text("Reporte guardado") },
                text = { Text("La fotografía y la observación quedaron asociadas a la parada.") },
                confirmButton = {
                    TextButton(onClick = onAcknowledge) {
                        Text("Aceptar")
                    }
                },
            )
        }
    }
}
