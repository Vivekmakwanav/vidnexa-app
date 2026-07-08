package com.example.data.model

import com.example.data.api.VideoFormatDto
import com.example.data.api.VideoMetadataResponse
import com.example.data.database.DownloadHistoryEntity
import com.example.data.database.FavoriteEntity

data class SupportedService(
    val name: String,
    val icon: String,
    val urlPattern: String
)

data class VideoFormat(
    val id: String,
    val resolution: String,
    val extension: String,
    val fileSize: Long, // in bytes, 0 if unknown
    val url: String,
    val type: String // "video", "audio", "video_with_audio"
)

data class VideoMetadata(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val duration: Int, // in seconds
    val platform: String,
    val formats: List<VideoFormat>
)

data class DownloadHistory(
    val id: String,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: Int,
    val platform: String,
    val formatSelected: String,
    val fileSize: Long,
    val downloadPath: String?,
    val status: String, // "PENDING", "DOWNLOADING", "COMPLETED", "FAILED", "PAUSED"
    val progress: Float,
    val timestamp: Long,
    val errorMessage: String?
)

data class Favorite(
    val url: String,
    val title: String,
    val platform: String,
    val timestamp: Long
)

// --- Mapper Extensions ---

fun VideoMetadataResponse.toDomain(): VideoMetadata {
    return VideoMetadata(
        id = this.id ?: "",
        title = this.title ?: "Untitled Video",
        description = this.description ?: "",
        thumbnail = this.thumbnail ?: "",
        duration = this.duration ?: 0,
        platform = this.platform ?: "Unknown",
        formats = this.formats?.map { it.toDomain() } ?: emptyList()
    )
}

fun VideoFormatDto.toDomain(): VideoFormat {
    return VideoFormat(
        id = this.id ?: "",
        resolution = this.resolution ?: "Unknown",
        extension = this.extension ?: "mp4",
        fileSize = this.file_size ?: 0L,
        url = this.url ?: "",
        type = this.type ?: "video_with_audio"
    )
}

fun DownloadHistoryEntity.toDomain(): DownloadHistory {
    return DownloadHistory(
        id = id,
        url = url,
        title = title,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        platform = platform,
        formatSelected = formatSelected,
        fileSize = fileSize,
        downloadPath = downloadPath,
        status = status,
        progress = progress,
        timestamp = timestamp,
        errorMessage = errorMessage
    )
}

fun DownloadHistory.toEntity(): DownloadHistoryEntity {
    return DownloadHistoryEntity(
        id = id,
        url = url,
        title = title,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        platform = platform,
        formatSelected = formatSelected,
        fileSize = fileSize,
        downloadPath = downloadPath,
        status = status,
        progress = progress,
        timestamp = timestamp,
        errorMessage = errorMessage
    )
}

fun FavoriteEntity.toDomain(): Favorite {
    return Favorite(
        url = url,
        title = title,
        platform = platform,
        timestamp = timestamp
    )
}

fun Favorite.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        url = url,
        title = title,
        platform = platform,
        timestamp = timestamp
    )
}
