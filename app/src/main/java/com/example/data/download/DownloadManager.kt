package com.example.data.download

import android.content.Context
import android.os.Environment
import com.example.data.model.DownloadHistory
import com.example.data.repository.VidnexaRepository
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class DownloadManager(
    private val context: Context,
    private val repository: VidnexaRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val pausedProgress = ConcurrentHashMap<String, Float>()

    // Start or Resume a Download
    fun startDownload(
        id: String,
        url: String,
        title: String,
        thumbnailUrl: String?,
        duration: Int,
        platform: String,
        formatSelected: String,
        fileSize: Long
    ) {
        // Cancel existing job if any
        activeJobs[id]?.cancel()

        val job = scope.launch {
            try {
                // Initialize in DB
                val initialProgress = pausedProgress[id] ?: 0.0f
                val downloadRecord = DownloadHistory(
                    id = id,
                    url = url,
                    title = title,
                    thumbnailUrl = thumbnailUrl,
                    duration = duration,
                    platform = platform,
                    formatSelected = formatSelected,
                    fileSize = fileSize,
                    downloadPath = null,
                    status = "DOWNLOADING",
                    progress = initialProgress,
                    timestamp = System.currentTimeMillis(),
                    errorMessage = null
                )
                repository.insertOrUpdateHistory(downloadRecord)

                val isMock = url.startsWith("https://example.com") || url.contains("mock")
                val targetFile = getTargetFile(title, formatSelected)

                if (isMock) {
                    // Simulated beautiful download progress loop
                    var progress = initialProgress
                    while (progress < 1.0f) {
                        delay(200) // update interval
                        progress += 0.05f
                        if (progress > 1.0f) progress = 1.0f
                        
                        // Check if paused or cancelled mid-loop
                        if (!coroutineContext.isActive) {
                            pausedProgress[id] = progress
                            repository.updateHistoryProgress(id, progress, "PAUSED")
                            return@launch
                        }

                        repository.updateHistoryProgress(id, progress, "DOWNLOADING")
                    }

                    // Write a mock placeholder file so "Open File" and "Share" intents work!
                    targetFile.parentFile?.mkdirs()
                    targetFile.writeText("Vidnexa Mock Media File Content for: $title\nFormat: $formatSelected\nURL: $url")
                    
                    repository.updateHistoryCompletedPath(id, targetFile.absolutePath, "COMPLETED")
                    pausedProgress.remove(id)
                    activeJobs.remove(id)
                } else {
                    // Actual file download using OkHttp
                    runActualDownload(id, url, targetFile, initialProgress)
                }
            } catch (e: CancellationException) {
                // Handled gracefully via outer structures
            } catch (e: Exception) {
                repository.updateHistoryError(id, e.localizedMessage ?: "Unknown download error")
                activeJobs.remove(id)
            }
        }

        activeJobs[id] = job
    }

    private suspend fun runActualDownload(id: String, url: String, targetFile: File, startProgress: Float) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Server returned code ${response.code}")
                }
                
                val body = response.body ?: throw Exception("Response body is empty")
                val totalBytes = body.contentLength()
                
                targetFile.parentFile?.mkdirs()
                
                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var downloadedBytes = 0L
                        var lastUpdateTimestamp = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (!coroutineContext.isActive) {
                                repository.updateHistoryProgress(id, downloadedBytes.toFloat() / totalBytes, "PAUSED")
                                pausedProgress[id] = downloadedBytes.toFloat() / totalBytes
                                return@withContext
                            }

                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            val currentTimestamp = System.currentTimeMillis()
                            if (currentTimestamp - lastUpdateTimestamp > 300) { // Throttle updates
                                val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0.5f
                                repository.updateHistoryProgress(id, progress, "DOWNLOADING")
                                lastUpdateTimestamp = currentTimestamp
                            }
                        }
                    }
                }
                
                repository.updateHistoryCompletedPath(id, targetFile.absolutePath, "COMPLETED")
                pausedProgress.remove(id)
                activeJobs.remove(id)
            }
        }
    }

    fun pauseDownload(id: String) {
        val job = activeJobs[id]
        if (job != null && job.isActive) {
            job.cancel() // cancels the coroutine
            // The cancelled job catch block or check will handle saving "PAUSED" state
            activeJobs.remove(id)
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        pausedProgress.remove(id)
        scope.launch {
            repository.deleteHistory(id)
        }
    }

    private fun getTargetFile(title: String, formatSelected: String): File {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val ext = if (formatSelected.contains("mp3") || formatSelected.contains("Audio")) "mp3" else "mp4"
        val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.$ext"
        
        // Save to public downloads directory or app external files directory as a secure fallback
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        return File(downloadDir, fileName)
    }
}
