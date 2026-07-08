package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: Int, // in seconds
    val platform: String,
    val formatSelected: String,
    val fileSize: Long,
    val downloadPath: String?,
    val status: String, // "PENDING", "DOWNLOADING", "COMPLETED", "FAILED", "PAUSED"
    val progress: Float, // 0.0 to 1.0f
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val url: String,
    val title: String,
    val platform: String,
    val timestamp: Long = System.currentTimeMillis()
)
