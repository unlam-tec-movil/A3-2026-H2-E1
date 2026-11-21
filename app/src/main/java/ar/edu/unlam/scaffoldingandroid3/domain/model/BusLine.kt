package ar.edu.unlam.scaffoldingandroid3.domain.model

import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus

data class BusLine(
    val lineId: Long,
    val number: Int,
    val routes: List<BusRoute> = emptyList(),
    val currentStatus: BusLineStatus = BusLineStatus.NORMAL,
    val code: String = number.toString(),
    val branch: String = "",
    val destination: String = "",
)
