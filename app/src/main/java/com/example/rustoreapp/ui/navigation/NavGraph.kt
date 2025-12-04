package com.example.rustoreapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rustoreapp.ui.screens.AppDetailScreen
import com.example.rustoreapp.ui.screens.AppListScreen
import com.example.rustoreapp.ui.screens.CategoryListScreen
import com.example.rustoreapp.ui.screens.FullscreenScreenshotsScreen
import com.example.rustoreapp.ui.screens.OnboardingScreen
import com.example.rustoreapp.ui.viewmodel.AppViewModel
import com.example.rustoreapp.utils.DataStoreManager

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object AppList : Screen("appList")
    object AppDetail : Screen("appDetail/{appId}") {
        fun createRoute(appId: Int) = "appDetail/$appId"
    }
    object CategoryList : Screen("categoryList")
    object FullscreenScreenshots : Screen("screenshots/{screenshots}/{initialPage}") {
        fun createRoute(screenshots: String, initialPage: Int) = "screenshots/$screenshots/$initialPage"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    val viewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(dataStoreManager)
    )

    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    // УБЕРИТЕ ЭТОТ LaunchedEffect - он автоматически переходит на главный экран!
    /*
    LaunchedEffect(Unit) {
        if (!onboardingCompleted) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(0)
            }
        }
    }
    */

    NavHost(
        navController = navController,
        // Если onboardingCompleted = true, сразу идет AppList, иначе Onboarding
        startDestination = if (onboardingCompleted) Screen.AppList.route else Screen.Onboarding.route
    ) {
        // Онбординг
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onContinueClick = {
                    viewModel.completeOnboarding()
                    navController.navigate(Screen.AppList.route) {
                        // Убираем Onboarding из стека навигации
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Список приложений
        composable(Screen.AppList.route) {
            AppListScreen(
                viewModel = viewModel,
                onAppClick = { appId ->
                    navController.navigate(Screen.AppDetail.createRoute(appId))
                },
                onCategoriesClick = {
                    navController.navigate(Screen.CategoryList.route)
                }
            )
        }

        // Детали приложения
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("appId") { type = NavType.IntType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt("appId") ?: return@composable
            val app = viewModel.getAppById(appId)

            if (app != null) {
                AppDetailScreen(
                    app = app,
                    onBackClick = { navController.popBackStack() },
                    onScreenshotClick = { screenshotIndex ->
                        val screenshotIds = app.screenshots.joinToString(",") { it.toString() }
                        navController.navigate(
                            Screen.FullscreenScreenshots.createRoute(
                                screenshotIds,
                                screenshotIndex
                            )
                        )
                    }
                )
            }
        }

        // Список категорий
        composable(Screen.CategoryList.route) {
            CategoryListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { category ->
                    viewModel.filterByCategory(category)
                    navController.popBackStack()
                }
            )
        }

        // Полноэкранные скриншоты
        composable(
            route = Screen.FullscreenScreenshots.route,
            arguments = listOf(
                navArgument("screenshots") { type = NavType.StringType },
                navArgument("initialPage") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val screenshotsString = backStackEntry.arguments?.getString("screenshots") ?: ""
            val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0

            val screenshots = screenshotsString.split(",")
                .filter { it.isNotEmpty() }
                .map { it.toInt() }

            if (screenshots.isNotEmpty()) {
                FullscreenScreenshotsScreen(
                    screenshots = screenshots,
                    initialPage = initialPage,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// Factory для создания ViewModel с зависимостью
class AppViewModelFactory(
    private val dataStoreManager: DataStoreManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}