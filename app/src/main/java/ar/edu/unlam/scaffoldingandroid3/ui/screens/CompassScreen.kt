package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.data.LocationRepo
import ar.edu.unlam.scaffoldingandroid3.data.OrientationRepo
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_TIMEOUT_MILLIS = 10_000L

/**
 * H02: el pasajero se orienta hacia una parada.
 * Muestra una flecha que apunta a la parada y se actualiza al girar el teléfono.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    stopName: String,
    stopLocation: GeoPoint,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val orientationRepo = remember(context) { OrientationRepo(context) }
    val locationRepo = remember(context) { LocationRepo(context) }

    val hasSensor = remember { orientationRepo.hasSensor() }
    val azimuthFlow = remember { orientationRepo.getAzimuth() }
    val deviceAzimuth by azimuthFlow.collectAsState(initial = 0f)

    var hasPermission by remember { mutableStateOf(locationRepo.hasPermission()) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locating by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasPermission, attempt) {
        if (!hasPermission) return@LaunchedEffect
        locating = true
        currentLocation = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { locationRepo.currentLocation() }
        locating = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Orientarme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val location = currentLocation
            when {
                !hasSensor ->
                    CompassMessage(
                        message = "La brújula no está disponible en este dispositivo.",
                        actionLabel = "Ver parada en el mapa",
                        onAction = onBack,
                    )

                !hasPermission ->
                    CompassMessage(
                        message = "Necesitamos tu ubicación para indicarte la dirección a la parada.",
                        actionLabel = "Permitir ubicación",
                        onAction = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                    )

                locating ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Obteniendo tu ubicación…")
                    }

                location == null ->
                    CompassMessage(
                        message = "No pudimos obtener tu ubicación.",
                        actionLabel = "Reintentar",
                        onAction = { attempt++ },
                    )

                else ->
                    CompassContent(
                        stopName = stopName,
                        distanceMeters = location.distanceTo(stopLocation),
                        targetBearing = location.bearingTo(stopLocation),
                        deviceAzimuth = deviceAzimuth,
                    )
            }
        }
    }
}

@Composable
private fun CompassContent(
    stopName: String,
    distanceMeters: Double,
    targetBearing: Float,
    deviceAzimuth: Float,
) {
    // Diferencia más corta entre el rumbo a la parada y hacia dónde apunta el teléfono.
    val delta = (targetBearing - deviceAzimuth + 360f) % 360f
    val targetRotation = if (delta > 180f) delta - 360f else delta
    val animatedAngle by animateFloatAsState(
        targetValue = targetRotation,
        label = "arrowAngle",
    )

    Text(
        text = stopName,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(32.dp))
    Icon(
        imageVector = Icons.Filled.KeyboardArrowUp,
        contentDescription = "Dirección a la parada",
        tint = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .size(160.dp)
                .rotate(animatedAngle),
    )
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = "Distancia en línea recta: ${distanceMeters.toInt()} m",
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun CompassMessage(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onAction) {
        Text(text = actionLabel)
    }
}
