package ar.edu.unlam.scaffoldingandroid3.data.datasources.demo

import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.model.offsetByMeters
import javax.inject.Inject

class DemoBusStopDataSourceImpl
    @Inject
    constructor() : DemoBusStopDataSource {
        override suspend fun getDemoStops(
            center: LocationData,
            radiusMeters: Double,
        ): List<BusStop> =
            DEMO_STOPS
                .asSequence()
                .map { demo -> demo.toBusStop(center) }
                .map { stop -> stop to center.distanceMetersTo(stop.locationData) }
                .filter { (_, distance) -> distance <= radiusMeters }
                .sortedBy { (_, distance) -> distance }
                .map { (stop, _) -> stop }
                .toList()

        private fun DemoStop.toBusStop(center: LocationData): BusStop =
            BusStop(
                stopId = stopId,
                name = name,
                locationData = center.offsetByMeters(northMeters = northMeters, eastMeters = eastMeters),
                markedAsFavorite = false,
                busLines = lineNumbers.toBusLines(),
                status = StopStatus.ACTIVA,
            )

        private fun List<Int>.toBusLines(): List<BusLine> =
            map { number ->
                BusLine(
                    lineId = number.toLong(),
                    number = number,
                    routes = emptyList(),
                    currentStatus = BusLineStatus.NORMAL,
                )
            }

        private data class DemoStop(
            val stopId: Long,
            val name: String,
            val lineNumbers: List<Int>,
            val northMeters: Double,
            val eastMeters: Double,
        )

        private companion object {
            val DEMO_STOPS =
                listOf(
                    DemoStop(
                        stopId = 1L,
                        name = "Av. Perón y Arieta",
                        lineNumbers = listOf(96, 185),
                        northMeters = 90.0,
                        eastMeters = 40.0,
                    ),
                    DemoStop(
                        stopId = 2L,
                        name = "Av. Perón y Salguero",
                        lineNumbers = listOf(205, 242),
                        northMeters = 210.0,
                        eastMeters = -120.0,
                    ),
                    DemoStop(
                        stopId = 3L,
                        name = "Almafuerte y Arieta",
                        lineNumbers = listOf(406, 620),
                        northMeters = -150.0,
                        eastMeters = 190.0,
                    ),
                    DemoStop(
                        stopId = 4L,
                        name = "Av. San Martín y Belgrano",
                        lineNumbers = listOf(143, 96),
                        northMeters = -320.0,
                        eastMeters = -80.0,
                    ),
                    DemoStop(
                        stopId = 5L,
                        name = "Av. Illia y Tucumán",
                        lineNumbers = listOf(306, 185),
                        northMeters = 420.0,
                        eastMeters = 260.0,
                    ),
                    DemoStop(
                        stopId = 6L,
                        name = "Ruta 3 y Zapiola",
                        lineNumbers = listOf(205, 620),
                        northMeters = -520.0,
                        eastMeters = 380.0,
                    ),
                )
        }
    }
