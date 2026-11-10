package ar.edu.unlam.scaffoldingandroid3.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Obtiene la ubicación actual del dispositivo con las APIs del sistema (sin Google Play Services). */
class LocationRepo(
    private val context: Context,
) {
    fun hasPermission(): Boolean {
        val fine =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val coarse =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationEnabled(): Boolean {
        val manager = context.getSystemService(LocationManager::class.java) ?: return false
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    /** Devuelve la última ubicación conocida o espera una nueva. Puede devolver null si no hay fix. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): GeoPoint? {
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val provider =
            when {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                    LocationManager.NETWORK_PROVIDER
                }
                else -> return null
            }

        manager.getLastKnownLocation(provider)?.let { return it.toGeoPoint() }

        return suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                signal,
                ContextCompat.getMainExecutor(context),
            ) { location ->
                if (continuation.isActive) continuation.resume(location?.toGeoPoint())
            }
        }
    }

    private fun Location.toGeoPoint() = GeoPoint(latitude = latitude, longitude = longitude)
}
