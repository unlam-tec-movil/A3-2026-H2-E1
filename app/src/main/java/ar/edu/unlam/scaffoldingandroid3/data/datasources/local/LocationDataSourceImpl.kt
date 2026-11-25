package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import ar.edu.unlam.scaffoldingandroid3.data.mapper.LocationMapper
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación de la fuente de datos para ubicación del dispositivo.
 *
 * Esta clase proporciona la implementación concreta de [LocationDataSource], encargada de
 * obtener los datos de ubicación del dispositivo utilizando el Fused Location Provider de
 * Google Play Services. Gestiona el acceso a las últimas ubicaciones conocidas y verifica
 * el estado del servicio de ubicación.
 *
 * **Funcionalidad**:
 * - Obtiene la última ubicación conocida del dispositivo mediante el Fused Location Provider
 * - Verifica si los proveedores de ubicación (GPS y red) están habilitados
 * - Mapea los datos de ubicación brutos al modelo de dominio [LocationData]
 *
 * **Dependencias**:
 * - Requiere [FusedLocationProviderClient] para acceder al Fused Location Provider
 * - Utiliza [LocationMapper] para mapear los datos de ubicación del dispositivo al modelo de dominio
 * - Necesita acceso al contexto de la aplicación para interactuar con [LocationManager]
 *
 * **Permisos**:
 * - Requiere permisos de ubicación fina y gruesa para obtener la última ubicación conocida
 * - Verifica el estado de los proveedores de ubicación (GPS y red)
 *
 * @see LocationDataSource
 * @see LocationMapper
 * @see FusedLocationProviderClient
 * @see LocationManager
 */
class LocationDataSourceImpl
    @Inject
    constructor(
        /**
         * Cliente del Fused Location Provider de Google Play Services.
         *
         * Utilizado para obtener la última ubicación conocida del dispositivo de manera
         * eficiente y unificada, combinando datos de múltiples fuentes de ubicación
         * (GPS, redes celulares, Wi-Fi, etc.).
         *
         * @see FusedLocationProviderClient
         */
        private val locationProvider: FusedLocationProviderClient,
        /**
         * Mapeador de datos de ubicación.
         *
         * Utilizado para convertir los datos de ubicación del dispositivo (Model) al modelo
         * de dominio (Domain) de la aplicación.
         *
         * @see LocationMapper
         */
        private val mapper: LocationMapper,
        /**
         * Contexto de la aplicación.
         *
         * Utilizado para acceder al [LocationManager] y verificar el estado de los
         * proveedores de ubicación (GPS y red).
         *
         * @see Context
         * @see LocationManager
         */
        @ApplicationContext private val context: Context,
    ) : LocationDataSource {
        /**
         * Obtiene la última ubicación conocida del dispositivo.
         *
         * Esta función consulta al Fused Location Provider para obtener la última ubicación
         * registrada por el dispositivo, incluyendo las coordenadas de latitud y longitud,
         * así como la hora del último registro de ubicación.
         *
         * **Comportamiento**:
         * - Retorna los datos de ubicación si el dispositivo ha registrado una ubicación
         * - Retorna null si no hay datos de ubicación disponibles o si el acceso falla
         *
         * **Requisitos**:
         * - Requiere permisos de ubicación fina y gruesa ([Manifest.permission.ACCESS_FINE_LOCATION], [Manifest.permission.ACCESS_COARSE_LOCATION])
         * - Requiere que el Fused Location Provider esté disponible
         *
         * @return los datos de ubicación más recientes, o null si no hay datos disponibles o si el acceso falla.
         * @see LocationData
         * @see Manifest.permission.ACCESS_FINE_LOCATION
         * @see Manifest.permission.ACCESS_COARSE_LOCATION
         */
        @RequiresPermission(
            allOf = [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ],
        )
        override suspend fun getLastKnowLocation(): LocationData? =
            runCatching {
                locationProvider.lastLocation.await()
            }.getOrNull()?.let { mapper.toDomain(it) }

        @RequiresPermission(
            allOf = [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ],
        )
        override fun getLocationUpdates(
            intervalMillis: Long,
            minDistanceMeters: Float,
        ): Flow<LocationData> =
            callbackFlow {
                val locationRequest =
                    LocationRequest
                        .Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
                        .setMinUpdateDistanceMeters(minDistanceMeters)
                        .build()

                val locationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                trySend(mapper.toDomain(location))
                            }
                        }
                    }

                try {
                    locationProvider.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper(),
                    )
                } catch (e: Exception) {
                    close(e)
                }

                awaitClose {
                    locationProvider.removeLocationUpdates(locationCallback)
                }
            }

        /**
         * Verifica si el servicio de ubicación está habilitado en el dispositivo.
         *
         * Esta función consulta al [LocationManager] para determinar si los proveedores de
         * ubicación (GPS y red) están habilitados y disponibles para usar.
         *
         * **Comportamiento**:
         * - Verifica si el proveedor GPS ([LocationManager.GPS_PROVIDER]) está habilitado
         * - Verifica si el proveedor de red ([LocationManager.NETWORK_PROVIDER]) está habilitado
         * - Retorna true si al menos uno de los proveedores está habilitado
         * - Retorna false si ambos proveedores están deshabilitados
         *
         * @return true si al menos un proveedor de ubicación (GPS o red) está habilitado,
         * false si ambos proveedores están deshabilitados.
         * @see LocationManager
         * @see LocationManager.GPS_PROVIDER
         * @see LocationManager.NETWORK_PROVIDER
         */
        @RequiresPermission(
            allOf = [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ],
        )
        override suspend fun isLocationEnabled(): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return isGpsEnabled || isNetworkEnabled
        }
    }
