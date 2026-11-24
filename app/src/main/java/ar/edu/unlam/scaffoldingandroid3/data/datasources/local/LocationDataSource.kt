package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de fuente de datos para obtener información de ubicación del dispositivo.
 *
 * Esta interfaz define los métodos para acceder a los datos de ubicación del dispositivo,
 * incluyendo la última ubicación conocida y el estado del servicio de ubicación.
 *
 * **Funcionalidad**:
 * - Obtiene los datos de ubicación más recientes del dispositivo
 * - Mantiene una suscripción continua para recibir actualizaciones de ubicación en tiempo real
 * - Verifica si el servicio de ubicación está habilitado
 *
 * **Uso**: Esta interfaz es implementada por [LocationDataSourceImpl] para proporcionar
 * acceso a los datos de ubicación del dispositivo.
 *
 * @see LocationDataSourceImpl
 * @see LocationData
 */
interface LocationDataSource {
    /**
     * Obtiene los datos de ubicación más recientes conocidos por el dispositivo.
     *
     * Esta función consulta al dispositivo para obtener la última ubicación registrada,
     * incluyendo las coordenadas de latitud y longitud, así como la hora del último
     * registro de ubicación.
     *
     * **Comportamiento**:
     * - Retorna los datos de ubicación si el dispositivo ha registrado una ubicación
     * - Retorna null si no hay datos de ubicación disponibles
     *
     * @return los datos de ubicación más recientes, o null si no hay datos disponibles.
     * @see LocationData
     */
    suspend fun getLastKnowLocation(): LocationData?

    /**
     * Proporciona un flujo continuo de actualizaciones de ubicación en tiempo real.
     */
    fun getLocationUpdates(
        intervalMillis: Long = 3_000L,
        minDistanceMeters: Float = 3f,
    ): Flow<LocationData>

    /**
     * Verifica si el servicio de ubicación está habilitado en el dispositivo.
     *
     * Esta función consulta al dispositivo para determinar si el servicio de ubicación
     * (GPS, redes celulares, etc.) está activado y disponible para usar.
     *
     * **Comportamiento**:
     * - Retorna true si el servicio de ubicación está habilitado y disponible
     * - Retorna false si el servicio está deshabilitado o no disponible
     *
     * @return true si el servicio de ubicación está habilitado, false en caso contrario.
     */
    suspend fun isLocationEnabled(): Boolean
}
