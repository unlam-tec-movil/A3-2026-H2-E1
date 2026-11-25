package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BusStopOverflowChip(
    expanded: Boolean,
    onToggle: () -> Unit,
    label: String,
    icon: ImageVector,
    iconDescription: String,
    modifier: Modifier,
) {
    AssistChip(
        onClick = onToggle,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                modifier = Modifier.size(16.dp),
            )
        },
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor =
                    if (expanded) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                labelColor =
                    if (expanded) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
            ),
        modifier = modifier,
    )
}
