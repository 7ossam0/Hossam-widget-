package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import com.example.ui.components.WidgetLivePreviewCard
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDesigner: (WidgetConfigEntity) -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val widgetConfigs by viewModel.widgetConfigs.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استوديو الودجت - Widget Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.triggerWidgetRefreshBroadcast() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الكل")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إنشاء ودجت جديد") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Navigation Hub Cards
            item {
                Text(
                    text = "إدارة المحتوى والنظام",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedCard(
                        onClick = onNavigateToContent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("محتوى الودجت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ElevatedCard(
                        onClick = onNavigateToCategories,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("التصنيفات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    ElevatedCard(
                        onClick = onNavigateToBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("النسخ الاحتياطي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Home Screen Widgets List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الودجت المصممة (${widgetConfigs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يمكنك التمرير داخل الودجت وتخصيصها بالكامل",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (widgetConfigs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "لم تقم بإنشاء ودجت حتى الآن",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "اضغط على زر 'إنشاء ودجت جديد' للبدء في تصميم ودجت مخصص بالألوان والخطوط والمحتوى المناسب لك.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(widgetConfigs, key = { it.appWidgetId }) { config ->
                    var items by remember { mutableStateOf<List<ContentItemEntity>>(emptyList()) }

                    LaunchedEffect(config) {
                        items = viewModel.getItemsForWidget(config)
                    }

                    val catName = categories.find { it.id == config.categoryId }?.name ?: "الكل"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Live Preview Component
                            WidgetLivePreviewCard(
                                config = config,
                                items = items,
                                categoryName = catName,
                                onRefreshClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) },
                                onNextClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Control Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onNavigateToDesigner(config) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تعديل والتصميم", fontSize = 12.sp)
                                }

                                Row {
                                    IconButton(onClick = { viewModel.copyWidgetConfig(config) }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(onClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = MaterialTheme.colorScheme.secondary)
                                    }

                                    IconButton(onClick = { viewModel.deleteWidgetConfig(config) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var selectedCatId by remember { mutableStateOf<Long?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("إنشاء ودجت مخصص جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الودجت (مثال: أذكار الصباح)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("حدد التصنيف المخصص للودجت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedCatId == null,
                            onClick = { selectedCatId = null },
                            label = { Text("جميع التصنيفات") }
                        )
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCatId == cat.id,
                                onClick = { selectedCatId = cat.id },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newConfig = viewModel.createNewStandaloneWidgetConfig(name, selectedCatId)
                        viewModel.saveWidgetConfig(newConfig)
                        showCreateDialog = false
                        onNavigateToDesigner(newConfig)
                    }
                ) {
                    Text("انتقال للمصمم الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
