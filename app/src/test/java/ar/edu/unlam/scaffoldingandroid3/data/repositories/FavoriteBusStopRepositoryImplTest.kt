
package ar.edu.unlam.scaffoldingandroid3.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteBusStopRepositoryImplTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `given no saved favorites, when observing, then emits an empty set`() =
        runTest {
            val repository = FavoriteBusStopRepositoryImpl(context)

            val favorites = repository.observeFavoriteStopIds().first()

            assertTrue(favorites.isEmpty())
        }

    @Test
    fun `given a stop, when adding it as favorite, then it is observed and persisted`() =
        runTest {
            val repository = FavoriteBusStopRepositoryImpl(context)

            repository.addFavorite(STOP_ID)

            assertEquals(
                setOf(STOP_ID),
                repository.observeFavoriteStopIds().first(),
            )
            assertTrue(repository.isFavorite(STOP_ID))
            assertEquals(
                setOf(STOP_ID),
                FavoriteBusStopRepositoryImpl(context)
                    .observeFavoriteStopIds()
                    .first(),
            )
        }

    @Test
    fun `given a favorite stop, when removing it, then it is no longer a favorite`() =
        runTest {
            val repository = FavoriteBusStopRepositoryImpl(context)
            repository.addFavorite(STOP_ID)

            repository.removeFavorite(STOP_ID)

            assertFalse(repository.isFavorite(STOP_ID))
            assertTrue(repository.observeFavoriteStopIds().first().isEmpty())
        }

    @Test
    fun `given a stop that was never added, when checking it, then it is not a favorite`() =
        runTest {
            val repository = FavoriteBusStopRepositoryImpl(context)

            val isFavorite = repository.isFavorite(STOP_ID)

            assertFalse(isFavorite)
        }

    @Test
    fun `given invalid persisted values, when loading, then they are ignored`() =
        runTest {
            context
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(
                    FAVORITES_KEY,
                    setOf("7", "not-a-number"),
                ).commit()

            val favorites =
                FavoriteBusStopRepositoryImpl(context)
                    .observeFavoriteStopIds()
                    .first()

            assertEquals(setOf(7L), favorites)
        }

    @Test
    fun `given a complete stop, when adding it, then its data is persisted`() =
        runTest {
            val stop =
                FavoriteStopSnapshot(
                    stopId = STOP_ID,
                    name = "Florencio Varela y Villegas",
                    latitude = -34.65,
                    longitude = -58.62,
                    lines = "Línea 96",
                )

            val repository = FavoriteBusStopRepositoryImpl(context)

            repository.addFavorite(stop)

            val restored = FavoriteBusStopRepositoryImpl(context)
            val savedStops = restored.observeFavoriteStops().first()

            assertEquals(listOf(stop), savedStops)
            assertTrue(restored.isFavorite(STOP_ID))
        }

    @Test
    fun `given preferences that cannot be written, when adding a favorite, then fails and keeps the previous state`() =
        runTest {
            val editor = mockk<SharedPreferences.Editor>()
            val preferences = mockk<SharedPreferences>()
            val failingContext = mockk<Context>()

            every {
                failingContext.getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE,
                )
            } returns preferences

            every {
                preferences.getStringSet(FAVORITES_KEY, emptySet())
            } returns emptySet()

            every {
                preferences.getString(SNAPSHOTS_KEY, "[]")
            } returns "[]"

            every { preferences.edit() } returns editor

            every {
                editor.putStringSet(FAVORITES_KEY, any())
            } returns editor

            every {
                editor.putString(SNAPSHOTS_KEY, any())
            } returns editor

            every { editor.commit() } returns false

            val repository = FavoriteBusStopRepositoryImpl(failingContext)

            val failure =
                runCatching {
                    repository.addFavorite(STOP_ID)
                }.exceptionOrNull()

            assertTrue(failure is IllegalStateException)
            assertFalse(repository.isFavorite(STOP_ID))
            assertTrue(repository.observeFavoriteStopIds().first().isEmpty())
        }

    private companion object {
        const val PREFERENCES_NAME = "voy_en_bondi_favorites"
        const val FAVORITES_KEY = "favorite_stop_ids"
        const val SNAPSHOTS_KEY = "favorite_stop_snapshots"
        const val STOP_ID = 1L
    }
}
