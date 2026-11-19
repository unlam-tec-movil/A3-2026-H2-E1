
package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import kotlinx.coroutines.flow.Flow

interface FavoriteBusStopRepository {
    fun observeFavoriteStopIds(): Flow<Set<Long>>

    fun observeFavoriteStops(): Flow<List<FavoriteStopSnapshot>>

    suspend fun addFavorite(stopId: Long)

    suspend fun addFavorite(stop: FavoriteStopSnapshot)

    suspend fun removeFavorite(stopId: Long)

    suspend fun isFavorite(stopId: Long): Boolean
}
