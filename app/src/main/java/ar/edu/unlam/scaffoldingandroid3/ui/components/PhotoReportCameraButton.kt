package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ar.edu.unlam.scaffoldingandroid3.R
import java.io.File
import java.io.IOException

private const val PHOTO_REPORTS_DIRECTORY = "photo_reports"
private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

internal data class PhotoFile(
    val file: File,
    val uri: Uri,
)

internal interface CameraPermissionChecker {
    fun isGranted(): Boolean
}

internal interface PhotoFileFactory {
    fun create(): PhotoFile?

    fun delete(file: File)
}

internal sealed interface PhotoCaptureResult {
    data class Taken(
        val photoUri: String,
        val stopId: Long,
    ) : PhotoCaptureResult

    data object Cancelled : PhotoCaptureResult
}

internal class CameraPhotoLauncher(
    private val permissionChecker: CameraPermissionChecker,
    private val photoFileFactory: PhotoFileFactory,
) {
    private var pending: PendingPhoto? = null

    fun isPermissionGranted(): Boolean = permissionChecker.isGranted()

    fun prepare(stopId: Long): Uri? {
        val photoFile = photoFileFactory.create() ?: return null
        pending =
            PendingPhoto(
                file = photoFile.file,
                uri = photoFile.uri,
                stopId = stopId,
            )
        return photoFile.uri
    }

    fun consumeResult(saved: Boolean): PhotoCaptureResult {
        val capture = pending
        pending = null
        if (saved && capture != null) {
            return PhotoCaptureResult.Taken(
                photoUri = capture.uri.toString(),
                stopId = capture.stopId,
            )
        }
        capture?.let { photoFileFactory.delete(it.file) }
        return PhotoCaptureResult.Cancelled
    }

    fun discardPending() {
        pending?.let { photoFileFactory.delete(it.file) }
        pending = null
    }

    private data class PendingPhoto(
        val file: File,
        val uri: Uri,
        val stopId: Long,
    )
}

private class ContextCameraPermissionChecker(
    private val context: Context,
) : CameraPermissionChecker {
    override fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
}

private class ContextPhotoFileFactory(
    private val context: Context,
) : PhotoFileFactory {
    override fun create(): PhotoFile? {
        val picturesDir =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: return null
        val directory = File(picturesDir, PHOTO_REPORTS_DIRECTORY)
        if (!directory.isDirectory && !directory.mkdirs()) return null
        val file =
            try {
                File.createTempFile("report_", ".jpg", directory)
            } catch (_: IOException) {
                return null
            }
        val uri =
            FileProvider.getUriForFile(
                context,
                context.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX,
                file,
            )
        return PhotoFile(file = file, uri = uri)
    }

    override fun delete(file: File) {
        file.delete()
    }
}

@Composable
fun PhotoReportCameraButton(
    stopId: Long,
    onPhotoTaken: (String, Long) -> Unit,
    onPermissionDenied: () -> Unit,
    onCancelled: () -> Unit,
) {
    val context = LocalContext.current
    val cameraPhotoLauncher =
        remember(context) {
            CameraPhotoLauncher(
                permissionChecker = ContextCameraPermissionChecker(context),
                photoFileFactory = ContextPhotoFileFactory(context),
            )
        }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            when (val result = cameraPhotoLauncher.consumeResult(saved)) {
                is PhotoCaptureResult.Taken -> onPhotoTaken(result.photoUri, result.stopId)
                PhotoCaptureResult.Cancelled -> onCancelled()
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera(stopId, cameraPhotoLauncher, takePictureLauncher, onCancelled)
            } else {
                onPermissionDenied()
            }
        }

    DisposableEffect(cameraPhotoLauncher) {
        onDispose { cameraPhotoLauncher.discardPending() }
    }

    TextButton(
        onClick = {
            if (cameraPhotoLauncher.isPermissionGranted()) {
                launchCamera(stopId, cameraPhotoLauncher, takePictureLauncher, onCancelled)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    ) {
        Text(stringResource(R.string.add_photo_report))
    }
}

private fun launchCamera(
    stopId: Long,
    cameraPhotoLauncher: CameraPhotoLauncher,
    takePictureLauncher: ActivityResultLauncher<Uri>,
    onCancelled: () -> Unit,
) {
    val uri = cameraPhotoLauncher.prepare(stopId)
    if (uri == null) {
        onCancelled()
        return
    }
    try {
        takePictureLauncher.launch(uri)
    } catch (_: ActivityNotFoundException) {
        cameraPhotoLauncher.discardPending()
        onCancelled()
    }
}
