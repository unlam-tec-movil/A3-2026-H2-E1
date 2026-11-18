package ar.edu.unlam.scaffoldingandroid3.domain.model

data class BusStop(
    val stopId: Long,
    val name: String,
    val locationData: LocationData,
    val markedAsFavorite: Boolean,
    val busLines: List<BusLine>,
    val status: StopStatus = StopStatus.ACTIVA,
)
