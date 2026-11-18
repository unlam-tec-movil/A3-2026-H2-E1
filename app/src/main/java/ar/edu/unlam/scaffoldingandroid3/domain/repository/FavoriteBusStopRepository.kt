
package ar.edu.unlam.scaffoldingandroid3.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteBusStopRepository {
    fun observeFavoriteStopIds(): Flow<Set<Long>>

    suspend fun addFavorite(stopId: Long)

    suspend fun removeFavorite(stopId: Long)

    suspend fun isFavorite(stopId: Long): Boolean
}
