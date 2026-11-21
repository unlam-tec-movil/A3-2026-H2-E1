
package ar.edu.unlam.scaffoldingandroid3.data.repositories

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class FavoriteBusStopRepositoryImpl(
    context: Context,
) : FavoriteBusStopRepository {
    private val preferences =
        context.getSharedPreferences(
            "voy_en_bondi_favorites",
            Context.MODE_PRIVATE,
        )

    private val favoriteIds = MutableStateFlow(loadIds())
    private val favoriteStops = MutableStateFlow(loadStops())

    override fun observeFavoriteStopIds() = favoriteIds.asStateFlow()

    override fun observeFavoriteStops() = favoriteStops.asStateFlow()

    override suspend fun addFavorite(stopId: Long) {
        persist(favoriteIds.value + stopId, favoriteStops.value)
    }

    override suspend fun addFavorite(stop: FavoriteStopSnapshot) {
        val updatedStops =
            favoriteStops.value
                .filterNot { it.stopId == stop.stopId } + stop

        persist(
            favoriteIds.value + stop.stopId,
            updatedStops,
        )
    }

    override suspend fun removeFavorite(stopId: Long) {
        persist(
            favoriteIds.value - stopId,
            favoriteStops.value.filterNot { it.stopId == stopId },
        )
    }

    override suspend fun isFavorite(stopId: Long): Boolean = stopId in favoriteIds.value

    private fun persist(
        ids: Set<Long>,
        stops: List<FavoriteStopSnapshot>,
    ) {
        val json = JSONArray()

        stops.forEach { stop ->
            json.put(
                JSONObject().apply {
                    put("stopId", stop.stopId)
                    put("name", stop.name)
                    put("latitude", stop.latitude)
                    put("longitude", stop.longitude)
                    put("lines", stop.lines)
                },
            )
        }

        val saved =
            preferences
                .edit()
                .putStringSet(
                    FAVORITES_KEY,
                    ids.map { it.toString() }.toSet(),
                ).putString(SNAPSHOTS_KEY, json.toString())
                .commit()

        if (!saved) {
            throw IllegalStateException(
                "No se pudieron guardar los favoritos",
            )
        }

        favoriteIds.value = ids
        favoriteStops.value = stops
    }

    private fun loadIds(): Set<Long> =
        preferences
            .getStringSet(
                FAVORITES_KEY,
                emptySet(),
            ).orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()

    private fun loadStops(): List<FavoriteStopSnapshot> {
        val stored = preferences.getString(SNAPSHOTS_KEY, "[]") ?: "[]"

        return try {
            val json = JSONArray(stored)

            (0 until json.length())
                .mapNotNull { index ->
                    try {
                        val item = json.getJSONObject(index)
                        FavoriteStopSnapshot(
                            stopId = item.getLong("stopId"),
                            name = item.getString("name"),
                            latitude = item.getDouble("latitude"),
                            longitude = item.getDouble("longitude"),
                            lines = item.getString("lines"),
                        )
                    } catch (_: Exception) {
                        null
                    }
                }.filter { it.stopId in favoriteIds.value }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val FAVORITES_KEY = "favorite_stop_ids"
        private const val SNAPSHOTS_KEY = "favorite_stop_snapshots"
    }
}
