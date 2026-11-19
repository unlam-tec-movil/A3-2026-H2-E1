package ar.edu.unlam.scaffoldingandroid3.domain

import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0
private const val METERS_PER_DEGREE = 111_320.0

/** Coordenada geográfica simple, independiente de Android. */
@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    /** Distancia en metros a otra coordenada (fórmula de Haversine). */
    fun distanceTo(other: GeoPoint): Double {
        val deltaLatitude = Math.toRadians(other.latitude - latitude)
        val deltaLongitude = Math.toRadians(other.longitude - longitude)
        val latitude1 = Math.toRadians(latitude)
        val latitude2 = Math.toRadians(other.latitude)
        val haversine =
            sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
                cos(latitude1) * cos(latitude2) * sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
        return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(haversine)))
    }

    /** Desplazamiento en metros: north positivo al norte, east positivo al este. */
    fun projectedMetersFrom(origin: GeoPoint): Pair<Double, Double> {
        val north = (latitude - origin.latitude) * METERS_PER_DEGREE
        val east =
            (longitude - origin.longitude) *
                METERS_PER_DEGREE * cos(Math.toRadians(latitude))
        return north to east
    }

    /** Devuelve un punto desplazado la distancia indicada. Se usa para datos de demostración. */
    fun offset(
        northMeters: Double,
        eastMeters: Double,
    ): GeoPoint {
        val latitudeDelta = Math.toDegrees(northMeters / EARTH_RADIUS_METERS)
        val longitudeDelta =
            Math.toDegrees(
                eastMeters / (EARTH_RADIUS_METERS * cos(Math.toRadians(latitude))),
            )
        return GeoPoint(latitude + latitudeDelta, longitude + longitudeDelta)
    }
}
