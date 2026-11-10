package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import ar.edu.unlam.scaffoldingandroid3.data.database.MongoDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.fromBsonDocument
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlinx.coroutines.flow.toList
import org.bson.Document
import javax.inject.Inject

class MongoBusStopDataSource
    @Inject
    constructor() : RemoteBusStopDataSource {
        private val collection by lazy {
            MongoDataSource
                .getDatabase(DATABASE_NAME)
                .getCollection<Document>(COLLECTION_NAME)
        }

        override suspend fun findByLocation(
            center: LocationData,
            radiusMeters: Double,
        ): List<BusStop> {
            val query =
                Document(
                    "geometry",
                    Document(
                        $$"$nearSphere",
                        Document(
                            $$"$geometry",
                            Document("type", "Point")
                                .append("coordinates", listOf(center.longitude.value, center.latitude.value)),
                        ).append($$"$maxDistance", radiusMeters),
                    ),
                )

            return collection.find(query).toList().map { document -> fromBsonDocument(document) }
        }

        private companion object {
            const val DATABASE_NAME = "voy_en_bondi"
            const val COLLECTION_NAME = "paradas"
        }
    }
