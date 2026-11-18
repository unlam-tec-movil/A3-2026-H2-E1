package ar.edu.unlam.scaffoldingandroid3.domain

/** Filtra las paradas por nombre de parada, código de línea o destino. Sin texto, devuelve lista vacía. */
fun filterBusStops(
    stops: List<BusStop>,
    query: String,
): List<BusStop> {
    val text = query.trim()
    if (text.isEmpty()) return emptyList()
    return stops.filter { stop ->
        stop.name.contains(text, ignoreCase = true) ||
            stop.lines.any { line ->
                line.code.contains(text, ignoreCase = true) ||
                    line.destination.contains(text, ignoreCase = true)
            }
    }
}
