package com.example.rustoreapp.data.repository

import com.example.rustoreapp.data.api.ApiClient
import com.example.rustoreapp.data.api.AppResponse
import com.example.rustoreapp.data.models.App
import com.example.rustoreapp.data.models.AgeRating
import com.example.rustoreapp.data.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    private val api = ApiClient.instance

    suspend fun getAllApps(): Result<List<App>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllApps()
            if (response.isSuccessful) {
                val apps = response.body()?.map { it.toApp() } ?: emptyList()
                Result.success(apps)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AppResponse.toApp(): App {
        return App(
            id = this.id,
            name = this.name,
            developer = this.developer,
            iconResId = getIconResource(this.id),
            shortDescription = this.shortDescription,
            fullDescription = this.fullDescription,
            category = Category.fromString(this.category),
            ageRating = AgeRating.fromString(this.ageRating),
            screenshots = getScreenshotResources(this.id),
            apkUrl = this.apkUrl
        )
    }

    private fun getIconResource(appId: Int): Int {
        return when (appId) {
            1 -> com.example.rustoreapp.R.drawable.sber_icon
            2 -> com.example.rustoreapp.R.drawable.vk_messenger_icon
            3 -> com.example.rustoreapp.R.drawable.gosuslugi_icon
            4 -> com.example.rustoreapp.R.drawable.yandex_metro_icon
            5 -> com.example.rustoreapp.R.drawable.checkers_icon
            6 -> com.example.rustoreapp.R.drawable.calculator_icon
            else -> com.example.rustoreapp.R.drawable.ic_launcher_foreground
        }
    }

    private fun getScreenshotResources(appId: Int): List<Int> {
        return when (appId) {
            1 -> listOf(
                com.example.rustoreapp.R.drawable.screenshot1,
                com.example.rustoreapp.R.drawable.screenshot2,
                com.example.rustoreapp.R.drawable.screenshot3
            )
            2 -> listOf(
                com.example.rustoreapp.R.drawable.screenshot6,
                com.example.rustoreapp.R.drawable.screenshot7,
                com.example.rustoreapp.R.drawable.screenshot8
            )
            // Добавьте остальные
            else -> emptyList()
        }
    }
}