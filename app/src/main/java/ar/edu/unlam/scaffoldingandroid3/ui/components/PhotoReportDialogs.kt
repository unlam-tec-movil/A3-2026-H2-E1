package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            var selectedCategory by rememberSaveable { mutableStateOf("Obras") }
            var customObservation by rememberSaveable { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Registrar estado de la parada") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Seleccioná una categoría:")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val isObrasSelected = selectedCategory == "Obras"
                            FilterChip(
                                selected = isObrasSelected,
                                onClick = { selectedCategory = "Obras" },
                                label = { Text("Obras") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isObrasSelected) Icons.Filled.Check else Icons.Filled.Construction,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            val isHardSelected = selectedCategory == "Difíciles de encontrar"
                            FilterChip(
                                selected = isHardSelected,
                                onClick = { selectedCategory = "Difíciles de encontrar" },
                                label = { Text("Difíciles de encontrar") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isHardSelected) Icons.Filled.Check else Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            val isOtherSelected = selectedCategory == "Otro"
                            FilterChip(
                                selected = isOtherSelected,
                                onClick = { selectedCategory = "Otro" },
                                label = { Text("Otro") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isOtherSelected) Icons.Filled.Check else Icons.Filled.MoreHoriz,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                        }

                        if (selectedCategory == "Otro") {
                            OutlinedTextField(
                                value = customObservation,
                                onValueChange = { customObservation = it },
                                label = { Text("Observación detallada") },
                                minLines = 3,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val observation =
                                if (selectedCategory == "Otro") {
                                    customObservation.ifBlank { "Otro" }
                                } else {
                                    selectedCategory
                                }
                            onConfirm(observation)
                        },
                        enabled = selectedCategory != "Otro" || customObservation.isNotBlank(),
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
