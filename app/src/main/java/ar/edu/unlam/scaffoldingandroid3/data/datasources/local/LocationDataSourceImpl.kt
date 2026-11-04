package ar.edu.unlam.mobile.movil22026h2.data.datasources.local

import android.Manifest
import androidx.annotation.RequiresPermission
import ar.edu.unlam.mobile.movil22026h2.data.datasources.mapper.LocationMapper
import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
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
 *
 * @param locationProvider el cliente de proveedor de ubicación de Google para obtener
 *                         la última ubicación del dispositivo.
 * @param mapper el mapeador utilizado para convertir datos de ubicación brutos al modelo
 *               de dominio [LocationData].
 */
class LocationDataSourceImpl
    @Inject
    constructor(
        private val locationProvider: FusedLocationProviderClient,
        private val mapper: LocationMapper,
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
    }
