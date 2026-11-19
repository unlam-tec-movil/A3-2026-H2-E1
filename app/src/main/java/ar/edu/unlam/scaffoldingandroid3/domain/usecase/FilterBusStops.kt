package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import javax.inject.Inject

class FilterBusStops
    @Inject
    constructor() {
        operator fun invoke(
            stops: List<BusStop>,
            query: String,
        ): List<BusStop> {
            val text = query.trim()
            if (text.isEmpty()) return stops
            return stops.filter { stop ->
                stop.name.contains(text, ignoreCase = true) ||
                    stop.busLines.any { line -> line.number.toString().contains(text, ignoreCase = true) }
            }
        }
    }
