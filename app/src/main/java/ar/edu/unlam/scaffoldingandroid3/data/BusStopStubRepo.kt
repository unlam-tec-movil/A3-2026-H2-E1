package ar.edu.unlam.scaffoldingandroid3.data

import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint

/**
 * Datos de demostración hasta que exista un backend de paradas.
 * Las paradas se generan alrededor del punto recibido para poder probar el escenario en cualquier lugar.
 */
class BusStopStubRepo : BusStopRepository {
    override suspend fun getNearbyStops(
        center: GeoPoint,
        radiusMeters: Double,
    ): List<BusStop> =
        DEMO_STOPS
            .map { demo -> demo.toBusStop(center) }
            .map { stop -> stop to center.distanceTo(stop.location) }
            .filter { (_, distance) -> distance <= radiusMeters }
            .sortedBy { (_, distance) -> distance }
            .map { (stop, _) -> stop }

    private fun DemoStop.toBusStop(center: GeoPoint): BusStop =
        BusStop(
            id = name,
            name = name,
            lines = lines,
            location = center.offset(northMeters = northMeters, eastMeters = eastMeters),
        )

    private data class DemoStop(
        val name: String,
        val lines: List<BusLine>,
        val northMeters: Double,
        val eastMeters: Double,
    )

    private companion object {
        val DEMO_STOPS =
            listOf(
                DemoStop(
                    name = "Av. Perón y Arieta",
                    lines =
                        listOf(
                            BusLine(code = "96", destination = "Plaza Once"),
                            BusLine(code = "185", destination = "Liniers"),
                        ),
                    northMeters = 90.0,
                    eastMeters = 40.0,
                ),
                DemoStop(
                    name = "Av. Perón y Salguero",
                    lines =
                        listOf(
                            BusLine(code = "205", destination = "La Matanza"),
                            BusLine(code = "242", destination = "San Justo"),
                        ),
                    northMeters = 210.0,
                    eastMeters = -120.0,
                ),
                DemoStop(
                    name = "Almafuerte y Arieta",
                    lines =
                        listOf(
                            BusLine(code = "406", destination = "Ramos Mejía"),
                            BusLine(code = "620", destination = "La Tablada"),
                        ),
                    northMeters = -150.0,
                    eastMeters = 190.0,
                ),
                DemoStop(
                    name = "Av. San Martín y Belgrano",
                    lines =
                        listOf(
                            BusLine(code = "143", destination = "Retiro"),
                            BusLine(code = "96", destination = "Plaza Once"),
                        ),
                    northMeters = -320.0,
                    eastMeters = -80.0,
                ),
                DemoStop(
                    name = "Av. Illia y Tucumán",
                    lines =
                        listOf(
                            BusLine(code = "306", destination = "Morón"),
                            BusLine(code = "185", destination = "Liniers"),
                        ),
                    northMeters = 420.0,
                    eastMeters = 260.0,
                ),
                DemoStop(
                    name = "Ruta 3 y Zapiola",
                    lines =
                        listOf(
                            BusLine(code = "205", destination = "La Matanza"),
                            BusLine(code = "620", destination = "La Tablada"),
                        ),
                    northMeters = -520.0,
                    eastMeters = 380.0,
                ),
            )
    }
}
