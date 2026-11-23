package ar.edu.unlam.scaffoldingandroid3.ui.components

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CameraPhotoLauncherTest {
    private val uri = mockk<Uri>(relaxed = true)
    private val file = File("report.jpg")
    private val permission = FakePermissionChecker()
    private val files = FakePhotoFileFactory()
    private val launcher = CameraPhotoLauncher(permission, files)

    init {
        every { uri.toString() } returns "content://photo/1"
        files.photoFile = PhotoFile(file, uri)
    }

    @Test
    fun `when a capture succeeds, then the content uri and the stop id at launch time are reported`() {
        var currentStopId = 42L
        val launchedUri = launcher.prepare(currentStopId)
        currentStopId = 99L

        assertSame(uri, launchedUri)
        assertEquals(99L, currentStopId)
        assertEquals(
            PhotoCaptureResult.Taken("content://photo/1", 42L),
            launcher.consumeResult(true),
        )
        assertTrue(files.deleted.isEmpty())
    }

    @Test
    fun `when a capture is cancelled, then the pending file is deleted`() {
        launcher.prepare(42L)

        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(false))
        assertEquals(listOf(file), files.deleted)
    }

    @Test
    fun `when the photo file cannot be created, then there is no capture to launch`() {
        files.photoFile = null

        assertNull(launcher.prepare(42L))
        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(true))
        assertTrue(files.deleted.isEmpty())
    }

    @Test
    fun `when a pending capture is discarded, then its file is deleted and the result is ignored`() {
        launcher.prepare(42L)

        launcher.discardPending()

        assertEquals(listOf(file), files.deleted)
        assertEquals(PhotoCaptureResult.Cancelled, launcher.consumeResult(true))
    }

    @Test
    fun `when checking the camera permission, then the checker result is returned`() {
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
}
