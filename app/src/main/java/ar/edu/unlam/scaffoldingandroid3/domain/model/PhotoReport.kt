package ar.edu.unlam.scaffoldingandroid3.domain.model

import java.time.Instant

data class PhotoReport(
    val id: String,
    val photoUri: String,
    val observation: String,
    val timestamp: Instant,
    val stopId: Long,
)
