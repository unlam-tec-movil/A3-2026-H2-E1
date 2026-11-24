package ar.edu.unlam.scaffoldingandroid3.ui.dataclass

import android.net.Uri
import java.io.File

data class PendingPhoto(
    val file: File,
    val uri: Uri,
    val stopId: Long,
)
