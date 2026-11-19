package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val FNV_OFFSET_BASIS = 0xcbf29ce484222325uL
private const val FNV_PRIME = 0x100000001b3uL

object BusStopMapper {
    @Serializable
    private data class CachedBusLine(
        val lineId: Long,
        val number: Int,
    )

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    fun BusStop.toCacheEntity(): BusStopCacheEntity =
        BusStopCacheEntity(
            stopId = stopId,
            name = name,
            latitude = locationData.latitude.value,
            longitude = locationData.longitude.value,
            linesJson =
                if (busLines.isEmpty()) {
                    ""
                } else {
                    jsonFormat.encodeToString(
                        busLines.map {
                            CachedBusLine(
                                it.lineId,
                                it.number,
                            )
                        },
                    )
                },
            markedAsFavorite = markedAsFavorite,
            stopStatus = status.name,
        )

    fun BusStopCacheEntity.toDomain(): BusStop =
        BusStop(
            stopId = stopId,
            name = name,
            locationData =
                LocationData(
                    latitude = Latitude(latitude),
                    longitude = Longitude(longitude),
                ),
            markedAsFavorite = markedAsFavorite,
            busLines =
                if (linesJson.isBlank()) {
                    emptyList()
                } else {
                    jsonFormat.decodeFromString<List<CachedBusLine>>(linesJson).map { cached ->
                        BusLine(
                            lineId = cached.lineId,
                            number = cached.number,
                            routes = emptyList(),
                            currentStatus = BusLineStatus.NORMAL,
                        )
                    }
                },
            status = runCatching { StopStatus.valueOf(stopStatus) }.getOrDefault(StopStatus.ACTIVA),
        )

    internal fun String.toLineNumber(): Int = toIntOrNull() ?: filter { it.isDigit() }.toIntOrNull() ?: hashCode()

    internal fun String.toStableLong(): Long = fold(FNV_OFFSET_BASIS) { acc, char -> (acc xor char.code.toULong()) * FNV_PRIME }.toLong()
}
