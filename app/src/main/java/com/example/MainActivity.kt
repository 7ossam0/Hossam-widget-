package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.WidgetConfigEntity
import com.example.ui.screens.*
import com.example.ui.theme.WidgetStudioTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WidgetStudioTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WidgetStudioApp(viewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetStudioApp(
    viewModel: MainViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val navController = rememberNavController()
    var activeDesignerConfig by remember { mutableStateOf<WidgetConfigEntity?>(null) }


    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDesigner = { config ->
                    activeDesignerConfig = config
                    navController.navigate("designer")
                },
                onNavigateToContent = { navController.navigate("content") },
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToBackup = { navController.navigate("backup") },
                onNavigateToPrayer = { navController.navigate("prayer") },
                onNavigateToTasbeeh = { navController.navigate("tasbeeh") }
            )
        }

        composable("prayer") {
            com.example.ui.screens.PrayerTimesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("tasbeeh") {
            com.example.ui.screens.TasbeehScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("designer") {
            activeDesignerConfig?.let { config ->
                WidgetDesignerScreen(
                    initialConfig = config,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            } ?: LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }

        composable("content") {
            ContentManagementScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("categories") {
            CategoryManagementScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("backup") {
            BackupRestoreScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
