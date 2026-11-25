package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.net.Uri
import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.PhotoFile
import ar.edu.unlam.scaffoldingandroid3.ui.helper.CameraPermissionChecker
import ar.edu.unlam.scaffoldingandroid3.ui.helper.CameraPhotoLauncher
import ar.edu.unlam.scaffoldingandroid3.ui.helper.PhotoFileFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import java.io.File

class CameraPhotoLauncherTest {
    private val uri = mockk<Uri>(relaxed = true)
    private val file = File(PHOTO_FILE_NAME)
    private val permission = FakePermissionChecker()
    private val files = FakePhotoFileFactory()
    private val launcher = CameraPhotoLauncher(permission, files)

    init {
        every { uri.toString() } returns PHOTO_CONTENT_URI
        files.photoFile = PhotoFile(file, uri)
    }

    @Test
    @DisplayName("Cuando una captura tiene éxito, el URI de contenido y el ID de parada en el momento del lanzamiento deben ser reportados")
    fun whenCaptureSucceedsThenContentUriAndStopIdAtLaunchTimeAreReported() {
        var currentStopId = STOP_ID
        val launchedUri = launcher.prepare(currentStopId)
        currentStopId = UPDATED_STOP_ID

        assertSame(uri, launchedUri)
        assertEquals(UPDATED_STOP_ID, currentStopId)
        assertEquals(
            PhotoCaptureResult.Taken(PHOTO_CONTENT_URI, STOP_ID),
            launcher.consumeResult(true),
        )
        assertTrue(files.deleted.isEmpty())
    }

    @Test
    @DisplayName("Cuando una captura es cancelada, el archivo pendiente debe ser eliminado")
    fun whenCaptureIsCancelledThenPendingFileIsDeleted() {
        launcher.prepare(STOP_ID)

        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(false))
        assertEquals(listOf(file), files.deleted)
    }

    @Test
    @DisplayName("Cuando no se puede crear el archivo de foto, no debe haber captura para lanzar")
    fun whenPhotoFileCannotBeCreatedThenNoCaptureToLaunch() {
        files.photoFile = null

        assertNull(launcher.prepare(STOP_ID))
        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(true))
        assertTrue(files.deleted.isEmpty())
    }

    @Test
    @DisplayName("Cuando una captura pendiente se descarta, su archivo debe ser eliminado y el resultado debe ser ignorado")
    fun whenPendingCaptureIsDiscardedThenItsFileIsDeletedAndResultIsIgnored() {
        launcher.prepare(STOP_ID)

        launcher.discardPending()

        assertEquals(listOf(file), files.deleted)
        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(true))
    }

    @Test
    @DisplayName("Cuando se verifica el permiso de cámara, el resultado del verificador debe ser devuelto")
    fun whenCameraPermissionIsCheckedThenCheckerResultIsReturned() {
        permission.granted = false
        assertFalse(launcher.isPermissionGranted())

        permission.granted = true
        assertTrue(launcher.isPermissionGranted())
    }

    private class FakePermissionChecker : CameraPermissionChecker {
        var granted: Boolean = false

        override fun isGranted(): Boolean = granted
    }

    private class FakePhotoFileFactory : PhotoFileFactory {
        var photoFile: PhotoFile? = null
        val deleted = mutableListOf<File>()

        override fun create(): PhotoFile? = photoFile

        override fun delete(file: File) {
            deleted += file
        }
    }

    private companion object {
        const val PHOTO_FILE_NAME = "report.jpg"
        const val PHOTO_CONTENT_URI = "content://photo/1"
        const val STOP_ID = 42L
        const val UPDATED_STOP_ID = 99L
    }
}
