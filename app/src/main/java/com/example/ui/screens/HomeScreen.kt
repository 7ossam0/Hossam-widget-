package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var widgetSearchQuery by remember { mutableStateOf("") }

    val filteredWidgetConfigs = remember(widgetConfigs, widgetSearchQuery) {
        if (widgetSearchQuery.isBlank()) {
            widgetConfigs
        } else {
            widgetConfigs.filter { it.name.contains(widgetSearchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "استوديو الودجت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Widget Studio Pro",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { viewModel.triggerWidgetRefreshBroadcast() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الكل", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إنشاء ودجت جديد", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
            // Designer & Contact Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المصمم والمطور: حسام أحمد",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "مصر (+20) 01285610761",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "دعم مباشر",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "لطلب أي تصاميم إضافية أو مقترحات مخصصة للأذكار والودجت، تواصل معي مباشرة:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val whatsappIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://wa.me/201285610761")
                                        )
                                        whatsappIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(whatsappIntent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تواصل واتساب (+20)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("WhatsApp Number", "+201285610761")
                                    clipboard.setPrimaryClip(clip)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرقم")
                            }
                        }
                    }
                }
            }

            // Navigation Hub Cards (3 High-UX Action Tiles)
            item {
                Text(
                    text = "أقسام الإدارة والتحكم",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        onClick = onNavigateToContent,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("المحتوى والأذكار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        onClick = onNavigateToCategories,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("التصنيفات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        onClick = onNavigateToBackup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("النسخ الاحتياطي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Home Screen Widgets List Header & Sorting Options
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الودجت المصممة (${widgetConfigs.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "خصص الترتيب للأعلى/للأسفل ⬆️⬇️",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Search Widget Field
                    if (widgetConfigs.size > 2) {
                        OutlinedTextField(
                            value = widgetSearchQuery,
                            onValueChange = { widgetSearchQuery = it },
                            placeholder = { Text("بحث بين الودجات المصممة...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = if (widgetSearchQuery.isNotEmpty()) {
                                { IconButton(onClick = { widgetSearchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp)) } }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (widgetConfigs.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ترتيب سريع:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SuggestionChip(
                                onClick = { viewModel.sortWidgetsNewestFirst() },
                                label = { Text("الأحدث أولاً", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            SuggestionChip(
                                onClick = { viewModel.sortWidgetsOldestFirst() },
                                label = { Text("الأقدم أولاً", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            SuggestionChip(
                                onClick = { viewModel.sortWidgetsAlphabetically() },
                                label = { Text("أبجدي", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }

            if (filteredWidgetConfigs.isEmpty()) {
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
                                if (widgetSearchQuery.isNotEmpty()) "لا توجد ودجت مطابقة لبحثك" else "لم تقم بإنشاء ودجت حتى الآن",
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
                itemsIndexed(filteredWidgetConfigs, key = { _, it -> it.appWidgetId }) { index, config ->
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
                            // Widget Order & Title Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = config.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Reorder Up / Down Action Controls
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.moveWidgetUp(config) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "نقل لأعلى",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    FilledTonalIconButton(
                                        onClick = { viewModel.moveWidgetDown(config) },
                                        enabled = index < widgetConfigs.size - 1,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "نقل لأسفل",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Live Preview Component
                            WidgetLivePreviewCard(
                                config = config,
                                items = items,
                                categoryName = catName,
                                onRefreshClick = { viewModel.advanceWidgetContent(config) },
                                onNextClick = { viewModel.advanceWidgetContent(config) }
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
                                    // Share Text/Dua Button
                                    IconButton(onClick = {
                                        val displayItem = items.getOrNull(config.currentContentIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)))
                                        val textToShare = if (displayItem != null) {
                                            val cleanTitle = displayItem.title.replace(Regex("<[^>]*>"), "")
                                            val cleanBody = displayItem.body.replace(Regex("<[^>]*>"), "")
                                            if (cleanTitle.isNotBlank()) "$cleanTitle\n\n$cleanBody" else cleanBody
                                        } else {
                                            config.name
                                        }

                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, config.name)
                                            putExtra(Intent.EXTRA_TEXT, textToShare)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة النص"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.tertiary)
                                    }

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
