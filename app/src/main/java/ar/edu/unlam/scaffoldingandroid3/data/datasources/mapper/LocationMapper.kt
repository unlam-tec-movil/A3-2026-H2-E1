package ar.edu.unlam.mobile.movil22026h2.data.datasources.mapper

import android.location.Location
import ar.edu.unlam.mobile.movil22026h2.domain.model.Latitude
import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData
import ar.edu.unlam.mobile.movil22026h2.domain.model.Longitude

/**
 * Mapper utilizado para convertir datos de ubicación brutos del sistema de ubicación
 * de Android al modelo de dominio de la aplicación.
 *
 * Esta clase proporciona una conversión simple desde [Location] (proporcionado por
 * Google Play Services) a [LocationData], extrayendo la latitud y longitud.
 *
 * @since 1.0
 */
class LocationMapper {
    /**
     * Convierte una ubicación bruta del sistema al modelo de dominio.
     *
     * Esta función extrae la latitud y longitud de una instancia de [Location] y las
     * envuelve en un objeto [LocationData], manteniendo las coordenadas en radianes
     * según el modelo de dominio.
     *
     * @param location la ubicación bruta proporcionada por el Fused Location Provider
     *                 o cualquier otro proveedor de ubicación de Android.
     * @return una instancia de [LocationData] con la latitud y longitud extraídas.
     */
    fun toDomain(location: Location): LocationData =
        LocationData(
            Latitude(location.latitude),
            Longitude(location.longitude),
        )
}
