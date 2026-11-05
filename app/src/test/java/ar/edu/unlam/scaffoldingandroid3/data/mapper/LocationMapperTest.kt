package ar.edu.unlam.scaffoldingandroid3.data.mapper

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.data.datasources.mapper.LocationMapper
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName

class LocationMapperTest {
    private val locationMapper = LocationMapper()

    @Test
    @DisplayName("Debería mapear correctamente una lectura del proveedor de ubicación a un objeto de dominio")
    fun shouldMapFusedLocationProviderClientOutputToLocationDataInstance() {
        val expectedLocation = TestDataFactory.locationDataInstance()

        val mockAndroidLocation =
            mockk<Location> {
                every { latitude } returns expectedLocation.latitude.value
                every { longitude } returns expectedLocation.longitude.value
            }

        val result = locationMapper.toDomain(mockAndroidLocation)

        assertEquals(expectedLocation.latitude.value, result.latitude.value)
        assertEquals(expectedLocation.longitude.value, result.longitude.value)
    }
}
