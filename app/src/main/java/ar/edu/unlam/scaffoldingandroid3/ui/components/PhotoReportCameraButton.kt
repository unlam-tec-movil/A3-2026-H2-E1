package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun PhotoReportCameraButton(
    stopId: Long,
    onPhotoTaken: (String, Long) -> Unit,
    onPermissionDenied: () -> Unit,
    onCancelled: () -> Unit,
) {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            val file = pendingFile
            if (saved && file != null) {
                onPhotoTaken(Uri.fromFile(file).toString(), stopId)
            } else {
                file?.delete()
                onCancelled()
            }
        }

    fun openCamera() {
        val directory =
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "photo_reports",
            )
        directory.mkdirs()
        val file = File(directory, "report_${System.currentTimeMillis()}.jpg")
        pendingFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePictureLauncher.launch(uri)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                onPermissionDenied()
            }
        }

    TextButton(
        onClick = {
            val granted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                openCamera()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
    ) {
        Text("Agregar reporte")
    }
}
