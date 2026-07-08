package com.example.ui.viewmodel

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.download.DownloadManager
import com.example.data.model.*
import com.example.data.repository.VidnexaRepository
import com.example.data.utils.Resource
import com.example.data.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VidnexaViewModel(
    private val repository: VidnexaRepository,
    private val downloadManager: DownloadManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    // --- Dynamic Preferences States ---
    private val _serverUrl = MutableStateFlow(settingsManager.serverUrl)
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _useDarkTheme = MutableStateFlow(settingsManager.useDarkTheme)
    val useDarkTheme: StateFlow<Boolean> = _useDarkTheme.asStateFlow()

    private val _demoMode = MutableStateFlow(settingsManager.demoMode)
    val demoMode: StateFlow<Boolean> = _demoMode.asStateFlow()

    private val _autoDeleteTemp = MutableStateFlow(settingsManager.autoDeleteTemp)
    val autoDeleteTemp: StateFlow<Boolean> = _autoDeleteTemp.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(settingsManager.notificationsEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _analyticsEnabled = MutableStateFlow(settingsManager.analyticsEnabled)
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()

    // --- Interactive UI States ---
    private val _activeInputUrl = MutableStateFlow("")
    val activeInputUrl: StateFlow<String> = _activeInputUrl.asStateFlow()

    private val _supportedServices = MutableStateFlow<Resource<List<SupportedService>>>(Resource.Loading)
    val supportedServices: StateFlow<Resource<List<SupportedService>>> = _supportedServices.asStateFlow()

    private val _videoMetadataState = MutableStateFlow<Resource<VideoMetadata>?>(null)
    val videoMetadataState: StateFlow<Resource<VideoMetadata>?> = _videoMetadataState.asStateFlow()

    // --- Database Flow States ---
    val downloadHistoryList: StateFlow<List<DownloadHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesList: StateFlow<List<Favorite>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Check if a URL is in the favorites list
    fun isUrlFavorite(url: String): Flow<Boolean> = repository.isFavorite(url)

    init {
        loadSupportedServices()
    }

    // --- Preference Actions ---

    fun updateServerUrl(url: String) {
        settingsManager.serverUrl = url
        _serverUrl.value = url
        loadSupportedServices() // reload if URL changes
    }

    fun toggleTheme(dark: Boolean) {
        settingsManager.useDarkTheme = dark
        _useDarkTheme.value = dark
    }

    fun toggleDemoMode(enabled: Boolean) {
        settingsManager.demoMode = enabled
        _demoMode.value = enabled
        loadSupportedServices() // reload with the updated mode (e.g. mock vs real)
    }

    fun toggleAutoDelete(enabled: Boolean) {
        settingsManager.autoDeleteTemp = enabled
        _autoDeleteTemp.value = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        settingsManager.notificationsEnabled = enabled
        _notificationsEnabled.value = enabled
    }

    fun toggleAnalytics(enabled: Boolean) {
        settingsManager.analyticsEnabled = enabled
        _analyticsEnabled.value = enabled
    }

    // --- Home Screen Actions ---

    fun onUrlInputChanged(url: String) {
        _activeInputUrl.value = url
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            _activeInputUrl.value = text.trim()
        }
    }

    fun clearUrlInput() {
        _activeInputUrl.value = ""
    }

    fun loadSupportedServices() {
        viewModelScope.launch {
            repository.getSupportedServices(_serverUrl.value, _demoMode.value)
                .collect { resource ->
                    _supportedServices.value = resource
                }
        }
    }

    // --- Metadata Resolving Actions ---

    fun fetchVideoMetadata(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            repository.getVideoMetadata(_serverUrl.value, url.trim(), _demoMode.value)
                .collect { resource ->
                    _videoMetadataState.value = resource
                }
        }
    }

    fun clearMetadataState() {
        _videoMetadataState.value = null
    }

    // --- Download Actions ---

    fun triggerFormatDownload(video: VideoMetadata, format: VideoFormat) {
        val downloadId = "dl_" + System.currentTimeMillis().toString().takeLast(6)
        
        // Log event to analytics if active
        if (_analyticsEnabled.value) {
            android.util.Log.d("VidnexaAnalytics", "URL Submission: ${video.platform} - ${format.resolution}")
        }

        downloadManager.startDownload(
            id = downloadId,
            url = format.url,
            title = video.title,
            thumbnailUrl = video.thumbnail,
            duration = video.duration,
            platform = video.platform,
            formatSelected = "${format.resolution} (${format.extension})",
            fileSize = format.fileSize
        )
    }

    fun pauseDownload(id: String) {
        downloadManager.pauseDownload(id)
    }

    fun resumeDownload(item: DownloadHistory) {
        downloadManager.startDownload(
            id = item.id,
            url = item.url,
            title = item.title,
            thumbnailUrl = item.thumbnailUrl,
            duration = item.duration,
            platform = item.platform,
            formatSelected = item.formatSelected,
            fileSize = item.fileSize
        )
    }

    fun cancelDownload(id: String) {
        downloadManager.cancelDownload(id)
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Favorite Actions ---

    fun toggleFavorite(url: String, title: String, platform: String) {
        viewModelScope.launch {
            val isFav = repository.isFavorite(url).first()
            if (isFav) {
                repository.removeFavorite(url)
            } else {
                repository.addFavorite(
                    Favorite(
                        url = url,
                        title = title,
                        platform = platform,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}

// --- Factory ---

class VidnexaViewModelFactory(
    private val repository: VidnexaRepository,
    private val downloadManager: DownloadManager,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VidnexaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VidnexaViewModel(repository, downloadManager, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
