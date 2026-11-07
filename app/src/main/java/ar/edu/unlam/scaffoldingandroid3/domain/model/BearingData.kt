package ar.edu.unlam.scaffoldingandroid3.domain.model

@JvmInline
value class Bearing(
    val value: Double,
)

data class BearingData(
    val bearing: Bearing,
) {
    init {
        require(bearing.value in 0.0..360.0) {
            "Bearing value must be between 0.0 and 360.0"
        }
    }

    fun withBearing(bearing: Bearing): BearingData =
        BearingData(bearing)
}
