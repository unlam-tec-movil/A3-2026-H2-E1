package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusRoute
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.StopStatus

class BusStopDataSourceImpl : BusStopDataSource {
    override suspend fun getBusStops(location: LocationData): List<BusStop> =
        listOf(
            BusStop(
                stopId = 1L,
                name = "Av. San Martín y Arieta",
                locationData =
                    LocationData(
                        latitude = Latitude(-34.6699),
                        longitude = Longitude(-58.5631),
                    ),
                markedAsFavorite = false,
                busLines =
                    listOf(
                        BusLine(
                            lineId = 101L,
                            number = 96,
                            routes =
                                listOf(
                                    BusRoute(
                                        routeId = 1001L,
                                        startingPoint =
                                            LocationData(
                                                Latitude(-34.6699),
                                                Longitude(-58.5631),
                                            ),
                                        destination =
                                            LocationData(
                                                Latitude(-34.6037),
                                                Longitude(-58.3816),
                                            ),
                                    ),
                                ),
                            currentStatus = BusLineStatus.NORMAL,
                        ),
                        BusLine(
                            lineId = 102L,
                            number = 620,
                            routes = emptyList(),
                            currentStatus = BusLineStatus.NORMAL,
                        ),
                    ),
                status = StopStatus.ACTIVA,
            ),
            BusStop(
                stopId = 2L,
                name = "Florencio Varela y Villegas",
                locationData =
                    LocationData(
                        latitude = Latitude(-34.6710),
                        longitude = Longitude(-58.5650),
                    ),
                markedAsFavorite = true,
                busLines =
                    listOf(
                        BusLine(
                            lineId = 103L,
                            number = 180,
                            routes = emptyList(),
                            currentStatus = BusLineStatus.DEMORADO,
                        ),
                    ),
                status = StopStatus.ACTIVA,
            ),
        )
}
