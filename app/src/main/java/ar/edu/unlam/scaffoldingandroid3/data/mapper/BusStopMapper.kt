package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BusStopMapper {
    fun BusStop.toCacheEntity(): BusStopCacheEntity =
        BusStopCacheEntity(
            id = id,
            name = name,
            latitude = location.latitude,
            longitude = location.longitude,
            linesJson = Json.encodeToString(lines),
        )

    fun BusStopCacheEntity.toDomain(): BusStop =
        BusStop(
            id = id,
            name = name,
            lines = if (linesJson.isBlank()) emptyList() else Json.decodeFromString(linesJson),
            location = GeoPoint(latitude = latitude, longitude = longitude),
        )
}
