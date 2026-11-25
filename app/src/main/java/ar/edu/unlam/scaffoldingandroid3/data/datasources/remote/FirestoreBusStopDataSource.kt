package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COLLECTION_NAME = "gtfs_stops"
private const val ORDER_BY_STRING = "geohash"

/**
 * DataSource remoto para recuperar paradas de autobús cercanas mediante consultas geoespaciales
 * en Firestore.
 *
 * Utiliza Firebase GeoFire para realizar consultas eficientes basadas en coordenadas geográficas,
 * obteniendo un rango de hashes geohash que cubre el radio especificado alrededor de un punto
 * central. Filtra los resultados por distancia real y elimina duplicados basándose en el [BusStop.stopId].
 *
 * @see RemoteBusStopDataSource
 * @see FirestoreBusStop
 */
class FirestoreBusStopDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : RemoteBusStopDataSource {
        /**
         * Busca y devuelve todas las paradas de autobús dentro de un radio específico
         * alrededor de una ubicación central dada.
         *
         * Realiza una consulta geoespacial en Firestore utilizando Firebase GeoFire para obtener
         * documentos que caen dentro del área circular definida por el centro y el radio.
         * Luego filtra los resultados para mantener solo las paradas dentro del radio exacto
         * y elimina duplicados basándose en el [BusStop.stopId].
         *
         * @param center La ubicación central (latitud y longitud) alrededor de la cual buscar paradas.
         * @param radiusMeters El radio en metros dentro del cual buscar paradas de autobús.
         * @return Una lista de [BusStop] dentro del radio especificado, sin duplicados, ordenados
         *         por su posición geográfica relativa al centro.
         * @see LocationData
         * @see BusStop
         * @see GeoFireUtils
         */
        override suspend fun findByLocation(
            center: LocationData,
            radiusMeters: Double,
        ): List<BusStop> {
            val bounds =
                GeoFireUtils.getGeoHashQueryBounds(
                    GeoLocation(center.latitude.value, center.longitude.value),
                    radiusMeters,
                )
            val filteredStops =
                coroutineScope {
                    bounds
                        .map { bound ->
                            async {
                                firestore
                                    .collection(COLLECTION_NAME)
                                    .orderBy(ORDER_BY_STRING)
                                    .startAt(bound.startHash)
                                    .endAt(bound.endHash)
                                    .get()
                                    .await()
                                    .documents
                                    .mapNotNull { doc ->
                                        try {
                                            doc.toObject(FirestoreBusStop::class.java)?.toDomain(doc.id)
                                        } catch (e: Exception) {
                                            throw e
                                        }
                                    }.filter { center.distanceMetersTo(it.locationData) <= radiusMeters }
                            }
                        }.awaitAll()
                        .flatten()
                        .distinctBy { it.stopId }
                }
            return filteredStops
        }
    }
