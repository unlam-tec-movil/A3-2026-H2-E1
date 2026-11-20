package ar.edu.unlam.scaffoldingandroid3.data.database.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_reports")
data class PhotoReportEntity(
    @PrimaryKey val id: String,
    val photoUri: String,
    val observation: String,
    val timestamp: Long,
    val stopId: Long,
)
