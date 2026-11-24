package ar.edu.unlam.scaffoldingandroid3.ui.helper

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ar.edu.unlam.scaffoldingandroid3.ui.components.PhotoCaptureResult
import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.PendingPhoto

class CameraPhotoLauncher(
    val permissionChecker: CameraPermissionChecker,
    val photoFileFactory: PhotoFileFactory,
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

    fun launchCamera(
        stopId: Long,
        cameraPhotoLauncher: CameraPhotoLauncher,
        takePictureLauncher: ActivityResultLauncher<Uri>,
        onCancelled: () -> Unit,
    ) {
        val uri = this.prepare(stopId)
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
}
