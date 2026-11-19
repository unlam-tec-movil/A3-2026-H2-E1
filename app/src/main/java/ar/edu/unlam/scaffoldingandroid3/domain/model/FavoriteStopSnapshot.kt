
package ar.edu.unlam.scaffoldingandroid3.domain.model

data class FavoriteStopSnapshot(
    val stopId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val lines: String,
)

fun BusStop.toFavoriteSnapshot(): FavoriteStopSnapshot =
    FavoriteStopSnapshot(
        stopId = stopId,
        name = name,
        latitude = locationData.latitude.value,
        longitude = locationData.longitude.value,
        lines = busLines.joinToString(" · ") { "Línea ${it.number}" },
    )
