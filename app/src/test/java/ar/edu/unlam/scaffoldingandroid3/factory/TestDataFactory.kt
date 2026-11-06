package ar.edu.unlam.scaffoldingandroid3.factory

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusRoute
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.StopStatus

class TestDataFactory {
    companion object DataFactory {
        const val MAIN_LATITUDE = -34.6037
        const val MAIN_LONGITUDE = -58.3816

        const val CENTRO_LATITUDE = -34.6040
        const val CENTRO_LONGITUDE = -58.3820

        const val UNIVERSIDAD_LATITUDE = -34.6050
        const val UNIVERSIDAD_LONGITUDE = -58.3780

        const val ROUTE1_DESTINATION_LATITUDE = -34.6050
        const val ROUTE1_DESTINATION_LONGITUDE = -58.3750

        const val ROUTE2_ORIGIN_LATITUDE = -34.6100
        const val ROUTE2_ORIGIN_LONGITUDE = -58.3800
        const val ROUTE2_DESTINATION_LATITUDE = -34.5900
        const val ROUTE2_DESTINATION_LONGITUDE = -58.3900

        const val STOP_ID_FIRST = 1L
        const val STOP_ID_SECOND = 2L
        const val STOP_ID_THIRD = 3L

        const val LINE_ID_FIRST = 1L
        const val LINE_ID_SECOND = 2L

        const val BUS_LINE_NUMBER_FIRST = 10
        const val BUS_LINE_NUMBER_SECOND = 20

        @JvmStatic
        fun locationDataInstance() =
            LocationData(
                Latitude(MAIN_LATITUDE),
                Longitude(MAIN_LONGITUDE),
            )

        @JvmStatic
        fun anotherLocationDataInstance() = LocationData(Latitude(CENTRO_LATITUDE), Longitude(CENTRO_LONGITUDE))

        @JvmStatic
        fun busStopList(): List<BusStop> {
            val busRoute1 =
                BusRoute(
                    routeId = 1L,
                    startingPoint =
                        LocationData(
                            Latitude(MAIN_LATITUDE),
                            Longitude(MAIN_LONGITUDE),
                        ),
                    destination =
                        LocationData(
                            Latitude(ROUTE1_DESTINATION_LATITUDE),
                            Longitude(ROUTE1_DESTINATION_LONGITUDE),
                        ),
                )

            val busRoute2 =
                BusRoute(
                    routeId = 2L,
                    startingPoint =
                        LocationData(
                            Latitude(ROUTE2_ORIGIN_LATITUDE),
                            Longitude(ROUTE2_ORIGIN_LONGITUDE),
                        ),
                    destination =
                        LocationData(
                            Latitude(ROUTE2_DESTINATION_LATITUDE),
                            Longitude(ROUTE2_DESTINATION_LONGITUDE),
                        ),
                )

            val busLine1 =
                BusLine(
                    lineId = LINE_ID_FIRST,
                    number = BUS_LINE_NUMBER_FIRST,
                    routes = listOf(busRoute1),
                    currentStatus = BusLineStatus.NORMAL,
                )

            val busLine2 =
                BusLine(
                    lineId = LINE_ID_SECOND,
                    number = BUS_LINE_NUMBER_SECOND,
                    routes = listOf(busRoute2),
                    currentStatus = BusLineStatus.NORMAL,
                )

            val locationData = LocationData(Latitude(MAIN_LATITUDE), Longitude(MAIN_LONGITUDE))

            return listOf(
                BusStop(
                    stopId = STOP_ID_FIRST,
                    name = "Parada Plaza",
                    locationData = locationData,
                    markedAsFavorite = false,
                    busLines = listOf(busLine1),
                    status = StopStatus.ACTIVA,
                ),
                BusStop(
                    stopId = STOP_ID_SECOND,
                    name = "Parada Centro",
                    locationData =
                        LocationData(
                            Latitude(CENTRO_LATITUDE),
                            Longitude(CENTRO_LONGITUDE),
                        ),
                    markedAsFavorite = true,
                    busLines = listOf(busLine1, busLine2),
                    status = StopStatus.ACTIVA,
                ),
                BusStop(
                    stopId = STOP_ID_THIRD,
                    name = "Parada Universidad",
                    locationData =
                        LocationData(
                            Latitude(UNIVERSIDAD_LATITUDE),
                            Longitude(UNIVERSIDAD_LONGITUDE),
                        ),
                    markedAsFavorite = false,
                    busLines = listOf(busLine2),
                    status = StopStatus.ACTIVA,
                ),
            )
        }
    }
}
