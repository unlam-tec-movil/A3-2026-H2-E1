package ar.edu.unlam.scaffoldingandroid3.data.local

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.demo.DemoBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.RemoteBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.repositories.BusStopRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.offsetByMeters
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.locationDataInstance
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.IOException

class BusStopRepositoryImplTest {
    private val remoteDataSource = mockk<RemoteBusStopDataSource>()
    private val busStopCacheDao = mockk<BusStopCacheDao>()
    private val searchHistoryDao = mockk<SearchHistoryDao>()
    private val demoDataSource = mockk<DemoBusStopDataSource>()
    private lateinit var repository: BusStopRepositoryImpl

    private val center: LocationData = locationDataInstance()

    @BeforeEach
    fun setUp() {
        repository =
            BusStopRepositoryImpl(
                remoteDataSource = remoteDataSource,
                busStopCacheDao = busStopCacheDao,
                searchHistoryDao = searchHistoryDao,
                demoDataSource = demoDataSource,
            )
    }

    private fun stopAt(
        stopId: Long,
        eastMeters: Double,
    ): BusStop =
        BusStop(
            stopId = stopId,
            name = "Parada $stopId",
            locationData = center.offsetByMeters(northMeters = 0.0, eastMeters = eastMeters),
            markedAsFavorite = false,
            busLines = emptyList(),
            status = StopStatus.ACTIVA,
        )

    @Test
    @DisplayName("Debería devolver las paradas remotas ordenadas por cercanía y refrescar la caché")
    fun shouldReturnRemoteStopsSortedByDistanceAndRefreshCache() =
        runTest {
            val nearStop = stopAt(stopId = 1L, eastMeters = 50.0)
            val farStop = stopAt(stopId = 2L, eastMeters = 400.0)
            coEvery { remoteDataSource.findByLocation(center, any()) } returns listOf(farStop, nearStop)
            coEvery { busStopCacheDao.clearCache() } just Runs
            coEvery { busStopCacheDao.insertStops(any()) } just Runs

            val result = repository.getNearbyStops(center)

            assertEquals(NearbyStopsOrigin.REMOTE, result.origin)
            assertEquals(listOf(nearStop, farStop), result.stops)
            coVerify(exactly = 1) { busStopCacheDao.clearCache() }
            coVerify(exactly = 1) { busStopCacheDao.insertStops(any()) }
        }

    @Test
    @DisplayName("Debería degradar a la caché cuando falla la fuente remota y hay datos guardados")
    fun shouldDegradeToCacheWhenRemoteFailsAndCacheHasStops() =
        runTest {
            val cachedStop = stopAt(stopId = 3L, eastMeters = 120.0)
            coEvery { remoteDataSource.findByLocation(center, any()) } throws IOException("sin conexión")
            coEvery { busStopCacheDao.getCachedStopsList() } returns listOf(cachedStop.toCacheEntity())

            val result = repository.getNearbyStops(center)

            assertEquals(NearbyStopsOrigin.CACHE, result.origin)
            assertEquals(listOf(cachedStop.toCacheEntity().toDomain()), result.stops)
            coVerify(exactly = 0) { busStopCacheDao.clearCache() }
        }

    @Test
    @DisplayName("Debería degradar a las paradas de ejemplo cuando no hay remoto ni caché")
    fun shouldDegradeToDemoWhenRemoteFailsAndCacheIsEmpty() =
        runTest {
            val demoStop = stopAt(stopId = 4L, eastMeters = 80.0)
            coEvery { remoteDataSource.findByLocation(center, any()) } throws IOException("sin conexión")
            coEvery { busStopCacheDao.getCachedStopsList() } returns emptyList()
            coEvery { demoDataSource.getDemoStops(center, any()) } returns listOf(demoStop)

            val result = repository.getNearbyStops(center)

            assertEquals(NearbyStopsOrigin.DEMO, result.origin)
            assertEquals(listOf(demoStop), result.stops)
        }

    @Test
    @DisplayName("Debería degradar a demo cuando la zona no tiene paradas, sin tocar la caché")
    fun shouldDegradeToDemoWhenRemoteReturnsNoStops() =
        runTest {
            val demoStop = stopAt(stopId = 5L, eastMeters = 80.0)
            coEvery { remoteDataSource.findByLocation(center, any()) } returns emptyList()
            coEvery { demoDataSource.getDemoStops(center, any()) } returns listOf(demoStop)

            val result = repository.getNearbyStops(center)

            assertEquals(NearbyStopsOrigin.DEMO, result.origin)
            assertEquals(listOf(demoStop), result.stops)
            coVerify(exactly = 0) { busStopCacheDao.getCachedStopsList() }
        }

    @Test
    @DisplayName("Debería ignorar búsquedas vacías al guardar el historial")
    fun shouldIgnoreBlankQueriesWhenSavingSearchHistory() =
        runTest {
            repository.saveSearchQuery("   ")

            coVerify(exactly = 0) { searchHistoryDao.deleteSearch(any()) }
            coVerify(exactly = 0) { searchHistoryDao.insertSearch(any()) }
        }

    @Test
    @DisplayName("Debería guardar la búsqueda recortada y moverla al tope del historial")
    fun shouldSaveTrimmedQueryAndMoveItToTheTopOfHistory() =
        runTest {
            coEvery { searchHistoryDao.deleteSearch("96") } just Runs
            coEvery { searchHistoryDao.insertSearch(any()) } just Runs

            repository.saveSearchQuery(" 96 ")

            coVerify(exactly = 1) { searchHistoryDao.deleteSearch("96") }
            coVerify(exactly = 1) { searchHistoryDao.insertSearch(match { it.query == "96" }) }
        }

    @Test
    @DisplayName("Debería exponer el historial reciente como modelo de dominio")
    fun shouldExposeRecentSearchesAsDomainModel() =
        runTest {
            every { searchHistoryDao.getRecentSearches() } returns
                flowOf(
                    listOf(
                        SearchHistoryEntity(id = 7L, query = "96", timestamp = 123L),
                    ),
                )

            val searches = repository.getRecentSearches().first()

            assertEquals(listOf(SearchHistory(id = 7L, query = "96", timestamp = 123L)), searches)
        }
}
