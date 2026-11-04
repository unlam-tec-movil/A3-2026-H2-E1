package ar.edu.unlam.mobile.movil22026h2.domain.model

@JvmInline
value class Latitude(
    val value: Double,
)

@JvmInline
value class Longitude(
    val value: Double,
)

data class LocationData(
    val latitude: Latitude,
    val longitude: Longitude,
) {
    init {
        require(latitude.value in -90.0..90.0) { IllegalArgumentException(INVALID_LATITUDE) }
        require(longitude.value in -180.0..180.0) { IllegalArgumentException(INVALID_LONGITUDE) }
    }

    fun withLatitude(value: Double) = this.copy(latitude = Latitude(value))

    fun withLongitude(value: Double) = this.copy(longitude = Longitude(value))
}

private const val INVALID_LATITUDE = "El valor de latitud debe ubicarse entre -90.0 y 90.0"
private const val INVALID_LONGITUDE = "El valor de longitud debe ubicarse entre -180.0 y 180.0"
