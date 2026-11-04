package ar.edu.unlam.mobile.movil22026h2.domain.model

data class BusStop(
    val stopId: Long,
    val name: String,
    val locationData: LocationData,
    val markedAsFavorite: Boolean,
    val busLines: List<BusLine>,
    val status: StopStatus = StopStatus.ACTIVA,
)
