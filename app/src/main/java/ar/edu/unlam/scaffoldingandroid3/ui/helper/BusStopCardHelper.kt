package ar.edu.unlam.scaffoldingandroid3.ui.helper

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.ui.components.MAX_INITIAL_LINES
import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.ShareMessage
import java.util.Locale
import kotlin.math.roundToInt

class BusStopCardHelper {
    fun formatDistance(meters: Double): String =
        if (meters < 1_000) {
            "${meters.roundToInt()} m"
        } else {
            String.format(
                Locale.getDefault(),
                "%.1f km",
                meters / 1_000,
            )
        }

    fun getLinesToDisplay(
        stop: BusStop,
        expanded: Boolean,
        hasOverflow: Boolean,
    ): List<BusLine> =
        if (expanded || !hasOverflow) {
            stop.busLines
        } else {
            stop.busLines.take(MAX_INITIAL_LINES)
        }

    fun buildLineLabel(line: BusLine): String =
        buildString {
            append(line.number)
            if (line.branch.isNotBlank()) {
                append(" ${line.branch}")
            } else if (line.code.isNotBlank() && line.code != line.number.toString()) {
                val nonDigits = line.code.filter { !it.isDigit() }.trim()
                if (nonDigits.isNotBlank()) {
                    append(" $nonDigits")
                }
            }
            if (line.destination.isNotBlank()) {
                append(" → ${line.destination}")
            }
        }

    fun buildShareMessage(stop: BusStop): ShareMessage =
        ShareMessage(
            stopName = stop.name,
            linesText =
                if (stop.busLines.isEmpty()) {
                    "Sin líneas informadas"
                } else {
                    stop.busLines.joinToString(", ") { line ->
                        buildString {
                            append(line.number)
                            if (line.branch.isNotBlank()) append(" ${line.branch}")
                            if (line.destination.isNotBlank()) append(" → ${line.destination}")
                        }
                    }
                },
            mapsUrl = "https://maps.google.com/?q=${stop.locationData.latitude.value},${stop.locationData.longitude.value}",
        )
}
