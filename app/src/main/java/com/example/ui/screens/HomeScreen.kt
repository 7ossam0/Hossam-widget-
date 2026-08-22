package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import com.example.ui.components.*
import com.example.ui.theme.AppCustomFontFamily
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "استوديو الودجت الذكي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppCustomFontFamily,
                                color = Color(0xFFF0F6FC)
                            )
                            Text(
                                "SMART DASHBOARD STUDIO",
                                fontSize = 9.sp,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { viewModel.triggerWidgetRefreshBroadcast() },
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF161B22)
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الكل", modifier = Modifier.size(20.dp), tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0D1117)) },
                text = { Text("إنشاء ودجت جديد", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFF0D1117)) },
                containerColor = Color(0xFF00E5FF),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00E5FF))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Smart Home Style Hero Header Card
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = Color(0xFF161B22),
                    glowColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المصمم والمطور: حسام أحمد",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        fontFamily = AppCustomFontFamily,
                                        color = Color(0xFFF0F6FC)
                                    )
                                    Text(
                                        text = "مصر (+20) 01285610761",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = AppCustomFontFamily,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "دعم مباشر ✨",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppCustomFontFamily,
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = "لطلب أي تصاميم أو مقترحات مخصصة للأذكار والودجات المتقدمة، تواصل معي مباشرة:",
                            fontSize = 12.sp,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFF8B949E)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                modifier = Modifier.weight(1f).height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("واتساب (+20)", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("WhatsApp Number", "+201285610761")
                                    clipboard.setPrimaryClip(clip)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(46.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFF21262D)
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرقم", tint = Color(0xFFF0F6FC))
                            }
                        }
                    }
                }
            }

            // Neumorphic Smart Controls Dashboard (3 Cards)
            item {
                Text(
                    text = "وحدات التحكم السريعة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeumorphicControlTile(
                        title = "الأذكار",
                        subtitle = "إدارة المحتوى",
                        icon = Icons.Default.Article,
                        isActive = true,
                        accentColor = Color(0xFF00E5FF),
                        onClick = onNavigateToContent,
                        modifier = Modifier.weight(1f)
                    )

                    NeumorphicControlTile(
                        title = "التصنيفات",
                        subtitle = "تقسيم القوائم",
                        icon = Icons.Default.Category,
                        isActive = false,
                        accentColor = Color(0xFFA371F7),
                        onClick = onNavigateToCategories,
                        modifier = Modifier.weight(1f)
                    )

                    NeumorphicControlTile(
                        title = "النسخ",
                        subtitle = "حفظ واسترجاع",
                        icon = Icons.Default.Backup,
                        isActive = false,
                        accentColor = Color(0xFFE3B341),
                        onClick = onNavigateToBackup,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Widgets Section with Search & Sort
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الودجات المصممة (${widgetConfigs.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFFF0F6FC)
                        )
                        Text(
                            text = "المعاينة الحية ⚡",
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF),
                            fontFamily = AppCustomFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (widgetConfigs.size > 2) {
                        OutlinedTextField(
                            value = widgetSearchQuery,
                            onValueChange = { widgetSearchQuery = it },
                            placeholder = { Text("بحث بين الودجات...", fontSize = 12.sp, fontFamily = AppCustomFontFamily) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF)) },
                            trailingIcon = if (widgetSearchQuery.isNotEmpty()) {
                                { IconButton(onClick = { widgetSearchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp)) } }
                            } else null,
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161B22),
                                unfocusedContainerColor = Color(0xFF161B22),
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF30363D)
                            ),
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
                                text = "ترتيب:",
                                fontSize = 11.sp,
                                fontFamily = AppCustomFontFamily,
                                color = Color(0xFF8B949E)
                            )
                            NeumorphicPillButton(
                                text = "الأحدث",
                                isSelected = true,
                                icon = Icons.Default.Schedule,
                                onClick = { viewModel.sortWidgetsNewestFirst() }
                            )
                            NeumorphicPillButton(
                                text = "الأقدم",
                                isSelected = false,
                                icon = Icons.Default.History,
                                onClick = { viewModel.sortWidgetsOldestFirst() }
                            )
                            NeumorphicPillButton(
                                text = "أبجدي",
                                isSelected = false,
                                icon = Icons.Default.SortByAlpha,
                                onClick = { viewModel.sortWidgetsAlphabetically() }
                            )
                        }
                    }
                }
            }

            if (filteredWidgetConfigs.isEmpty()) {
                item {
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (widgetSearchQuery.isNotEmpty()) "لا توجد ودجات مطابقة لبحثك" else "لم تقم بإنشاء ودجت حتى الآن",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = AppCustomFontFamily,
                                color = Color(0xFFF0F6FC)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "اضغط على زر 'إنشاء ودجت جديد' للبدء في تصميم ودجت مخصص بالألوان والخطوط والأحجام المتنوعة.",
                                fontSize = 12.sp,
                                fontFamily = AppCustomFontFamily,
                                color = Color(0xFF8B949E)
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

                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        glowColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
                    ) {
                        Column {
                            // Widget Order & Title Header
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF))
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = AppCustomFontFamily,
                                            color = Color(0xFF00E5FF),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = config.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFamily = AppCustomFontFamily,
                                        color = Color(0xFFF0F6FC)
                                    )
                                }

                                // Reorder Up / Down Controls
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.moveWidgetUp(config) },
                                        enabled = index > 0,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = Color(0xFF21262D)
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "نقل لأعلى", modifier = Modifier.size(16.dp), tint = Color(0xFFF0F6FC))
                                    }

                                    FilledTonalIconButton(
                                        onClick = { viewModel.moveWidgetDown(config) },
                                        enabled = index < widgetConfigs.size - 1,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = Color(0xFF21262D)
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "نقل لأسفل", modifier = Modifier.size(16.dp), tint = Color(0xFFF0F6FC))
                                    }
                                }
                            }

                            // Live Floating Preview Component
                            WidgetLivePreviewCard(
                                config = config,
                                items = items,
                                categoryName = catName,
                                onRefreshClick = { viewModel.advanceWidgetContent(config) },
                                onNextClick = { viewModel.advanceWidgetContent(config) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onNavigateToDesigner(config) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF0D1117)
                                    )
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تعديل وتصميم", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color(0xFFE3B341))
                                    }

                                    IconButton(onClick = { viewModel.copyWidgetConfig(config) }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = Color(0xFF00E5FF))
                                    }

                                    IconButton(onClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color(0xFFA371F7))
                                    }

                                    IconButton(onClick = { viewModel.deleteWidgetConfig(config) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFF85149))
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
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(26.dp),
            title = { Text("إنشاء ودجت مخصص جديد", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الودجت (مثال: أذكار المساء)", fontFamily = AppCustomFontFamily) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("حدد التصنيف المخصص للودجت", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedCatId == null,
                            onClick = { selectedCatId = null },
                            label = { Text("جميع التصنيفات", fontFamily = AppCustomFontFamily) }
                        )
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCatId == cat.id,
                                onClick = { selectedCatId = cat.id },
                                label = { Text(cat.name, fontFamily = AppCustomFontFamily) }
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0D1117)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("انتقال للمصمم الآن", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }
}
