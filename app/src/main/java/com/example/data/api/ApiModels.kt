package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoMetadataResponse(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "thumbnail") val thumbnail: String?,
    @Json(name = "duration") val duration: Int?, // in seconds
    @Json(name = "platform") val platform: String?,
    @Json(name = "formats") val formats: List<VideoFormatDto>?
)

@JsonClass(generateAdapter = true)
data class VideoFormatDto(
    @Json(name = "id") val id: String?,
    @Json(name = "resolution") val resolution: String?,
    @Json(name = "extension") val extension: String?,
    @Json(name = "file_size") val file_size: Long?,
    @Json(name = "url") val url: String?,
    @Json(name = "type") val type: String? // "video", "audio", "video_with_audio"
)

@JsonClass(generateAdapter = true)
data class SupportedServicesResponse(
    @Json(name = "services") val services: List<SupportedServiceDto>?
)

@JsonClass(generateAdapter = true)
data class SupportedServiceDto(
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "url_pattern") val url_pattern: String?
)
