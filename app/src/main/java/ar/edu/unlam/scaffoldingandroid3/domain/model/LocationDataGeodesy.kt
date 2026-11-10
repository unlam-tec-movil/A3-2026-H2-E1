package ar.edu.unlam.scaffoldingandroid3.domain.model

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0
private const val METERS_PER_DEGREE = 111_320.0

fun LocationData.distanceMetersTo(other: LocationData): Double {
    val deltaLatitude = Math.toRadians(other.latitude.value - latitude.value)
    val deltaLongitude = Math.toRadians(other.longitude.value - longitude.value)
    val latitude1 = Math.toRadians(latitude.value)
    val latitude2 = Math.toRadians(other.latitude.value)
    val haversine =
        sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
            cos(latitude1) * cos(latitude2) * sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
    return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(haversine)))
}

fun LocationData.bearingDegreesTo(target: LocationData): Double {
    val latitude1 = Math.toRadians(latitude.value)
    val latitude2 = Math.toRadians(target.latitude.value)
    val deltaLongitude = Math.toRadians(target.longitude.value - longitude.value)

    val y = sin(deltaLongitude) * cos(latitude2)
    val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(deltaLongitude)

    val degrees = Math.toDegrees(atan2(y, x))
    return (degrees + 360.0) % 360.0
}

fun LocationData.projectedMetersFrom(origin: LocationData): Pair<Double, Double> {
    val north = (latitude.value - origin.latitude.value) * METERS_PER_DEGREE
    val east =
        (longitude.value - origin.longitude.value) *
            METERS_PER_DEGREE * cos(Math.toRadians(latitude.value))
    return north to east
}

fun LocationData.offsetByMeters(
    northMeters: Double,
    eastMeters: Double,
): LocationData {
    val latitudeDelta = Math.toDegrees(northMeters / EARTH_RADIUS_METERS)
    val longitudeDelta =
        Math.toDegrees(
            eastMeters / (EARTH_RADIUS_METERS * cos(Math.toRadians(latitude.value))),
        )
    return LocationData(
        Latitude(latitude.value + latitudeDelta),
        Longitude(longitude.value + longitudeDelta),
    )
}
