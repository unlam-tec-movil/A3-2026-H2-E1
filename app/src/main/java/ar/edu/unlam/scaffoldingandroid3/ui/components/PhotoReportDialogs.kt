package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.AlertDialogBuilder
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.PhotoReportUiState

private val CATEGORIES =
    listOf(
        R.string.photo_report_category_obras,
        R.string.photo_report_category_hard_to_find,
        R.string.photo_report_category_other,
    )

@Composable
fun PhotoReportDialogs(
    state: PhotoReportUiState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit,
) {
    when (state) {
        PhotoReportUiState.Idle -> {
        }

        is PhotoReportUiState.PhotoTaken -> {
            val categoryNames = CATEGORIES.associateWith { stringResource(it) }
            var selectedCategory by remember { mutableIntStateOf(CATEGORIES.first()) }
            var customObservation by rememberSaveable { mutableStateOf("") }
            val isOtherCategory = selectedCategory == R.string.photo_report_category_other
            val unknownCategoryMessage = stringResource(R.string.photo_report_unknown_category)

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.photo_report_dialog_title)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(stringResource(R.string.photo_report_select_category))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            CATEGORIES.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(categoryNames.getValue(category)) },
                                )
                            }
                        }

                        if (isOtherCategory) {
                            OutlinedTextField(
                                value = customObservation,
                                onValueChange = { customObservation = it },
                                label = { Text(stringResource(R.string.photo_report_observation_label)) },
                                minLines = 3,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val observation =
                                getObservationText(
                                    selectedCategory = selectedCategory,
                                    categoryNames = categoryNames,
                                    customObservation = customObservation,
                                    unknownCategoryMessage = unknownCategoryMessage,
                                )

                            onConfirm(observation)
                        },
                        enabled = !isOtherCategory || customObservation.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.photo_report_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.photo_report_cancel))
                    }
                },
            )
        }

        PhotoReportUiState.PermissionDenied -> {
            AlertDialogBuilder(
                onDismiss,
                stringResource(R.string.photo_report_permission_denied_title),
                stringResource(R.string.photo_report_permission_denied_message),
                stringResource(R.string.photo_report_understood),
            ).Build()
        }

        PhotoReportUiState.Saved -> {
            AlertDialogBuilder(
                onAcknowledge,
                stringResource(R.string.photo_report_saved_title),
                stringResource(R.string.photo_report_saved_message),
                stringResource(R.string.photo_report_accept),
            ).Build()
        }
    }
}

private fun getObservationText(
    selectedCategory: Int,
    categoryNames: Map<Int, String>,
    customObservation: String,
    unknownCategoryMessage: String,
): String =
    when (selectedCategory) {
        in categoryNames -> {
            categoryNames.getValue(selectedCategory)
        }

        R.string.photo_report_category_other -> {
            customObservation.ifBlank { categoryNames.getValue(R.string.photo_report_category_other) }
        }

        else -> {
            throw IllegalStateException(unknownCategoryMessage)
        }
    }
