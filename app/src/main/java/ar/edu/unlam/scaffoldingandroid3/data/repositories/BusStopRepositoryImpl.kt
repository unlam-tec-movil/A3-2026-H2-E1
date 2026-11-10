package ar.edu.unlam.scaffoldingandroid3.data.repositories

import android.util.Log
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.demo.DemoBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.RemoteBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de la interfaz de repositorio para las paradas de autobús.
 *
 * Proporciona acceso a las paradas de autobús a través de múltiples fuentes de datos:
 * - Fuente remota (API principal)
 * - Cache local (almacenamiento persistente)
 * - Fuente de demostración (fallback)
 *
 * **Funcionalidad**:
 * - Consulta paradas cercanas según ubicación y radio
 * - Gestiona el cache local de paradas de autobús
 * - Persiste y recupera la historial de búsquedas
 * - Implementa estrategia de fallback entre fuentes de datos
 *
 * **Estrategia de datos**:
 * 1. Intenta obtener paradas desde la fuente remota
 * 2. Si la fuente remota devuelve resultados, los guarda en cache y los retorna
 * 3. Si la fuente remota falla o no devuelve resultados, usa el cache
 * 4. Si el cache está vacío, usa datos de demostración
 *
 * **Dependencias**:
 * - [RemoteBusStopDataSource] - Fuente principal de datos de paradas
 * - [BusStopCacheDao] - DAO para el cache local de paradas
 * - [SearchHistoryDao] - DAO para la persistencia del historial de búsquedas
 * - [DemoBusStopDataSource] - Fuente de datos de demostración (fallback)
 *
 * @see BusStopRepository
 * @see RemoteBusStopDataSource
 * @see BusStopCacheDao
 * @see SearchHistoryDao
 */
@Singleton
class BusStopRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteBusStopDataSource,
        private val busStopCacheDao: BusStopCacheDao,
        private val searchHistoryDao: SearchHistoryDao,
        private val demoDataSource: DemoBusStopDataSource,
    ) : BusStopRepository {
        /**
         * Consulta las paradas de autobús cercanas a una ubicación.
         *
         * Esta función implementa una estrategia de fallback para obtener las paradas cercanas:
         * 1. Primero intenta obtener las paradas desde la fuente remota (API principal)
         * 2. Si la API devuelve datos, los guarda en el cache y los retorna
         * 3. Si la API falla o no devuelve datos, intenta usar el cache local
         * 4. Si el cache está vacío, retorna datos de demostración
         *
         * **Estrategia de fallback**:
         * - Fuente remota → Cache local → Datos de demostración
         *
         * **Ordenación**: Las paradas se ordenan por distancia creciente desde el centro.
         *
         * @param center la ubicación central para la búsqueda (latitud y longitud)
         * @param radiusMeters el radio de búsqueda en metros
         * @return un [NearbyStopsResult] con las paradas ordenadas por proximidad y la fuente utilizada.
         * @see BusStopRepository.getNearbyStops()
         */
        override suspend fun getNearbyStops(
            center: LocationData,
            radiusMeters: Double,
        ): NearbyStopsResult {
            val remoteStops =
                try {
                    remoteDataSource.findByLocation(center, radiusMeters)
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "Error al consultar las paradas remotas; se degrada la fuente", e)
                    null
                }

            if (!remoteStops.isNullOrEmpty()) {
                busStopCacheDao.clearCache()
                busStopCacheDao.insertStops(remoteStops.map { it.toCacheEntity() })
                return NearbyStopsResult(
                    stops = remoteStops.sortedByNearestTo(center),
                    origin = NearbyStopsOrigin.REMOTE,
                )
            }

            if (remoteStops == null) {
                val cachedStops = busStopCacheDao.getCachedStopsList().map { it.toDomain() }
                if (cachedStops.isNotEmpty()) {
                    return NearbyStopsResult(
                        stops = cachedStops.sortedByNearestTo(center),
                        origin = NearbyStopsOrigin.CACHE,
                    )
                }
            }

            return NearbyStopsResult(
                stops = demoDataSource.getDemoStops(center, radiusMeters),
                origin = NearbyStopsOrigin.DEMO,
            )
        }

        /**
         * Proporciona un flujo con todas las paradas guardadas en el cache local.
         *
         * Esta función consulta el DAO del cache y transforma las entidades de cache
         * a objetos de dominio [BusStop]. El flujo se actualiza automáticamente
         * cuando el cache cambia.
         *
         * @return un [Flow] que emite la lista de todas las paradas guardadas en cache.
         * @see BusStopRepository.getCachedStops()
         * @see BusStopCacheDao.getAllCachedStops()
         */
        override fun getCachedStops(): Flow<List<BusStop>> =
            busStopCacheDao.getAllCachedStops().map { entities ->
                entities.map { it.toDomain() }
            }

        /**
         * Guarda una consulta de búsqueda en el historial.
         *
         * Esta función elimina la versión anterior de la consulta (si existe) e inserta
         * la nueva consulta en el historial para futuras recuperaciones.
         *
         * **Comportamiento**:
         * - Elimina la consulta previa con el mismo texto (tras eliminar espacios en blanco)
         * - Inserta la nueva consulta en el historial
         *
         * @param query la consulta de búsqueda a guardar.
         * @see BusStopRepository.saveSearchQuery()
         */
        override suspend fun saveSearchQuery(query: String) {
            val trimmed = query.trim()
            if (trimmed.isNotBlank()) {
                searchHistoryDao.deleteSearch(trimmed)
                searchHistoryDao.insertSearch(SearchHistoryEntity(query = trimmed))
            }
        }

        /**
         * Proporciona un flujo con las búsquedas recientes guardadas en el historial.
         *
         * Esta función consulta el DAO del historial y transforma las entidades
         * a objetos de dominio [SearchHistory]. El flujo se actualiza automáticamente
         * cuando el historial cambia.
         *
         * **Nota**: El historial se ordena por fecha de búsqueda (más reciente primero).
         *
         * @return un [Flow] que emite la lista de búsquedas recientes.
         * @see BusStopRepository.getRecentSearches()
         * @see SearchHistoryDao.getRecentSearches()
         */
        override fun getRecentSearches(): Flow<List<SearchHistory>> =
            searchHistoryDao.getRecentSearches().map { entities ->
                entities.map { entity ->
                    SearchHistory(
                        id = entity.id,
                        query = entity.query,
                        timestamp = entity.timestamp,
                    )
                }
            }

        /**
         * Ordena las paradas por distancia creciente desde el centro especificado.
         *
         * Esta función ordena una lista de paradas calculando la distancia desde
         * cada parada hasta la ubicación central especificada.
         *
         * @param center la ubicación central para calcular las distancias.
         * @return la lista de paradas ordenada por distancia creciente.
         * @see BusStopRepository.getNearbyStops()
         * @see LocationData.distanceMetersTo()
         */
        private fun List<BusStop>.sortedByNearestTo(center: LocationData): List<BusStop> =
            sortedBy { stop -> center.distanceMetersTo(stop.locationData) }

        private companion object {
            const val LOG_TAG = "BusStopRepository"
        }
    }
