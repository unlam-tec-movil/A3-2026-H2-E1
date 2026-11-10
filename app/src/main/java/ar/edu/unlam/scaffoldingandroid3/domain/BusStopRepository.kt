package ar.edu.unlam.scaffoldingandroid3.domain

import kotlinx.coroutines.flow.Flow

/** Fuente de paradas de colectivo cercanas a un punto e historial de búsquedas. */
interface BusStopRepository {
    /** Devuelve las paradas dentro de [radiusMeters], ordenadas de la más cercana a la más lejana. */
    suspend fun getNearbyStops(
        center: GeoPoint,
        radiusMeters: Double = 1000.0,
    ): List<BusStop>

    /** Devuelve las paradas guardadas en la caché local de Room. */
    fun getCachedStops(): Flow<List<BusStop>>

    /** Guarda una consulta de búsqueda en el historial local de Room. */
    suspend fun saveSearchQuery(query: String)

    /** Devuelve el historial de búsquedas recientes. */
    fun getRecentSearches(): Flow<List<SearchHistory>>
}
