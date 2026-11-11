package ar.edu.unlam.scaffoldingandroid3.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
data class PhotoReport(
    val id: String,
    val photoUri: String,
    val observation: String,
    val timestamp: Instant,
    val stopId: Long,
) {
    init {
        require(photoUri.isNotBlank()) { "photoUri no puede estar vacío o en blanco" }
        require(id.isNotBlank()) { "id no puede estar vacío o en blanco" }
        require(stopId > 0) { "stopId debe ser positivo (mayor a 0)" }
        require(timestamp <= Instant.now()) { "timestamp no puede estar en el futuro" }
    }
}
