package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface VidnexaApi {
    @GET
    suspend fun getVideoMetadata(
        @Url fullUrl: String,
        @Query("url") targetVideoUrl: String
    ): VideoMetadataResponse

    @GET
    suspend fun getSupportedServices(
        @Url fullUrl: String
    ): SupportedServicesResponse
}
