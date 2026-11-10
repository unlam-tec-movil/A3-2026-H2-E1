package ar.edu.unlam.scaffoldingandroid3.data.mongodb

import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import javax.inject.Inject

private const val LOCATION_FIELD = "location"

/** Línea tal como se guarda en MongoDB. */
data class BusLineDocument(
    val code: String = "",
    val destination: String = "",
)

/** Punto GeoJSON: `coordinates` es `[longitud, latitud]`. */
data class GeoJsonPointDocument(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList(),
)

/** Parada tal como se guarda en MongoDB. */
data class BusStopDocument(
    @BsonId val id: String = "",
    val name: String = "",
    val lines: List<BusLineDocument> = emptyList(),
    val location: GeoJsonPointDocument = GeoJsonPointDocument(),
)

internal fun BusStopDocument.toDomain(): BusStop? {
    val (longitude, latitude) = location.coordinates.takeIf { it.size >= 2 } ?: return null
    return BusStop(
        id = id,
        name = name,
        lines = lines.map { BusLine(code = it.code, destination = it.destination) },
        location = GeoPoint(latitude = latitude, longitude = longitude),
    )
}

/**
 * Paradas reales desde un MongoDB remoto.
 * La colección necesita un índice `2dsphere` sobre `location` (GeoJSON Point).
 */
class BusStopMongoRepo
    @Inject
    constructor(
        private val collection: MongoCollection<BusStopDocument>,
    ) : BusStopRepository {
        override suspend fun getNearbyStops(
            center: GeoPoint,
            radiusMeters: Double,
        ): List<BusStop> {
            val origin = Point(Position(center.longitude, center.latitude))
            // $near ya devuelve los resultados ordenados del más cercano al más lejano.
            return collection
                .find(Filters.near(LOCATION_FIELD, origin, radiusMeters, null))
                .toList()
                .mapNotNull { it.toDomain() }
        }
    }
