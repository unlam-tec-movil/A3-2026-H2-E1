package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.helper.CameraPhotoLauncher
import ar.edu.unlam.scaffoldingandroid3.ui.helper.ContextCameraPermissionChecker
import ar.edu.unlam.scaffoldingandroid3.ui.helper.ContextPhotoFileFactory

@Composable
fun PhotoReportCameraButton(
    stopId: Long,
    onPhotoTaken: (String, Long) -> Unit,
    onPermissionDenied: () -> Unit,
    onCancelled: () -> Unit,
) {
    val context = LocalContext.current
    val photoLauncher =
        remember(context) {
            CameraPhotoLauncher(
                permissionChecker = ContextCameraPermissionChecker(context),
                photoFileFactory = ContextPhotoFileFactory(context),
            )
        }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            when (val result = photoLauncher.consumeResult(saved)) {
                is PhotoCaptureResult.Taken -> onPhotoTaken(result.photoUri, result.stopId)
                PhotoCaptureResult.Cancelled -> onCancelled()
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                photoLauncher.launchCamera(
                    stopId,
                    photoLauncher,
                    takePictureLauncher,
                    onCancelled,
                )
            } else {
                onPermissionDenied()
            }
        }

    DisposableEffect(photoLauncher) {
        onDispose { photoLauncher.discardPending() }
    }

    TextButton(
        onClick = {
            if (photoLauncher.isPermissionGranted()) {
                photoLauncher.launchCamera(
                    stopId,
                    photoLauncher,
                    takePictureLauncher,
                    onCancelled,
                )
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    ) {
        Text(stringResource(R.string.add_photo_report))
    }
}

sealed interface PhotoCaptureResult {
    data class Taken(
        val photoUri: String,
        val stopId: Long,
    ) : PhotoCaptureResult

    data object Cancelled : PhotoCaptureResult
}
