package ar.edu.unlam.scaffoldingandroid3.domain

/** Fuente de paradas de colectivo cercanas a un punto. */
interface BusStopRepository {
    /** Devuelve las paradas dentro de [radiusMeters], ordenadas de la más cercana a la más lejana. */
    suspend fun getNearbyStops(
        center: GeoPoint,
        radiusMeters: Double,
    ): List<BusStop>
}
