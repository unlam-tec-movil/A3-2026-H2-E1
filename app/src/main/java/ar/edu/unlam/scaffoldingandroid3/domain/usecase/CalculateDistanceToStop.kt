package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Caso de uso para calcular la distancia entre dos puntos de ubicación en la superficie terrestre.
 *
 * Esta clase implementa el algoritmo de Haversine para calcular la distancia geodésica entre
 * dos puntos dados por sus coordenadas de latitud y longitud. El resultado se expresa en kilómetros.
 *
 * **Algoritmo**: Haversine formula para distancias esféricas.
 *
 * **Unidad**: Kilómetros (km).
 *
 * **Radio de la Tierra**: 6371.0 km (valor estándar para cálculos geodésicos).
 *
 * @see LocationData
 */
class CalculateDistanceToStop {
    /**
     * Calcula la distancia en kilómetros entre dos puntos de ubicación.
     *
     * Esta función:
     * 1. Convierte las coordenadas de latitud y longitud de los puntos de partida y destino a radianes
     * 2. Aplica el algoritmo de Haversine para calcular la distancia geodésica
     * 3. Retorna la distancia en kilómetros
     *
     * **Fórmula utilizada**: Haversine formula:
     * ```text
     * a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
     * c = 2 × atan2(√a, √(1-a))
     * distancia = R × c
     * ```
     * donde R es el radio de la Tierra (6371.0 km).
     *
     * @param startingPoint el punto de partida con coordenadas de latitud y longitud.
     * @param destination el punto de destino con coordenadas de latitud y longitud.
     * @return la distancia en kilómetros entre los dos puntos de ubicación.
     * @throws IllegalArgumentException si las coordenadas de latitud o longitud no son válidas.
     */
    operator fun invoke(
        startingPoint: LocationData,
        destination: LocationData,
    ): Double = calculateDistanceByHaversineAlgorithm(startingPoint, destination)

    /**
     * Implementación del algoritmo de Haversine para calcular la distancia entre dos puntos.
     *
     * Esta función:
     * 1. Convierte las coordenadas de latitud y longitud a radianes
     * 2. Calcula las diferencias angulares de latitud y longitud
     * 3. Aplica la fórmula de Haversine:
     *    - Calcula el término 'a' usando seno y coseno
     *    - Calcula el arco 'c' usando atan2
     *    - Multiplica por el radio de la Tierra (6371.0 km)
     *
     * @param startingPoint el punto de partida con coordenadas de latitud y longitud.
     * @param destination el punto de destino con coordenadas de latitud y longitud.
     * @return la distancia en kilómetros entre los dos puntos.
     */
    private fun calculateDistanceByHaversineAlgorithm(
        startingPoint: LocationData,
        destination: LocationData,
    ): Double {
        val lat1 = Math.toRadians(startingPoint.latitude.value)
        val lat2 = Math.toRadians(destination.latitude.value)
        val deltaLat = Math.toRadians(destination.latitude.value - startingPoint.latitude.value)
        val deltaLon = Math.toRadians(destination.longitude.value - startingPoint.longitude.value)

        val a =
            sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return 6371.0 * c
    }
}
