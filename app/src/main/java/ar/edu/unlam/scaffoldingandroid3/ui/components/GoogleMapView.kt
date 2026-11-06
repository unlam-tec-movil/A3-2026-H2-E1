package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
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
    userLocation: GeoPoint,
    stops: List<BusStop>,
    selectedStopId: String?,
    onStopClick: (BusStop) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userLatLng = remember(userLocation) { LatLng(userLocation.latitude, userLocation.longitude) }
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
                        stop.id,
                        stop.location,
                    ) { MarkerState(position = LatLng(stop.location.latitude, stop.location.longitude)) },
                title = stop.name,
                snippet = stop.lines.joinToString(" · ") { it.code },
                onClick = {
                    onStopClick(stop)
                    false
                },
            )
        }
    }
}
