package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.MapViewModel

@Composable
fun LocationPermissionManager(
    viewModel: MapViewModel,
    onRequestPermissionsResult: () -> Unit,
    resolveRequest: Int,
) {
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            viewModel.onPermissionResult(
                fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true,
                coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true,
            )
            onRequestPermissionsResult()
        }

    fun resolveLocationPermission() {
        val fineLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocation && !coarseLocation) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else {
            viewModel.loadUserLocationAndStops(isApproximate = coarseLocation && !fineLocation)
        }
    }

    LaunchedEffect(Unit) { resolveLocationPermission() }

    LaunchedEffect(resolveRequest) {
        if (resolveRequest > 0) {
            resolveLocationPermission()
        }
    }
}
