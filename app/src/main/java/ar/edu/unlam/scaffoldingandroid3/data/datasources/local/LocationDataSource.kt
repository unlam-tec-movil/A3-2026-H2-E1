package ar.edu.unlam.mobile.movil22026h2.data.datasources.local

import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData

/**
 * Interfaz de DataSource para obtener datos de ubicación del dispositivo.
 *
 * Esta interfaz define el contrato para fuentes de datos locales que proporcionan
 * acceso a la última ubicación conocida del dispositivo. Las implementaciones típicas
 * utilizan el Fused Location Provider de Google Play Services.
 *
 * @since 1.0
 */
interface LocationDataSource {
    /**
     * Obtiene la última ubicación conocida del dispositivo.
     *
     * Esta función consulta la fuente de datos subyacente para recuperar la última
     * ubicación disponible. El comportamiento exacto depende de la implementación:
     * - Puede devolver `null` si no hay ubicación disponible
     * - Puede devolver una ubicación reciente o en caché
     * - Puede devolver `null` si no hay permisos de ubicación
     *
     * **Nota**: Esta operación es suspendida y debe ejecutarse en un contexto
     * adecuado para corutinas (como un coroutine scope o suspend function).
     *
     * @return una instancia de [LocationData] con la última ubicación conocida, o `null`
     *         si no es posible obtener la ubicación.
     */
    suspend fun getLastKnowLocation(): LocationData?
}
