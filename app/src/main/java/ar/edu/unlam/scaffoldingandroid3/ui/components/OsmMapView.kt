package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color as AndroidColor
import org.osmdroid.util.GeoPoint as OsmGeoPoint

@Composable
fun OsmMapView(
    userLocation: LocationData,
    stops: List<BusStop>,
    selectedStopId: Long?,
    onStopClick: (BusStop) -> Unit,
    modifier: Modifier = Modifier,
    focusTrigger: Int = 0,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val fillColor =
        MaterialTheme.colorScheme.primary
            .copy(alpha = 0.15f)
            .toArgb()

    val blueIconDrawable =
        remember(context) {
            ContextCompat.getDrawable(
                context,
                R.drawable.bus_stop_pointer,
            )
        }

    val selectedBlueIconDrawable =
        remember(context) {
            ContextCompat.getDrawable(
                context,
                R.drawable.bus_stop_pointer_selected,
            )
        }

    val userRedIconDrawable =
        remember(context) {
            val defaultMarkerIcon = Marker(MapView(context)).icon
            defaultMarkerIcon?.mutate()?.apply {
                setTint(AndroidColor.parseColor("#D32F2F")) // Rojo distintivo para ubicación del usuario
            }
        }

    val mapView =
        remember {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(15.5)
                controller.setCenter(OsmGeoPoint(userLocation.latitude.value, userLocation.longitude.value))
            }
        }

    LaunchedEffect(selectedStopId, focusTrigger) {
        if (selectedStopId != null) {
            val selectedStop = stops.firstOrNull { it.stopId == selectedStopId }
            if (selectedStop != null) {
                val stopOsmPoint = OsmGeoPoint(selectedStop.locationData.latitude.value, selectedStop.locationData.longitude.value)
                mapView.controller.animateTo(stopOsmPoint, 17.0, 1000L)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        mapView.onPause()
                    }

                    else -> {}
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.matchParentSize().clipToBounds(),
            update = { map ->
                val userOsmPoint = OsmGeoPoint(userLocation.latitude.value, userLocation.longitude.value)

                map.overlays.clear()

                // Radio de 1000m alrededor del usuario (Círculo Polygon)
                val circleOverlay =
                    Polygon(map).apply {
                        points = Polygon.pointsAsCircle(userOsmPoint, 1000.0)
                        fillPaint.color = fillColor
                        outlinePaint.color = primaryColor
                        outlinePaint.strokeWidth = 3f
                    }
                map.overlays.add(circleOverlay)

                // Marcador distintivo ROJO únicamente para la ubicación del usuario
                val userMarker =
                    Marker(map).apply {
                        position = userOsmPoint
                        title = "Tu ubicación"
                        icon = userRedIconDrawable
                        infoWindow = null
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                map.overlays.add(userMarker)

                // Marcadores de las paradas
                stops.forEach { stop ->
                    val stopPoint = OsmGeoPoint(stop.locationData.latitude.value, stop.locationData.longitude.value)
                    val isSelected = stop.stopId == selectedStopId
                    val stopMarker =
                        Marker(map).apply {
                            position = stopPoint
                            title = stop.name
                            snippet = stop.busLines.joinToString(" · ") { "Línea ${it.number}" }
                            icon = if (isSelected) selectedBlueIconDrawable else blueIconDrawable
                            infoWindow = null
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                onStopClick(stop)
                                true
                            }
                        }
                    map.overlays.add(stopMarker)
                }

                map.postInvalidate()
            },
        )

        // Botón Flotante (FAB) para recentrar el mapa en la ubicación del usuario aplicando zoom
        FloatingActionButton(
            onClick = {
                val userOsmPoint = OsmGeoPoint(userLocation.latitude.value, userLocation.longitude.value)
                mapView.controller.animateTo(userOsmPoint, 17.0, 1000L)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "Centrar en mi ubicación",
            )
        }
    }
}
