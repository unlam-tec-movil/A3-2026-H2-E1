package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine

@Composable
fun BusLineChip(
    line: BusLine,
    modifier: Modifier = Modifier,
) {
    val chipLabel = HELPER.buildLineLabel(line)

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = chipLabel,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        colors =
            SuggestionChipDefaults.suggestionChipColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                labelColor =
                    MaterialTheme.colorScheme.onSurface,
            ),
        modifier = modifier,
    )
}
