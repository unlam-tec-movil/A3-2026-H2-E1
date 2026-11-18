package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import app.cash.turbine.test
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoriteBusStopsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val favorites = MutableStateFlow(emptySet<Long>())
    private val repository = mockk<FavoriteBusStopRepository>(relaxed = true)

    private fun createViewModel(): FavoriteBusStopsViewModel {
        every { repository.observeFavoriteStopIds() } returns favorites
        return FavoriteBusStopsViewModel(repository)
    }

    @Test
    fun `given no favorites, when observing, then emits an empty set`() =
        runTest {
            // Given
            val viewModel = createViewModel()

            // When / Then
            viewModel.favoriteStopIds.test {
                assertEquals(emptySet<Long>(), awaitItem())
            }
        }

    @Test
    fun `given the repository emits favorites, when observing, then the view model exposes them`() =
        runTest {
            // Given
            val viewModel = createViewModel()

            // When / Then
            viewModel.favoriteStopIds.test {
                assertEquals(emptySet<Long>(), awaitItem())
                favorites.value = setOf(STOP_ID)
                assertEquals(setOf(STOP_ID), awaitItem())
            }
        }

    @Test
    fun `given a stop, when adding a favorite, then delegates to the repository`() =
        runTest {
            // Given
            val viewModel = createViewModel()

            // When
            viewModel.addFavorite(STOP_ID)

            // Then
            coVerify(exactly = 1) { repository.addFavorite(STOP_ID) }
        }

    @Test
    fun `given a stop, when removing a favorite, then delegates to the repository`() =
        runTest {
            // Given
            val viewModel = createViewModel()

            // When
            viewModel.removeFavorite(STOP_ID)

            // Then
            coVerify(exactly = 1) { repository.removeFavorite(STOP_ID) }
        }

    @Test
    fun `given a stop that is not a favorite, when toggling it, then it is added`() =
        runTest {
            // Given
            coEvery { repository.isFavorite(STOP_ID) } returns false
            val viewModel = createViewModel()

            // When
            viewModel.toggleFavorite(STOP_ID)

            // Then
            coVerify(exactly = 1) { repository.addFavorite(STOP_ID) }
            coVerify(exactly = 0) { repository.removeFavorite(any()) }
        }

    @Test
    fun `given a stop that is a favorite, when toggling it, then it is removed`() =
        runTest {
            // Given
            coEvery { repository.isFavorite(STOP_ID) } returns true
            val viewModel = createViewModel()

            // When
            viewModel.toggleFavorite(STOP_ID)

            // Then
            coVerify(exactly = 1) { repository.removeFavorite(STOP_ID) }
            coVerify(exactly = 0) { repository.addFavorite(any()) }
        }

    private companion object {
        const val STOP_ID = 1L
    }
}
