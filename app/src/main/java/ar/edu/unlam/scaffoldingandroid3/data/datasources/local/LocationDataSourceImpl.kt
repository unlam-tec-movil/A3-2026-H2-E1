package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import ar.edu.unlam.scaffoldingandroid3.data.datasources.mapper.LocationMapper
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación de [LocationDataSource] que utiliza el Fused Location Provider de Google
 * para obtener la última ubicación conocida del dispositivo.
 *
 * Esta implementación:
 * - Utiliza [FusedLocationProviderClient] para acceder al servicio de ubicación unificado
 * - Convierte los datos de ubicación brutos al modelo de dominio mediante [LocationMapper]
 * - Maneja permisos de ubicación finos y gruesos mediante [RequiresPermission]
 * - Utiliza [Context.LOCATION_SERVICE] para verificar la disponibilidad del servicio de ubicación
 *
 * @param locationProvider el cliente de proveedor de ubicación de Google para obtener
 *                         la última ubicación del dispositivo.
 * @param mapper el mapeador utilizado para convertir datos de ubicación brutos al modelo
 *               de dominio [LocationData].
 * @param context el contexto de la aplicación utilizado para obtener el servicio de
 *                ubicación [LocationManager].
 */
class LocationDataSourceImpl
    @Inject
    constructor(
        private val locationProvider: FusedLocationProviderClient,
        private val mapper: LocationMapper,
        @ApplicationContext private val context: Context,
    ) : LocationDataSource {
        /**
         * Obtiene la última ubicación conocida del dispositivo.
         *
         * Esta función:
         * 1. Solicita la última ubicación mediante [FusedLocationProviderClient.lastLocation]
         * 2. Convierte los datos brutos al modelo de dominio usando [LocationMapper.toDomain]
         * 3. Devuelve `null` si no hay ubicación disponible o si ocurre un error
         *
         * **Requisitos de permisos**: Requiere ambos permisos de ubicación:
         * - [Manifest.permission.ACCESS_FINE_LOCATION] para precisión de GPS
         * - [Manifest.permission.ACCESS_COARSE_LOCATION] para ubicación aproximada
         *
         * **Comportamiento**:
         * - Si no hay permisos, devuelve `null`
         * - Si el Fused Location Provider no está disponible, devuelve `null`
         * - Si la ubicación no está disponible, devuelve `null`
         * - Si ocurre una excepción durante la obtención, captura y devuelve `null`
         *
         * @return una instancia de [LocationData] con la última ubicación conocida, o `null`
         *         si no es posible obtener la ubicación.
         */
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override suspend fun getLastKnowLocation(): LocationData? =
            runCatching {
                locationProvider.lastLocation.await()
            }.getOrNull()?.let { mapper.toDomain(it) }

        /**
         * Verifica si la ubicación está habilitada en el dispositivo.
         *
         * Esta función:
         * 1. Obtiene el servicio de ubicación a través de [Context.LOCATION_SERVICE]
         * 2. Verifica si el proveedor GPS está habilitado mediante [android.provider.Settings.Secure.isLocationProviderEnabled]
         * 3. Verifica si el proveedor de red está habilitado mediante [android.provider.Settings.Secure.isLocationProviderEnabled]
         * 4. Devuelve `true` si al menos uno de los proveedores está habilitado
         *
         * **Requisitos de permisos**: Requiere ambos permisos de ubicación:
         * - [Manifest.permission.ACCESS_FINE_LOCATION] para precisión de GPS
         * - [Manifest.permission.ACCESS_COARSE_LOCATION] para ubicación aproximada
         *
         * **Comportamiento**:
         * - Si no hay permisos, devuelve `false`
         * - Si el GPS está habilitado, devuelve `true`
         * - Si la red está habilitada, devuelve `true`
         * - Si ninguno está habilitado, devuelve `false`
         *
         * @return `true` si al menos uno de los proveedores de ubicación (GPS o red)
         *         está habilitado, `false` si no hay permisos o si ningún proveedor
         *         está disponible.
         */
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override suspend fun isLocationEnabled(): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return isGpsEnabled || isNetworkEnabled
        }
    }
