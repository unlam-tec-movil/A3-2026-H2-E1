@file:Suppress("ktlint:standard:filename")

package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class Compass(
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
)
