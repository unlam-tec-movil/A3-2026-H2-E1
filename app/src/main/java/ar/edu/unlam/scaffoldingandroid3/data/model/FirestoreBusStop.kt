package ar.edu.unlam.scaffoldingandroid3.data.model

import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint

data class FirestoreBusStop(
    val name: String = "",
    val lines: List<FirestoreBusLine> = emptyList(),
    val location: FirestoreGeoPoint? = null,
    val geohash: String = "",
)

data class FirestoreBusLine(
    val code: String = "",
    val destination: String? = null,
)
