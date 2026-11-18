package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.content.Context
import android.location.Location
import android.location.LocationManager
import ar.edu.unlam.scaffoldingandroid3.data.datasources.mapper.LocationMapper
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.locationDataInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationDataSourceImplTest {
    private val locationProvider = mockk<FusedLocationProviderClient>()
    private val mapper = mockk<LocationMapper>()
    private val context = mockk<Context>()
    private val locationManager = mockk<LocationManager>()
    private val dataSource = LocationDataSourceImpl(locationProvider, mapper, context)

    @Test
    fun `given a known last location, when getting it, then returns the mapped domain location`() =
        runTest {
            // Given
            val rawLocation = mockk<Location>()
            val expected = locationDataInstance()
            every { locationProvider.lastLocation } returns Tasks.forResult(rawLocation)
            every { mapper.toDomain(rawLocation) } returns expected

            // When
            val actual = dataSource.getLastKnowLocation()

            // Then
            assertEquals(expected, actual)
            verify(exactly = 1) { mapper.toDomain(rawLocation) }
        }

    @Test
    fun `given no last location, when getting it, then returns null without mapping`() =
        runTest {
            // Given
            every { locationProvider.lastLocation } returns Tasks.forResult(null)

            // When
            val actual = dataSource.getLastKnowLocation()

            // Then
            assertNull(actual)
            verify(exactly = 0) { mapper.toDomain(any()) }
        }

    @Test
    fun `given the provider fails, when getting the last location, then returns null`() =
        runTest {
            // Given
            every { locationProvider.lastLocation } returns Tasks.forException(IllegalStateException())

            // When
            val actual = dataSource.getLastKnowLocation()

            // Then
            assertNull(actual)
        }

    @Test
    fun `given only GPS enabled, when checking location, then returns true`() =
        runTest {
            // Given
            givenProviders(gps = true, network = false)

            // When
            val enabled = dataSource.isLocationEnabled()

            // Then
            assertTrue(enabled)
        }

    @Test
    fun `given only network enabled, when checking location, then returns true`() =
        runTest {
            // Given
            givenProviders(gps = false, network = true)

            // When
            val enabled = dataSource.isLocationEnabled()

            // Then
            assertTrue(enabled)
        }

    @Test
    fun `given no provider enabled, when checking location, then returns false`() =
        runTest {
            // Given
            givenProviders(gps = false, network = false)

            // When
            val enabled = dataSource.isLocationEnabled()

            // Then
            assertFalse(enabled)
        }

    private fun givenProviders(
        gps: Boolean,
        network: Boolean,
    ) {
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns gps
        every { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } returns network
    }
}
