package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Bearing
import ar.edu.unlam.scaffoldingandroid3.domain.model.BearingData
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Función auxiliar para convertir grados a radianes.
 *
 * @param degrees el valor en grados a convertir.
 * @return el valor equivalente en radianes.
 */
private fun degToRad(degrees: Double): Double = degrees * PI / 180.0

/**
 * Función auxiliar para convertir radianes a grados, normalizando el resultado al rango [0.0, 360.0).
 *
 * @param radians el valor en radianes a convertir.
 * @return el valor equivalente en grados, normalizado al rango [0.0, 360.0).
 */
private fun toDegrees(radians: Double): Double {
    val degrees = Math.toDegrees(radians)
    return (degrees + 360.0) % 360.0
}

/**
 * Caso de uso para calcular la dirección (rumbo) desde la ubicación del usuario hacia una parada de autobús.
 *
 * Esta clase implementa el algoritmo de cálculo de rumbo (bearing) entre dos puntos en la superficie terrestre.
 * El resultado representa el ángulo en grados medido en sentido horario desde el Norte verdadero (0°).
 *
 * **Algoritmo**: Fórmula del rumbo inicial (initial bearing formula).
 *
 * **Unidad**: Grados (°), normalizado al rango [0.0, 360.0).
 *
 * **Dirección**: 0° = Norte, 90° = Este, 180° = Sur, 270° = Oeste.
 *
 * @see LocationData
 * @see BearingData
 */
class CalculateDestinationDirection {
    /**
     * Calcula la dirección (rumbo) desde la ubicación del usuario hacia la ubicación de la parada de autobús.
     *
     * Esta función:
     * 1. Convierte las coordenadas de latitud y longitud a radianes
     * 2. Calcula las diferencias angulares de longitud
     * 3. Aplica la fórmula del rumbo inicial para calcular el ángulo
     * 4. Normaliza el resultado al rango [0.0, 360.0)
     * 5. Retorna el rumbo encapsulado en [BearingData]
     *
     * **Fórmula utilizada**: Initial bearing formula:
     * ```text
     * lat1, lat2 = latitudes en radianes
     * dLon = lon2 - lon1 en radianes
     * y = sin(dLon) × cos(lat2)
     * x = cos(lat1) × sin(lat2) - sin(lat1) × cos(lat2) × cos(dLon)
     * bearing = atan2(y, x)
     * ```
     *
     * @param userLocation la ubicación actual del usuario con coordenadas de latitud y longitud.
     * @param busStopLocation la ubicación de la parada de autobús con coordenadas de latitud y longitud.
     * @return un objeto [BearingData] con el rumbo hacia la parada de autobús, normalizado al rango [0.0, 360.0).
     */
    operator fun invoke(
        userLocation: LocationData,
        busStopLocation: LocationData,
    ): BearingData {
        val lat1Rad = degToRad(userLocation.latitude.value)
        val lat2Rad = degToRad(busStopLocation.latitude.value)
        val dLonRad =
            degToRad(
                busStopLocation.longitude.value - userLocation.longitude.value,
            )

        val y = sin(dLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)

        val bearingRad = atan2(y, x)
        val bearingDegrees = toDegrees(bearingRad)

        return BearingData(Bearing(bearingDegrees))
    }
}
