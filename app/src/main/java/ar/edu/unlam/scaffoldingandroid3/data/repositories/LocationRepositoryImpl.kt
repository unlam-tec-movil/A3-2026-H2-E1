package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Implementación de [LocationRepository] que obtiene la ubicación del dispositivo a través
 * de [LocationDataSource].
 *
 * Esta implementación:
 * - Utiliza [LocationDataSource] para acceder a la última ubicación conocida
 * - Proporciona una ubicación por defecto si no es posible obtener la ubicación real
 * - La ubicación por defecto corresponde a Buenos Aires, Argentina (-34.6037, -58.3816)
 *
 * @param dataSource la fuente de datos de ubicación utilizada para obtener la última
 *                   ubicación conocida del dispositivo.
 */
class LocationRepositoryImpl
    @Inject
    constructor(
        private val dataSource: LocationDataSource,
    ) : LocationRepository {
        /**
         * Obtiene la ubicación actual del dispositivo.
         *
         * Esta función:
         * 1. Intenta obtener la última ubicación mediante [LocationDataSource.getLastKnowLocation]
         * 2. Si no hay ubicación disponible, utiliza [defaultLocation] como fallback
         * 3. Siempre devuelve una ubicación válida (nunca `null`)
         *
         * **Comportamiento**:
         * - Si el dataSource devuelve una ubicación, se devuelve esa ubicación
         * - Si el dataSource devuelve `null`, se devuelve [defaultLocation]
         * - Nunca devuelve `null`
         *
         * @return una instancia de [LocationData] con la ubicación actual o la ubicación por defecto.
         */
        override suspend fun getLocationData(): LocationData =
            runCatching { dataSource.getLastKnowLocation() ?: defaultLocation }.getOrNull()
                ?: defaultLocation

        /**
         * Ubicación por defecto utilizada cuando no es posible obtener la ubicación del dispositivo.
         *
         * Esta ubicación corresponde a Buenos Aires, Argentina:
         * - Latitud: -34.6037
         * - Longitud: -58.3816
         *
         * Se utiliza como fallback para garantizar que siempre se devuelve una ubicación válida,
         * incluso si el Fused Location Provider no está disponible o no hay permisos.
         */
        private val defaultLocation =
            LocationData(
                latitude = Latitude(-34.6037),
                longitude = Longitude(-58.3816),
            )
    }
