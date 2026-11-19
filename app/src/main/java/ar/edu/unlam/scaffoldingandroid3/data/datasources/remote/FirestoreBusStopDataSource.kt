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

class FirestoreBusStopDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : RemoteBusStopDataSource {
        override suspend fun findByLocation(
            center: LocationData,
            radiusMeters: Double,
        ): List<BusStop> {
            val bounds =
                GeoFireUtils.getGeoHashQueryBounds(
                    GeoLocation(center.latitude.value, center.longitude.value),
                    radiusMeters,
                )

            return coroutineScope {
                bounds
                    .map { bound ->
                        async {
                            firestore
                                .collection(COLLECTION_NAME)
                                .orderBy("geohash")
                                .startAt(bound.startHash)
                                .endAt(bound.endHash)
                                .get()
                                .await()
                                .documents
                                .mapNotNull { doc -> doc.toObject(FirestoreBusStop::class.java)?.toDomain(doc.id) }
                        }
                    }.awaitAll()
                    .flatten()
            }.distinctBy { it.stopId }
                .filter { center.distanceMetersTo(it.locationData) <= radiusMeters }
        }

        private companion object {
            const val COLLECTION_NAME = "gtfs_stops"
        }
    }
