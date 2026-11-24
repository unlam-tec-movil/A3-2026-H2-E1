package ar.edu.unlam.scaffoldingandroid3.ui.helper

import ar.edu.unlam.scaffoldingandroid3.ui.dataclass.PhotoFile
import java.io.File

interface PhotoFileFactory {
    fun create(): PhotoFile?

    fun delete(file: File)
}
