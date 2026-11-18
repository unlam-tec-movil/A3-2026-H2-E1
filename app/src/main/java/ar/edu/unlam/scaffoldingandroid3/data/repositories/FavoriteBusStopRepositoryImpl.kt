
package ar.edu.unlam.scaffoldingandroid3.data.repositories

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoriteBusStopRepositoryImpl(
    context: Context,
) : FavoriteBusStopRepository {

    private val preferences = context.getSharedPreferences(
        "voy_en_bondi_favorites",
        Context.MODE_PRIVATE,
    )

    private val favorites = MutableStateFlow(loadFavorites())

    override fun observeFavoriteStopIds() = favorites.asStateFlow()

    override suspend fun addFavorite(stopId: Long) {
        saveFavorites(favorites.value + stopId)
    }

    override suspend fun removeFavorite(stopId: Long) {
        saveFavorites(favorites.value - stopId)
    }

    override suspend fun isFavorite(stopId: Long): Boolean {
        return stopId in favorites.value
    }

    private fun saveFavorites(updatedFavorites: Set<Long>) {
        val saved = preferences.edit()
            .putStringSet(
                FAVORITES_KEY,
                updatedFavorites.map { it.toString() }.toSet(),
            )
            .commit()

        if (saved) {
            favorites.value = updatedFavorites
        } else {
            throw IllegalStateException(
                "No se pudieron guardar los cambios en favoritos",
            )
        }
    }

    private fun loadFavorites(): Set<Long> {
        return preferences.getStringSet(
            FAVORITES_KEY,
            emptySet(),
        ).orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    companion object {
        private const val FAVORITES_KEY = "favorite_stop_ids"
    }
}
