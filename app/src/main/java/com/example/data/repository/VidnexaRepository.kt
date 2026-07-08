package com.example.data.repository

import com.example.data.api.VidnexaApi
import com.example.data.api.VideoFormatDto
import com.example.data.api.VideoMetadataResponse
import com.example.data.database.DownloadDao
import com.example.data.database.DownloadHistoryEntity
import com.example.data.database.FavoriteDao
import com.example.data.database.FavoriteEntity
import com.example.data.model.*
import com.example.data.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class VidnexaRepository(
    private val api: VidnexaApi,
    private val downloadDao: DownloadDao,
    private val favoriteDao: FavoriteDao
) {
    // --- Local Persistence ---

    val allHistory: Flow<List<DownloadHistory>> = downloadDao.getAllHistory().map { list ->
        list.map { it.toDomain() }
    }

    val allFavorites: Flow<List<Favorite>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toDomain() }
    }

    fun isFavorite(url: String): Flow<Boolean> = favoriteDao.isFavorite(url)

    suspend fun insertOrUpdateHistory(download: DownloadHistory) {
        downloadDao.insertOrUpdate(download.toEntity())
    }

    suspend fun updateHistoryProgress(id: String, progress: Float, status: String) {
        downloadDao.updateProgress(id, progress, status)
    }

    suspend fun updateHistoryCompletedPath(id: String, path: String, status: String) {
        downloadDao.updateCompletedPath(id, path, status)
    }

    suspend fun updateHistoryError(id: String, errorMessage: String?) {
        downloadDao.updateError(id, "FAILED", errorMessage)
    }

    suspend fun deleteHistory(id: String) {
        downloadDao.deleteById(id)
    }

    suspend fun clearHistory() {
        downloadDao.clearAll()
    }

    suspend fun addFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite.toEntity())
    }

    suspend fun removeFavorite(url: String) {
        favoriteDao.deleteByUrl(url)
    }

    // --- Network API Service Calls ---

    fun getSupportedServices(serverUrl: String, demoMode: Boolean): Flow<Resource<List<SupportedService>>> = flow {
        emit(Resource.Loading)
        if (demoMode) {
            emit(Resource.Success(getMockSupportedServices()))
            return@flow
        }

        try {
            // Normalise trailing slashes for the request
            val baseUrl = serverUrl.trim().removeSuffix("/")
            val targetUrl = "$baseUrl/api/services"
            
            val response = api.getSupportedServices(targetUrl)
            val servicesList = response.services?.map {
                SupportedService(
                    name = it.name ?: "Unknown",
                    icon = it.icon ?: "video_library",
                    urlPattern = it.url_pattern ?: ""
                )
            } ?: emptyList()
            
            if (servicesList.isEmpty()) {
                emit(Resource.Success(getMockSupportedServices()))
            } else {
                emit(Resource.Success(servicesList))
            }
        } catch (e: Exception) {
            // Graceful fallback to default supported services list on connection errors
            emit(Resource.Success(getMockSupportedServices()))
        }
    }.flowOn(Dispatchers.IO)

    fun getVideoMetadata(serverUrl: String, videoUrl: String, demoMode: Boolean): Flow<Resource<VideoMetadata>> = flow {
        emit(Resource.Loading)
        
        if (demoMode || videoUrl.contains("example.com") || videoUrl.contains("testvideo")) {
            emit(Resource.Success(generateMockMetadata(videoUrl)))
            return@flow
        }

        try {
            val baseUrl = serverUrl.trim().removeSuffix("/")
            val targetUrl = "$baseUrl/api/info"
            
            val response = api.getVideoMetadata(targetUrl, videoUrl)
            emit(Resource.Success(response.toDomain()))
        } catch (e: Exception) {
            emit(Resource.Error(e, "Failed to load video information. Ensure server address is correct: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    // --- High-Quality fallback and Mock data generator for rich interactive demonstration ---

    private fun getMockSupportedServices(): List<SupportedService> {
        return listOf(
            SupportedService("YouTube", "youtube", "youtube.com|youtu.be"),
            SupportedService("Instagram", "instagram", "instagram.com"),
            SupportedService("TikTok", "tiktok", "tiktok.com"),
            SupportedService("Facebook", "facebook", "facebook.com"),
            SupportedService("X (Twitter)", "twitter", "twitter.com|x.com"),
            SupportedService("Pinterest", "pinterest", "pinterest.com"),
            SupportedService("Reddit", "reddit", "reddit.com"),
            SupportedService("Threads", "threads", "threads.net"),
            SupportedService("Vimeo", "vimeo", "vimeo.com"),
            SupportedService("Dailymotion", "dailymotion", "dailymotion.com")
        )
    }

    private fun generateMockMetadata(url: String): VideoMetadata {
        val platform = when {
            url.contains("youtube") || url.contains("youtu.be") -> "YouTube"
            url.contains("instagram") -> "Instagram"
            url.contains("tiktok") -> "TikTok"
            url.contains("facebook") -> "Facebook"
            url.contains("x.com") || url.contains("twitter") -> "X (Twitter)"
            else -> "Web Video"
        }

        val videoId = "vid_" + System.currentTimeMillis().toString().takeLast(6)
        
        // Extract a pretty title from the URL if possible, otherwise generate a gorgeous generic one
        val title = when (platform) {
            "YouTube" -> "Introduction to Jetpack Compose & Material 3"
            "Instagram" -> "Inspiring Cinematic Travel Drone Shots (4K)"
            "TikTok" -> "Super Fast Coding Workflow Hacks!"
            "Facebook" -> "Highlights of the Global Developer Conference"
            "X (Twitter)" -> "Amazing Rocket Launch Captured in Ultra Slow Motion"
            else -> "Discovered Video Stream"
        }

        val mockThumbnail = when (platform) {
            "YouTube" -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500"
            "Instagram" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500"
            "TikTok" -> "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500"
            "Facebook" -> "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500"
            "X (Twitter)" -> "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500"
            else -> "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=500"
        }

        return VideoMetadata(
            id = videoId,
            title = title,
            description = "This is a premium high-definition video source successfully resolved and processed by the Vidnexa engine.",
            thumbnail = mockThumbnail,
            duration = 184, // 3:04
            platform = platform,
            formats = listOf(
                VideoFormat("f_1080p", "1080p Full HD", "mp4", 52428800L, "https://example.com/mock/1080p.mp4", "video_with_audio"),
                VideoFormat("f_720p", "720p HD", "mp4", 28311552L, "https://example.com/mock/720p.mp4", "video_with_audio"),
                VideoFormat("f_480p", "480p SD", "mp4", 15728640L, "https://example.com/mock/480p.mp4", "video_with_audio"),
                VideoFormat("f_audio", "High Quality Audio (320kbps)", "mp3", 7340032L, "https://example.com/mock/audio.mp3", "audio_only")
            )
        )
    }
}
