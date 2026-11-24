package ar.edu.unlam.scaffoldingandroid3.ui.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ContextCameraPermissionChecker(
    private val context: Context,
) : CameraPermissionChecker {
    override fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
}
