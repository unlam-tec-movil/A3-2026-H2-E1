package ar.edu.unlam.scaffoldingandroid3.domain.model

/** Fotografía y observación registradas para una parada. */
data class PhotoReport(
    val id: String,
    val photoUri: String,
    val observation: String,
    val timestamp: Long,
    val stopId: Long,
) {
    init {
        require(id.isNotBlank()) { "id no puede estar vacío" }
        require(photoUri.isNotBlank()) { "photoUri no puede estar vacío" }
        require(observation.isNotBlank()) { "observation no puede estar vacía" }
        require(timestamp > 0) { "timestamp debe ser positivo" }
        require(stopId > 0) { "stopId debe ser positivo" }
    }
}
