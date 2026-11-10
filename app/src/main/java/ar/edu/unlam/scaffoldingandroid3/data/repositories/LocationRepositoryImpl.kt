package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Implementación de la interfaz de repositorio para datos de ubicación.
 *
 * Proporciona acceso a los datos de ubicación del dispositivo a través de
 * la fuente de datos [LocationDataSource].
 *
 * **Funcionalidad**:
 * - Obtiene los datos de ubicación más recientes conocidos por el dispositivo
 * - Verifica si el servicio de ubicación está habilitado en el dispositivo
 *
 * **Dependencia**: [LocationDataSource] - Fuente de datos para la ubicación del dispositivo.
 *
 * @see LocationRepository
 * @see LocationDataSource
 */
class LocationRepositoryImpl
    @Inject
    constructor(
        private val dataSource: LocationDataSource,
    ) : LocationRepository {
        /**
         * Obtiene los datos de ubicación más recientes conocidos por el dispositivo.
         *
         * Esta función consulta al dispositivo para obtener la última ubicación conocida,
         * incluyendo coordenadas de latitud y longitud, y la hora del último registro.
         *
         * **Comportamiento**:
         * - Retorna los datos de ubicación si el dispositivo ha registrado una ubicación
         * - Retorna null si no hay datos de ubicación disponibles
         *
         * @return los datos de ubicación más recientes, o null si no hay datos disponibles.
         * @see LocationRepository.getLocationData()
         */
        override suspend fun getLocationData(): LocationData? = dataSource.getLastKnowLocation()

        /**
         * Verifica si el servicio de ubicación está habilitado en el dispositivo.
         *
         * Esta función consulta al dispositivo para determinar si el servicio de ubicación
         * (GPS, redes celulares, etc.) está activado y disponible para usar.
         *
         * **Funcionalidad**:
         * - Consulta al sistema del dispositivo para verificar el estado del servicio de ubicación
         * - Retorna true si el servicio está habilitado y disponible
         * - Retorna false si el servicio está deshabilitado o no disponible
         *
         * @return true si el servicio de ubicación está habilitado, false en caso contrario.
         * @see LocationRepository.isLocationEnabled()
         */
        override suspend fun isLocationEnabled(): Boolean = dataSource.isLocationEnabled()
    }
