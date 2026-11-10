package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlin.math.min

/** Radio, en metros, que entra en el mapa. */
private const val MAP_RADIUS_METERS = 900f

/**
 * Mapa esquemático centrado en la ubicación del pasajero.
 * Se dibuja con Compose para no depender de un proveedor de mapas ni de una API key.
 */
@Composable
fun NearbyStopsMap(
    userLocation: GeoPoint,
    stops: List<BusStop>,
    selectedStopId: String?,
    onStopClick: (BusStop) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val density = LocalDensity.current
    val userRadius = with(density) { 9.dp.toPx() }
    val stopRadius = with(density) { 7.dp.toPx() }
    val tapRadius = with(density) { 24.dp.toPx() }
    val gridStep = with(density) { 56.dp.toPx() }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val positions =
        remember(userLocation, stops) {
            stops.associate { stop -> stop.id to stop.location.projectedMetersFrom(userLocation) }
        }

    Canvas(
        modifier =
            modifier
                .background(backgroundColor)
                .onSizeChanged { canvasSize = it }
                .pointerInput(stops, canvasSize) {
                    detectTapGestures { tap ->
                        if (canvasSize.width == 0 || canvasSize.height == 0) return@detectTapGestures
                        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                        val scale = mapScale(canvasSize)
                        val tapped =
                            stops
                                .map { stop ->
                                    val (north, east) = positions.getValue(stop.id)
                                    stop to
                                        Offset(
                                            center.x + (east * scale).toFloat(),
                                            center.y - (north * scale).toFloat(),
                                        )
                                }.minByOrNull { (_, position) -> (position - tap).getDistance() }
                        val distance = tapped?.let { (_, position) -> (position - tap).getDistance() }
                        if (tapped != null && distance != null && distance <= tapRadius) {
                            onStopClick(tapped.first)
                        }
                    }
                },
    ) {
        var x = 0f
        while (x <= size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += gridStep
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += gridStep
        }

        val center = Offset(size.width / 2f, size.height / 2f)
        val scale = mapScale(IntSize(size.width.toInt(), size.height.toInt()))

        drawCircle(primary.copy(alpha = 0.15f), radius = userRadius * 4f, center = center)
        drawCircle(Color.White, radius = userRadius + 3.dp.toPx(), center = center)
        drawCircle(primary, radius = userRadius, center = center)

        stops.forEach { stop ->
            val (north, east) = positions.getValue(stop.id)
            val position =
                Offset(
                    center.x + (east * scale).toFloat(),
                    center.y - (north * scale).toFloat(),
                )
            val isSelected = stop.id == selectedStopId
            val radius = if (isSelected) stopRadius * 1.4f else stopRadius
            drawCircle(Color.White, radius = radius + 3.dp.toPx(), center = position)
            drawCircle(if (isSelected) primary else secondary, radius = radius, center = position)
        }
    }
}

private fun mapScale(size: IntSize): Float = min(size.width, size.height) / 2f / MAP_RADIUS_METERS
