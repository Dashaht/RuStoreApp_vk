package com.example.rustoreapp.data.models

data class App(
    val id: Int,
    val name: String,
    val developer: String,
    val iconResId: Int, // или URL если будет API
    val shortDescription: String,
    val fullDescription: String,
    val category: Category,
    val ageRating: AgeRating,
    val screenshots: List<Int>, // или List<String> если будет API
    val apkUrl: String? = null // Для опциональной установки
)

enum class AgeRating(val displayName: String) {
    ZERO_PLUS("0+"),
    SIX_PLUS("6+"),
    EIGHT_PLUS("8+"),
    TWELVE_PLUS("12+"),
    SIXTEEN_PLUS("16+"),
    EIGHTEEN_PLUS("18+");

    companion object {
        fun fromString(value: String): AgeRating {
            return values().find { it.displayName == value } ?: ZERO_PLUS
        }
    }
}