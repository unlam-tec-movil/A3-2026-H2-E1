package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject

@Serializable
internal data class OsrmResponseDto(
    val code: String = "",
    val routes: List<OsrmRouteDto> = emptyList(),
)

@Serializable
internal data class OsrmRouteDto(
    val geometry: OsrmGeometryDto? = null,
    val distance: Double = 0.0,
    val duration: Double = 0.0,
)

@Serializable
internal data class OsrmGeometryDto(
    val coordinates: List<List<Double>> = emptyList(),
    val type: String = "",
)

class OsrmRouteDataSourceImpl
    @Inject
    constructor() : OsrmRouteDataSource {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        override suspend fun getPedestrianRoute(
            origin: LocationData,
            destination: LocationData,
        ): PedestrianRoute =
            withContext(Dispatchers.IO) {
                val urlString =
                    String.format(
                        Locale.US,
                        "$BASE_URL/route/v1/foot/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                        origin.longitude.value,
                        origin.latitude.value,
                        destination.longitude.value,
                        destination.latitude.value,
                    )

                val url = URL(urlString)
                val connection =
                    (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = TIMEOUT_MILLIS
                        readTimeout = TIMEOUT_MILLIS
                        setRequestProperty("User-Agent", "VoyEnBondi/1.0 (Android)")
                    }

                try {
                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw IllegalStateException("OSRM API HTTP error: $responseCode")
                    }

                    val responseText =
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            reader.readText()
                        }

                    parseResponse(responseText)
                } finally {
                    connection.disconnect()
                }
            }

        fun parseResponse(jsonString: String): PedestrianRoute {
            val response = json.decodeFromString<OsrmResponseDto>(jsonString)
            if (response.code != "Ok" || response.routes.isEmpty()) {
                throw IllegalStateException("OSRM did not return any route: code=${response.code}")
            }

            val route = response.routes.first()
            val points =
                route.geometry?.coordinates?.mapNotNull { coord ->
                    if (coord.size >= 2) {
                        val lon = coord[0]
                        val lat = coord[1]
                        LocationData(
                            latitude = Latitude(lat),
                            longitude = Longitude(lon),
                        )
                    } else {
                        null
                    }
                } ?: emptyList()

            return PedestrianRoute(
                points = points,
                distanceMeters = route.distance,
                durationSeconds = route.duration,
            )
        }

        companion object {
            private const val BASE_URL = "https://router.project-osrm.org"
            private const val TIMEOUT_MILLIS = 8_000
        }
    }
