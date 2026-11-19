package ar.edu.unlam.scaffoldingandroid3.domain.model

data class SearchHistory(
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
)
