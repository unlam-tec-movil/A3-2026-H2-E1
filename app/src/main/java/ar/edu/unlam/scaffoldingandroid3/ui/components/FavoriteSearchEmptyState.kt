package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteSearchEmptyState(onClearSearch: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.surface,
                contentColor = colors.onSurface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp),
            )

            Text(
                text = "No encontramos favoritas con esa búsqueda",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Probá con otro nombre o número de línea.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            TextButton(
                onClick = onClearSearch,
            ) {
                Text("Limpiar búsqueda")
            }
        }
    }
}
