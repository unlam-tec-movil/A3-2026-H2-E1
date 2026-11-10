package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.CompassUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.CompassViewModel

/**
 * H02: el pasajero se orienta hacia una parada.
 * Muestra una flecha que apunta a la parada y se actualiza al girar el teléfono.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    onBack: () -> Unit,
    viewModel: CompassViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted -> viewModel.onPermissionResult(granted) }

    fun resolveLocationPermission() {
        val fine =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (fine) {
            viewModel.locateUser()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(Unit) { resolveLocationPermission() }

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
            when (val state = uiState) {
                CompassUiState.SensorUnavailable -> {
                    CompassMessage(
                        message = "La brújula no está disponible en este dispositivo.",
                        actionLabel = "Ver parada en el mapa",
                        onAction = onBack,
                    )
                }

                CompassUiState.PermissionDenied -> {
                    CompassMessage(
                        message = "Necesitamos tu ubicación para indicarte la dirección a la parada.",
                        actionLabel = "Permitir ubicación",
                        onAction = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    )
                }

                CompassUiState.Locating -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Obteniendo tu ubicación…")
                    }
                }

                CompassUiState.LocationUnavailable -> {
                    CompassMessage(
                        message = "No pudimos obtener tu ubicación.",
                        actionLabel = "Reintentar",
                        onAction = { viewModel.locateUser() },
                    )
                }

                is CompassUiState.Ready -> {
                    CompassContent(
                        stopName = viewModel.stopName,
                        distanceMeters = state.distanceMeters,
                        targetBearing = state.targetBearingDegrees,
                        deviceAzimuth = state.deviceAzimuthDegrees,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassContent(
    stopName: String,
    distanceMeters: Double,
    targetBearing: Double,
    deviceAzimuth: Float,
) {
    // Diferencia más corta entre el rumbo a la parada y hacia dónde apunta el teléfono.
    val delta = (targetBearing - deviceAzimuth.toDouble() + 360.0) % 360.0
    val targetRotation = (if (delta > 180.0) delta - 360.0 else delta).toFloat()
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
