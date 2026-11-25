package ar.edu.unlam.scaffoldingandroid3.ui.dataclass

data class ShareMessage(
    val stopName: String,
    val linesText: String,
    val mapsUrl: String,
) {
    val message: String
        get() = "Parada: $stopName\nLíneas: $linesText\nUbicación: $mapsUrl"
}
