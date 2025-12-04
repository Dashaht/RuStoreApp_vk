package com.example.rustoreapp.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class AppResponse(
    val id: Int,
    val name: String,
    val developer: String,
    val iconUrl: String,
    val shortDescription: String,
    val fullDescription: String,
    val category: String,
    val ageRating: String,
    val screenshots: List<String>,
    val apkUrl: String? = null,
    val version: String = "1.0",
    val size: String = "50 MB",
    val downloads: String = "1M+"
)

interface AppApi {
    @GET("apps")
    suspend fun getAllApps(): Response<List<AppResponse>>

    @GET("apps/{id}")
    suspend fun getAppById(@Path("id") id: Int): Response<AppResponse>

    @GET("apps/category/{category}")
    suspend fun getAppsByCategory(@Path("category") category: String): Response<List<AppResponse>>
}