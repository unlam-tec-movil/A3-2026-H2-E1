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
            IllegalArgumentException(INVALID_BEARING)
        }
    }

    fun withBearing(value: Double) = this.copy(bearing = Bearing(value))
}

private const val INVALID_BEARING = "El valor de bearing debe ubicarse entre 0.0 y 360.0"
