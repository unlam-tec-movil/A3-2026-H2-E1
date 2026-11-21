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
                busLines = lines.toBusLines(),
                status = StopStatus.ACTIVA,
            )

        private fun List<DemoLine>.toBusLines(): List<BusLine> =
            map { demoLine ->
                BusLine(
                    lineId = demoLine.number.toLong() * 1000 + (demoLine.branch.hashCode().toLong() and 0x7FFFFFFF) % 1000,
                    number = demoLine.number,
                    routes = emptyList(),
                    currentStatus = BusLineStatus.NORMAL,
                    code = if (demoLine.branch.isNotBlank()) "${demoLine.number}${demoLine.branch}" else demoLine.number.toString(),
                    branch = demoLine.branch,
                    destination = demoLine.destination,
                )
            }

        private data class DemoStop(
            val stopId: Long,
            val name: String,
            val lines: List<DemoLine>,
            val northMeters: Double,
            val eastMeters: Double,
        )

        private data class DemoLine(
            val number: Int,
            val branch: String = "",
            val destination: String = "",
        )

        private companion object {
            val DEMO_STOPS =
                listOf(
                    DemoStop(
                        stopId = 1L,
                        name = "Av. Perón y Arieta",
                        lines =
                            listOf(
                                DemoLine(number = 96, branch = "A", destination = "Plaza Once"),
                                DemoLine(number = 185, branch = "B", destination = "Liniers"),
                            ),
                        northMeters = 90.0,
                        eastMeters = 40.0,
                    ),
                    DemoStop(
                        stopId = 2L,
                        name = "Av. Perón y Salguero",
                        lines =
                            listOf(
                                DemoLine(number = 205, branch = "1", destination = "La Matanza"),
                                DemoLine(number = 242, branch = "Directo", destination = "San Justo"),
                            ),
                        northMeters = 210.0,
                        eastMeters = -120.0,
                    ),
                    DemoStop(
                        stopId = 3L,
                        name = "Almafuerte y Arieta",
                        lines =
                            listOf(
                                DemoLine(number = 406, branch = "2", destination = "Ramos Mejía"),
                                DemoLine(number = 620, branch = "C", destination = "La Tablada"),
                            ),
                        northMeters = -150.0,
                        eastMeters = 190.0,
                    ),
                    DemoStop(
                        stopId = 4L,
                        name = "Av. San Martín y Belgrano",
                        lines =
                            listOf(
                                DemoLine(number = 143, branch = "A", destination = "Retiro"),
                                DemoLine(number = 96, branch = "B", destination = "Plaza Once"),
                            ),
                        northMeters = -320.0,
                        eastMeters = -80.0,
                    ),
                    DemoStop(
                        stopId = 5L,
                        name = "Av. Illia y Tucumán",
                        lines =
                            listOf(
                                DemoLine(number = 306, branch = "3", destination = "Morón"),
                                DemoLine(number = 185, branch = "A", destination = "Liniers"),
                            ),
                        northMeters = 420.0,
                        eastMeters = 260.0,
                    ),
                    DemoStop(
                        stopId = 6L,
                        name = "Ruta 3 y Zapiola",
                        lines =
                            listOf(
                                DemoLine(number = 205, branch = "2", destination = "La Matanza"),
                                DemoLine(number = 620, branch = "A", destination = "La Tablada"),
                            ),
                        northMeters = -520.0,
                        eastMeters = 380.0,
                    ),
                )
        }
    }
