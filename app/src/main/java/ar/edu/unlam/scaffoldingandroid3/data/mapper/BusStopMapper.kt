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
import org.bson.Document

private const val FALLBACK_STOP_NAME = "Parada de Colectivo"
private const val FNV_OFFSET_BASIS = 0xcbf29ce484222325uL
private const val FNV_PRIME = 0x100000001b3uL

object BusStopMapper {
    @Serializable
    private data class CachedBusLine(
        val lineId: Long,
        val number: Int,
    )

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    fun fromBsonDocument(doc: Document): BusStop {
        val properties = doc.get("properties", Document::class.java) ?: Document()
        val name =
            properties.getString("DIRECCION")
                ?: properties.getString("CALLE")
                ?: FALLBACK_STOP_NAME

        return BusStop(
            stopId = parseStopId(doc),
            name = name,
            locationData = parseLocation(doc, properties),
            markedAsFavorite = false,
            busLines = parseLines(properties),
            status = StopStatus.ACTIVA,
        )
    }

    private fun parseStopId(doc: Document): Long {
        val id = doc.getObjectId("_id")?.toHexString() ?: doc["_id"]?.toString().orEmpty()
        return id.toStableLong()
    }

    private fun parseLocation(
        doc: Document,
        properties: Document,
    ): LocationData {
        val geometry = doc.get("geometry", Document::class.java)
        if (geometry != null) {
            val coordinates = geometry["coordinates"] as? List<*>
            if (coordinates != null && coordinates.size >= 2) {
                val lng = (coordinates[0] as? Number)?.toDouble() ?: 0.0
                val lat = (coordinates[1] as? Number)?.toDouble() ?: 0.0
                return LocationData(latitude = Latitude(lat), longitude = Longitude(lng))
            }
        }

        val lat =
            properties.getDouble("coord_Y")
                ?: properties["coord_Y"]?.toString()?.toDoubleOrNull()
                ?: 0.0
        val lng =
            properties.getDouble("coord_X")
                ?: properties["coord_X"]?.toString()?.toDoubleOrNull()
                ?: 0.0

        return LocationData(latitude = Latitude(lat), longitude = Longitude(lng))
    }

    private fun parseLines(properties: Document): List<BusLine> {
        val lines = mutableListOf<BusLine>()

        properties.keys
            .filter { key ->
                key.startsWith("L", ignoreCase = true) &&
                    !key.contains(
                        "sen",
                        ignoreCase = true,
                    )
            }.sortedBy { key -> key.substring(1).toIntOrNull() ?: 99 }
            .forEach { key ->
                val code = properties.getString(key)?.trim()
                if (!code.isNullOrBlank() && !code.equals("null", ignoreCase = true)) {
                    lines.add(
                        BusLine(
                            lineId = code.toStableLong(),
                            number = code.toLineNumber(),
                            routes = emptyList(),
                            currentStatus = BusLineStatus.NORMAL,
                        ),
                    )
                }
            }

        return lines.distinctBy { it.lineId }
    }

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

    private fun String.toLineNumber(): Int = toIntOrNull() ?: filter { it.isDigit() }.toIntOrNull() ?: hashCode()

    private fun String.toStableLong(): Long = fold(FNV_OFFSET_BASIS) { acc, char -> (acc xor char.code.toULong()) * FNV_PRIME }.toLong()
}
