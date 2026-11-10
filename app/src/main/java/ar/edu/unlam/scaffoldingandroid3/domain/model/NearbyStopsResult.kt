package ar.edu.unlam.scaffoldingandroid3.domain.model

import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin

data class NearbyStopsResult(
    val stops: List<BusStop>,
    val origin: NearbyStopsOrigin,
)
