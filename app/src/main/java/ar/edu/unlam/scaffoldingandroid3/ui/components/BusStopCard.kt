package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import java.util.Locale
import kotlin.math.roundToInt

/** Tarjeta de una parada cercana: nombre, distancia y líneas que pasan por ella. */
@Composable
fun BusStopCard(
    stop: BusStop,
    distanceMeters: Double,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            if (selected) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatDistance(distanceMeters),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            stop.lines.forEach { line ->
                Text(
                    text = "${line.code} → ${line.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun formatDistance(meters: Double): String =
    if (meters < 1_000) {
        "${meters.roundToInt()} m"
    } else {
        String.format(Locale.getDefault(), "%.1f km", meters / 1_000)
    }
