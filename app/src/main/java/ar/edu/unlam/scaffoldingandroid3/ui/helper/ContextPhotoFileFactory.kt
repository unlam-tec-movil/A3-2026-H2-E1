package ar.edu.unlam.scaffoldingandroid3.ui.helper

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.PhotoFile
import java.io.File
import java.io.IOException

private const val PHOTO_REPORTS_DIRECTORY = "photo_reports"
private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

class ContextPhotoFileFactory(
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
