package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GoogleMapView(
    userLocation: LocationData,
    stops: List<BusStop>,
    selectedStopId: Long?,
    onStopClick: (BusStop) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasApiKey = remember(context) { context.hasGoogleMapsApiKey() }

    // Sin API key el SDK de Google Maps cierra la app. En ese caso usamos el mapa esquemático.
    if (!hasApiKey) {
        NearbyStopsMap(
            userLocation = userLocation,
            stops = stops,
            selectedStopId = selectedStopId,
            onStopClick = onStopClick,
            modifier = modifier,
        )
        return
    }

    val userLatLng = remember(userLocation) { LatLng(userLocation.latitude.value, userLocation.longitude.value) }
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
        }

    LaunchedEffect(userLatLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = remember(primaryColor) { primaryColor.copy(alpha = 0.15f) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
    ) {
        // Círculo del radio de 1000m
        Circle(
            center = userLatLng,
            radius = 1000.0,
            fillColor = fillColor,
            strokeColor = primaryColor,
            strokeWidth = 3f,
        )

        // Marcadores de paradas cercanas
        stops.forEach { stop ->
            Marker(
                state =
                    remember(
                        stop.stopId,
                        stop.locationData,
                    ) { MarkerState(position = LatLng(stop.locationData.latitude.value, stop.locationData.longitude.value)) },
                title = stop.name,
                snippet = stop.busLines.joinToString(" · ") { line -> "Línea ${line.number}" },
                onClick = {
                    onStopClick(stop)
                    false
                },
            )
        }
    }
}

/** El SDK de Google Maps solo se puede crear si el manifest define la API key. */
private fun Context.hasGoogleMapsApiKey(): Boolean =
    try {
        @Suppress("DEPRECATION")
        val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        !info.metaData?.getString("com.google.android.geo.API_KEY").isNullOrBlank()
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
