package com.example.rustoreapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FullscreenScreenshotsScreen(
    screenshots: List<Int>,
    initialPage: Int,
    onBackClick: () -> Unit
) {
    if (screenshots.isEmpty()) {
        // Если скриншотов нет, показываем сообщение
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceAtMost(screenshots.size - 1),
        pageCount = { screenshots.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = screenshots[page]),
                contentDescription = "Скриншот ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White
            )
        }
    }
}