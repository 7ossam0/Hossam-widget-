package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.WidgetConfigEntity
import com.example.ui.screens.*
import com.example.ui.theme.WidgetStudioTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
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
                        WidgetStudioApp()
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetStudioApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

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
                onNavigateToBackup = { navController.navigate("backup") }
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
