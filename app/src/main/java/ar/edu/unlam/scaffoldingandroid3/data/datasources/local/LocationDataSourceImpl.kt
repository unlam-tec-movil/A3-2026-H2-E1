package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import ar.edu.unlam.scaffoldingandroid3.data.mapper.LocationMapper
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationDataSourceImpl
    @Inject
    constructor(
        private val locationProvider: FusedLocationProviderClient,
        private val mapper: LocationMapper,
        @ApplicationContext private val context: Context,
    ) : LocationDataSource {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override suspend fun getLastKnowLocation(): LocationData? =
            runCatching {
                locationProvider.lastLocation.await()
            }.getOrNull()?.let { mapper.toDomain(it) }

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override suspend fun isLocationEnabled(): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return isGpsEnabled || isNetworkEnabled
        }
    }
