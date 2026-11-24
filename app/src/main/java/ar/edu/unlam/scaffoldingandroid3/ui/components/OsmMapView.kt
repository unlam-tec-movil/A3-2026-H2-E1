package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color as AndroidColor
import org.osmdroid.util.GeoPoint as OsmGeoPoint

private val SELECTED_CLUSTER_COLOR = AndroidColor.parseColor("#FF9800")

@Composable
fun OsmMapView(
    userLocation: LocationData,
    stops: List<BusStop>,
    selectedStopId: Long?,
    onStopClick: (BusStop) -> Unit,
    modifier: Modifier = Modifier,
    selectedClusterStopIds: Set<Long>? = null,
    onClusterClick: (List<BusStop>) -> Unit = {},
    focusTrigger: Int = 0,
    userFocusTrigger: Int = 0,
    routePoints: List<LocationData>? = null,
    isNavigationMode: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentStops by rememberUpdatedState(stops)
    val currentSelectedStopId by rememberUpdatedState(selectedStopId)
    val currentSelectedClusterStopIds by rememberUpdatedState(selectedClusterStopIds)
    val currentUserLocation by rememberUpdatedState(userLocation)
    val currentOnStopClick by rememberUpdatedState(onStopClick)
    val currentOnClusterClick by rememberUpdatedState(onClusterClick)
    val currentRoutePoints by rememberUpdatedState(routePoints)
    val currentIsNavigationMode by rememberUpdatedState(isNavigationMode)

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

    val normalClusterCache = remember(context, primaryColor) { mutableMapOf<Int, Drawable>() }
    val selectedClusterCache = remember(context) { mutableMapOf<Int, Drawable>() }

    fun getClusterDrawable(
        count: Int,
        isSelected: Boolean,
    ): Drawable =
        (if (isSelected) selectedClusterCache else normalClusterCache).getOrPut(count) {
            createClusterBitmap(
                context = context,
                count = count,
                backgroundColor = if (isSelected) SELECTED_CLUSTER_COLOR else primaryColor,
            )
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

    fun refreshOverlays(map: MapView) {
        val userOsmPoint = OsmGeoPoint(currentUserLocation.latitude.value, currentUserLocation.longitude.value)

        map.overlays.clear()

        if (currentIsNavigationMode) {
            val points = currentRoutePoints
            if (points != null && points.size >= 2) {
                val polyline =
                    Polyline(map).apply {
                        val geoPoints =
                            points.map { OsmGeoPoint(it.latitude.value, it.longitude.value) }
                        setPoints(geoPoints)
                        outlinePaint.color = primaryColor
                        outlinePaint.strokeWidth = 14f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.strokeJoin = Paint.Join.ROUND
                        infoWindow = null
                    }
                map.overlays.add(polyline)
            }

            val userMarker =
                Marker(map).apply {
                    position = userOsmPoint
                    title = "Tu ubicación"
                    icon = userRedIconDrawable
                    infoWindow = null
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
            map.overlays.add(userMarker)

            val selectedStop = currentStops.firstOrNull { it.stopId == currentSelectedStopId }
            if (selectedStop != null) {
                val stopPoint =
                    OsmGeoPoint(
                        selectedStop.locationData.latitude.value,
                        selectedStop.locationData.longitude.value,
                    )
                val stopMarker =
                    Marker(map).apply {
                        position = stopPoint
                        title = selectedStop.name
                        snippet = selectedStop.busLines.joinToString(" · ") { "Línea ${it.number}" }
                        icon = selectedBlueIconDrawable
                        infoWindow = null
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            currentOnStopClick(selectedStop)
                            true
                        }
                    }
                map.overlays.add(stopMarker)
            }

            map.postInvalidate()
            return
        }

        // Radio de 1000m alrededor del usuario (Círculo Polygon)
        val circleOverlay =
            Polygon(map).apply {
                points = Polygon.pointsAsCircle(userOsmPoint, 1000.0)
                fillPaint.color = fillColor
                outlinePaint.color = primaryColor
                outlinePaint.strokeWidth = 3f
                infoWindow = null
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

        // Proyección y clustering de paradas (eximiendo la parada seleccionada)
        val tempPoint = Point()
        val density = context.resources.displayMetrics.density
        val radiusPx = 60f * density

        val clusterItems =
            clusterBusStops(
                stops = currentStops,
                toScreenPixels = { lat, lon ->
                    val osmP = OsmGeoPoint(lat, lon)
                    val proj = map.projection
                    if (proj != null) {
                        proj.toPixels(osmP, tempPoint)
                        ScreenPoint(tempPoint.x.toFloat(), tempPoint.y.toFloat())
                    } else {
                        ScreenPoint((lon * 1000).toFloat(), (lat * 1000).toFloat())
                    }
                },
                radiusPixels = radiusPx,
                selectedStopId = currentSelectedStopId,
            )

        clusterItems.forEach { item ->
            when (item) {
                is ClusterItem.Single -> {
                    val stop = item.stop
                    val stopPoint = OsmGeoPoint(stop.locationData.latitude.value, stop.locationData.longitude.value)
                    val isSelected = stop.stopId == currentSelectedStopId
                    val stopMarker =
                        Marker(map).apply {
                            position = stopPoint
                            title = stop.name
                            snippet = stop.busLines.joinToString(" · ") { "Línea ${it.number}" }
                            icon = if (isSelected) selectedBlueIconDrawable else blueIconDrawable
                            infoWindow = null
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                currentOnStopClick(stop)
                                true
                            }
                        }
                    map.overlays.add(stopMarker)
                }

                is ClusterItem.Group -> {
                    val clusterPoint = OsmGeoPoint(item.centerLat, item.centerLon)
                    val selectedIds = currentSelectedClusterStopIds
                    val isClusterSelected =
                        selectedIds != null &&
                            item.stops.any { it.stopId in selectedIds }

                    val clusterMarker =
                        Marker(map).apply {
                            position = clusterPoint
                            title = "Grupo de ${item.stops.size} paradas"
                            icon = getClusterDrawable(item.stops.size, isClusterSelected)
                            infoWindow = null
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { _, _ ->
                                currentOnClusterClick(item.stops)
                                true
                            }
                        }
                    map.overlays.add(clusterMarker)
                }
            }
        }

        map.postInvalidate()
    }

    LaunchedEffect(mapView) {
        val mapListener =
            object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean = false

                override fun onZoom(event: ZoomEvent?): Boolean {
                    mapView.post {
                        refreshOverlays(mapView)
                    }
                    return false
                }
            }
        mapView.addMapListener(mapListener)
    }

    LaunchedEffect(selectedStopId, focusTrigger) {
        if (selectedStopId != null) {
            val selectedStop = stops.firstOrNull { it.stopId == selectedStopId }
            if (selectedStop != null) {
                val stopOsmPoint = OsmGeoPoint(selectedStop.locationData.latitude.value, selectedStop.locationData.longitude.value)
                mapView.controller.animateTo(stopOsmPoint, 19.5, 1000L)
            }
        }
    }

    LaunchedEffect(userFocusTrigger) {
        if (userFocusTrigger > 0) {
            val userOsmPoint = OsmGeoPoint(userLocation.latitude.value, userLocation.longitude.value)
            mapView.controller.animateTo(userOsmPoint, 17.0, 1000L)
        }
    }

    LaunchedEffect(isNavigationMode, routePoints) {
        refreshOverlays(mapView)
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
            factory = {
                mapView.addOnFirstLayoutListener { _, _, _, _, _ ->
                    refreshOverlays(mapView)
                }
                mapView
            },
            modifier = Modifier.matchParentSize().clipToBounds(),
            update = { map ->
                refreshOverlays(map)
            },
        )
    }
}

private fun createClusterBitmap(
    context: Context,
    count: Int,
    backgroundColor: Int,
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (36 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val radius = sizePx / 2f
    val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
    canvas.drawCircle(radius, radius, radius - 1f, paint)

    val strokePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
    canvas.drawCircle(radius, radius, radius - (1.5f * density), strokePaint)

    val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 13f * density
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    val text = if (count > 99) "99+" else count.toString()
    val yPos = radius - ((textPaint.descent() + textPaint.ascent()) / 2)
    canvas.drawText(text, radius, yPos, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}
