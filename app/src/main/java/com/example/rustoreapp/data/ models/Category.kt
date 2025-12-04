package com.example.rustoreapp.data.models

enum class Category(val displayName: String, val icon: String) {
    FINANCE("Финансы", "💰"),
    TOOLS("Инструменты", "🛠️"),
    GAMES("Игры", "🎮"),
    GOVERNMENT("Государственные", "🏛️"),
    TRANSPORT("Транспорт", "🚗"),
    ALL("Все приложения", "📱");

    companion object {
        fun fromString(value: String): Category {
            return values().find { it.displayName == value } ?: ALL
        }
    }
}