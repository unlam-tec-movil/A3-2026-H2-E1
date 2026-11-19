package ar.edu.unlam.scaffoldingandroid3.domain.model

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Constantes geodésicas para cálculos de distancia y dirección sobre la superficie terrestre.
 *
 * Este objeto define las constantes fundamentales utilizadas en los cálculos geodésicos:
 * - Radio de la Tierra para cálculos de distancia
 * - Longitud de un grado de latitud en metros
 * - Grados en un círculo completo
 *
 * **Constantes**:
 * - [EARTH_RADIUS_METERS]: Radio medio de la Tierra (6,371,000 metros)
 * - [METERS_PER_DEGREE]: Aproximación de la longitud de un grado de latitud (111,320 metros)
 * - [DEGREES_IN_CIRCLE]: Grados en un círculo completo (360)
 *
 * @see LocationData.distanceMetersTo()
 * @see LocationData.bearingDegreesTo()
 */
object GeodesyConstants {
    /**
     * Radio medio de la Tierra en metros.
     *
     * Valor estándar utilizado en cálculos de distancia sobre la superficie terrestre.
     * Representa el radio medio de la Tierra para convertir distancias angulares a metros.
     *
     * **Valor**: 6,371,000 metros (6,371 km)
     *
     * @see LocationData.distanceMetersTo()
     */
    const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Longitud aproximada de un grado de latitud en metros.
     *
     * Valor aproximado utilizado para cálculos de distancia simple en latitud.
     * Un grado de latitud tiene una longitud relativamente constante (~111 km)
     * en cualquier lugar de la Tierra, independientemente de la longitud.
     *
     * **Valor**: 111,320 metros por grado de latitud
     *
     * @see LocationData.projectedMetersFrom()
     */
    const val METERS_PER_DEGREE = 111_320.0

    /**
     * Grados en un círculo completo.
     *
     * Valor utilizado para normalizar ángulos de rumbo al rango [0.0, 360.0).
     *
     * **Valor**: 360.0 grados
     *
     * @see LocationData.bearingDegreesTo()
     */
    const val DEGREES_IN_CIRCLE = 360.0
}

/**
 * Calcula la distancia en metros entre dos ubicaciones utilizando la fórmula de Haversine.
 *
 * Esta función calcula la distancia más corta sobre la superficie de una esfera (la Tierra)
 * entre dos puntos dados por sus coordenadas de latitud y longitud.
 *
 * **Algoritmo**: Fórmula de Haversine.
 *
 * **Fórmula**:
 * ```text
 * Δφ = φ₂ - φ₁ (diferencia en latitud en radianes)
 * Δλ = λ₂ - λ₁ (diferencia en longitud en radianes)
 * a = sin²(Δφ/2) + cos(φ₁) × cos(φ₂) × sin²(Δλ/2)
 * c = 2 × atan2(√a, √(1−a))
 * d = R × c
 * ```
 *
 * Donde:
 * - φ = latitud en radianes
 * - λ = longitud en radianes
 * - R = radio de la Tierra
 * - d = distancia en metros
 *
 * **Unidad**: Metro (m)
 *
 * **Nota**: El resultado se limita a 2πR (circunferencia máxima) para evitar errores por
 * redondeo cuando los puntos están muy cerca del antípoda.
 *
 * @param other la ubicación objetivo para calcular la distancia.
 * @return la distancia en metros entre esta ubicación y la ubicación objetivo.
 * @see LocationData.distanceMetersTo()
 * @see GeodesyConstants.EARTH_RADIUS_METERS
 */
fun LocationData.distanceMetersTo(other: LocationData): Double {
    val deltaLatitude = Math.toRadians(other.latitude.value - latitude.value)
    val deltaLongitude = Math.toRadians(other.longitude.value - longitude.value)
    val latitude1 = Math.toRadians(latitude.value)
    val latitude2 = Math.toRadians(other.latitude.value)
    val haversine =
        sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
            cos(latitude1) * cos(latitude2) * sin(deltaLongitude / 2) * sin(deltaLongitude / 2)
    return 2 * GeodesyConstants.EARTH_RADIUS_METERS * asin(min(1.0, sqrt(haversine)))
}

/**
 * Calcula el rumbo (dirección) en grados desde esta ubicación hasta una ubicación objetivo.
 *
 * Esta función calcula el ángulo de dirección (bearing) en sentido horario desde el Norte
 * verdadero (0°) hacia la ubicación objetivo.
 *
 * **Algoritmo**: Fórmula del rumbo inicial (initial bearing formula).
 *
 * **Fórmula**:
 * ```text
 * φ₁, φ₂ = latitudes en radianes
 * Δλ = λ₂ - λ₁ (diferencia en longitud en radianes)
 * y = sin(Δλ) × cos(φ₂)
 * x = cos(φ₁) × sin(φ₂) - sin(φ₁) × cos(φ₂) × cos(Δλ)
 * θ = atan2(y, x)
 * ```
 *
 * Donde:
 * - θ = rumbo en radianes
 * - El resultado se convierte a grados y se normaliza al rango [0.0, 360.0)
 *
 * **Unidad**: Grado (°)
 *
 * **Convención de ángulos**:
 * - 0° = Norte
 * - 90° = Este
 * - 180° = Sur
 * - 270° = Oeste
 *
 * **Nota**: El resultado se normaliza al rango [0.0, 360.0) usando el operador módulo.
 *
 * @param target la ubicación objetivo hacia la cual calcular el rumbo.
 * @return el rumbo en grados desde esta ubicación hacia la ubicación objetivo,
 *         normalizado al rango [0.0, 360.0).
 * @see LocationData.bearingDegreesTo()
 * @see GeodesyConstants.DEGREES_IN_CIRCLE
 */
fun LocationData.bearingDegreesTo(target: LocationData): Double {
    val latitude1 = Math.toRadians(latitude.value)
    val latitude2 = Math.toRadians(target.latitude.value)
    val deltaLongitude = Math.toRadians(target.longitude.value - longitude.value)

    val y = sin(deltaLongitude) * cos(latitude2)
    val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(deltaLongitude)

    val degrees = Math.toDegrees(atan2(y, x))
    return (degrees + GeodesyConstants.DEGREES_IN_CIRCLE) % GeodesyConstants.DEGREES_IN_CIRCLE
}

/**
 * Calcula el desplazamiento proyectado en metros (norte, este) desde una ubicación origen.
 *
 * Esta función calcula las componentes norte y este de la diferencia entre esta ubicación
 * y una ubicación origen, utilizando una aproximación plana para distancias cortas.
 *
 * **Algoritmo**: Proyección plana (easting/northing).
 *
 * **Fórmula**:
 * ```text
 * north = (φ - φ₀) × M
 * east = (λ - λ₀) × N × cos(φ)
 * ```
 *
 * Donde:
 * - M = longitud de un grado de latitud (~111,320 metros)
 * - N = longitud de un grado de longitud (varía con la latitud)
 * - φ = latitud en grados
 * - λ = longitud en grados
 *
 * **Unidad**: Metro (m)
 *
 * **Aproximación**: Esta función asume una superficie plana local y es adecuada para
 * distancias cortas (varios kilómetros). Para distancias largas, use [distanceMetersTo()].
 *
 * @param origin la ubicación origen desde la cual calcular el desplazamiento proyectado.
 * @return una pareja de valores (norte, este) en metros:
 *         - norte: desplazamiento norte-sur positivo hacia el norte
 *         - este: desplazamiento este-oeste positivo hacia el este
 * @see LocationData.projectedMetersFrom()
 * @see GeodesyConstants.METERS_PER_DEGREE
 */
fun LocationData.projectedMetersFrom(origin: LocationData): Pair<Double, Double> {
    val north = (latitude.value - origin.latitude.value) * GeodesyConstants.METERS_PER_DEGREE
    val east =
        (longitude.value - origin.longitude.value) *
            GeodesyConstants.METERS_PER_DEGREE * cos(Math.toRadians(latitude.value))
    return north to east
}

/**
 * Calcula una nueva ubicación desplazada una distancia específica en metros desde esta ubicación.
 *
 * Esta función calcula las coordenadas de latitud y longitud de una nueva ubicación
 * desplazada por las distancias especificadas en las direcciones norte y este.
 *
 * **Algoritmo**: Inversión de la proyección plana.
 *
 * **Fórmula**:
 * ```text
 * Δφ = north / R
 * Δλ = east / (R × cos(φ))
 * φ' = φ + Δφ
 * λ' = λ + Δλ
 * ```
 *
 * Donde:
 * - R = radio de la Tierra
 * - φ = latitud en radianes (para el cálculo de Δλ)
 * - φ' = nueva latitud en radianes
 * - λ' = nueva longitud en radianes
 *
 * **Unidad**: Metro (m)
 *
 * **Nota**: El desplazamiento es local y asume una superficie plana. Para distancias
 * grandes, el resultado puede ser inexacto.
 *
 * @param northMeters el desplazamiento en dirección norte (positivo hacia el norte).
 * @param eastMeters el desplazamiento en dirección este (positivo hacia el este).
 * @return una nueva [LocationData] con las coordenadas desplazadas.
 * @see LocationData.offsetByMeters()
 * @see GeodesyConstants.EARTH_RADIUS_METERS
 */
fun LocationData.offsetByMeters(
    northMeters: Double,
    eastMeters: Double,
): LocationData {
    val latitudeDelta = Math.toDegrees(northMeters / GeodesyConstants.EARTH_RADIUS_METERS)
    val longitudeDelta =
        Math.toDegrees(
            eastMeters / (GeodesyConstants.EARTH_RADIUS_METERS * cos(Math.toRadians(latitude.value))),
        )
    return LocationData(
        Latitude(latitude.value + latitudeDelta),
        Longitude(longitude.value + longitudeDelta),
    )
}
