package com.example

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.WidgetConfigEntity
import com.example.services.PrayerNotificationHelper
import com.example.ui.screens.*
import com.example.ui.theme.WidgetStudioTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            // Fallback for older OEM customizations
        }

        try {
            PrayerNotificationHelper.createNotificationChannels(this)
        } catch (e: Throwable) {
            // Safe guard
        }

        // Schedule alarms safely in background
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                PrayerNotificationHelper.scheduleNextPrayerAlarms(this@MainActivity)
            } catch (e: Throwable) {
                // Ignore background alarm scheduling failures
            }
        }

        setContent {
            WidgetStudioTheme {
                // Request Notification Permission on Android 13+ (API 33+)
                val context = LocalContext.current
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            try {
                                PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                            } catch (t: Throwable) {
                                // Ignore
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        try {
                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (!isGranted) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } catch (e: Throwable) {
                            // Safe fallback
                        }
                    }
                }

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
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    var activeDesignerConfig by remember { mutableStateOf<WidgetConfigEntity?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        NavigationItem("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("quran", "المصحف", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavigationItem("qibla", "القبلة", Icons.Filled.Explore, Icons.Outlined.Explore),
        NavigationItem("tasks", "المهام", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable),
        NavigationItem("ai_wisdom", "بيان الذكي", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    )

    val showBottomBar = currentRoute != "designer"

    val navigateBackOrHome: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF0D1420),
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = when (item.route) {
                            "home" -> currentRoute in listOf("home", "prayer", "tasbeeh", "content", "categories", "backup", null)
                            else -> currentRoute == item.route
                        }
                        NavigationBarItem(
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.ui.graphics.Color(0xFF090D14),
                                selectedTextColor = androidx.compose.ui.graphics.Color(0xFFE5C07B),
                                indicatorColor = androidx.compose.ui.graphics.Color(0xFFE5C07B),
                                unselectedIconColor = androidx.compose.ui.graphics.Color(0x99FFFFFF),
                                unselectedTextColor = androidx.compose.ui.graphics.Color(0x77FFFFFF)
                            ),
                            onClick = {
                                if (item.route == "home") {
                                    navController.navigate("home") {
                                        popUpTo("home") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("home") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
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
                        onNavigateToTasbeeh = { navController.navigate("tasbeeh") },
                        onNavigateToQuran = { navController.navigate("quran") },
                        onNavigateToQibla = { navController.navigate("qibla") },
                        onNavigateToTasks = { navController.navigate("tasks") },
                        onNavigateToAiWisdom = { navController.navigate("ai_wisdom") }
                    )
                }

                composable("quran") {
                    QuranScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("qibla") {
                    QiblaARScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("tasks") {
                    SpiritualTasksScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("ai_wisdom") {
                    AiWisdomScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("prayer") {
                    PrayerTimesScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("tasbeeh") {
                    TasbeehScreen(
                        viewModel = viewModel,
                        onNavigateBack = navigateBackOrHome
                    )
                }

                composable("designer") {
                    activeDesignerConfig?.let { config ->
                        WidgetDesignerScreen(
                            initialConfig = config,
                            viewModel = viewModel,
                            onBackClick = navigateBackOrHome
                        )
                    } ?: LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }

                composable("content") {
                    ContentManagementScreen(
                        viewModel = viewModel,
                        onBackClick = navigateBackOrHome
                    )
                }

                composable("categories") {
                    CategoryManagementScreen(
                        viewModel = viewModel,
                        onBackClick = navigateBackOrHome
                    )
                }

                composable("backup") {
                    BackupRestoreScreen(
                        viewModel = viewModel,
                        onBackClick = navigateBackOrHome
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
