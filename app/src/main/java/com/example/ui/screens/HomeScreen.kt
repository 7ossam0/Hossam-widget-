package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onNavigateToBackup: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToQuran: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAiWisdom: () -> Unit = {}
) {
    val widgetConfigs by viewModel.widgetConfigs.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val prayerSchedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val simulatedHour by viewModel.simulatedHour.collectAsStateWithLifecycle()
    val activeTasbeeh by viewModel.activeTasbeeh.collectAsStateWithLifecycle()

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

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dynamic Celestial Sky Live Background
        DynamicCelestialSky(
            phase = prayerSchedule.skyPhase,
            simulatedHourFraction = simulatedHour
        )

        // Frosted glass gradient dark backdrop for clear contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x30000000),
                            Color(0x60000000),
                            Color(0x88000000)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x3300E5FF))
                                    .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    "استوديو الودجت الذكي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "DYNAMIC CELESTIAL & WIDGET STUDIO",
                                    fontSize = 8.sp,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    },
                    actions = {
                        GlassIconButton(
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.triggerWidgetRefreshBroadcast() },
                            contentDescription = "تحديث الكل",
                            size = 40.dp
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0D1117)) },
                    text = { Text("تصميم ودجت جديد", fontWeight = FontWeight.Bold, color = Color(0xFF0D1117)) },
                    containerColor = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00E5FF))
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
            ) {
                // 1. Hero Dynamic Prayer & Celestial Sky Glass Card
                item {
                    HeroPrayerSkyGlassBanner(
                        schedule = prayerSchedule,
                        onOpenPrayerScreen = onNavigateToPrayer
                    )
                }

                // 2. Hero Interactive Mini Tasbeeh Glass Widget
                item {
                    HeroQuickTasbeehGlassCard(
                        activeItem = activeTasbeeh,
                        onIncrement = { item -> viewModel.incrementTasbeeh(item.id) },
                        onOpenTasbeehScreen = onNavigateToTasbeeh
                    )
                }

                // 3. Glassmorphic Navigation Quick Hub (Core Feature Pillars)
                item {
                    Text(
                        text = "الخدمات والخصائص الأساسية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    
                    // Row 1: المصحف الشريف & القبلة AR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavGlassTile(
                            title = "المصحف والتفسير",
                            subtitle = "رسم عثماني وتفسير ميسر",
                            icon = Icons.Default.MenuBook,
                            accentColor = Color(0xFF10B981),
                            onClick = onNavigateToQuran,
                            modifier = Modifier.weight(1f)
                        )

                        QuickNavGlassTile(
                            title = "القبلة بالواقع المعزز",
                            subtitle = "بوصلة فلكية و AR",
                            icon = Icons.Default.Explore,
                            accentColor = Color(0xFF00E5FF),
                            onClick = onNavigateToQibla,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: المهام والجدول الروحي & مساعد بيان الذكي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavGlassTile(
                            title = "مهام الصلاة والعادات",
                            subtitle = "جدولة المهام فلكياً",
                            icon = Icons.Default.EventAvailable,
                            accentColor = Color(0xFFF59E0B),
                            onClick = onNavigateToTasks,
                            modifier = Modifier.weight(1f)
                        )

                        QuickNavGlassTile(
                            title = "مساعد بيان الذكي",
                            subtitle = "بحث وتدبر موثق (RAG)",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = Color(0xFF8B5CF6),
                            onClick = onNavigateToAiWisdom,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: المسبحة & الأذكار & النسخ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickNavGlassTile(
                            title = "المسبحة الذكية",
                            subtitle = "عداد وأهداف",
                            icon = Icons.Default.Adjust,
                            accentColor = Color(0xFFEC4899),
                            onClick = onNavigateToTasbeeh,
                            modifier = Modifier.weight(1f)
                        )

                        QuickNavGlassTile(
                            title = "الأذكار والمحتوى",
                            subtitle = "إدارة النصوص",
                            icon = Icons.Default.Article,
                            accentColor = Color(0xFF6366F1),
                            onClick = onNavigateToContent,
                            modifier = Modifier.weight(1f)
                        )

                        QuickNavGlassTile(
                            title = "النسخ والاحتياط",
                            subtitle = "تصدير واستيراد",
                            icon = Icons.Default.Backup,
                            accentColor = Color(0xFF14B8A6),
                            onClick = onNavigateToBackup,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Developer Support & Custom Design Info Card
                item {
                    DeveloperGlassCard(context = context)
                }

                // 5. Designed Widgets Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ودجات الشاشة الرئيسية (${widgetConfigs.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "معاينة حية زجاجية ⚡",
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (widgetConfigs.size > 2) {
                            OutlinedTextField(
                                value = widgetSearchQuery,
                                onValueChange = { widgetSearchQuery = it },
                                placeholder = { Text("بحث بين الودجات المصممة...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF)) },
                                trailingIcon = if (widgetSearchQuery.isNotEmpty()) {
                                    { IconButton(onClick = { widgetSearchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp)) } }
                                } else null,
                                shape = RoundedCornerShape(18.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x22FFFFFF),
                                    unfocusedContainerColor = Color(0x15FFFFFF),
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (filteredWidgetConfigs.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF00E5FF))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    if (widgetSearchQuery.isNotEmpty()) "لا توجد ودجات مطابقة للبحث" else "لم تقم بإنشاء ودجات حتى الآن",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "اضغط على زر 'تصميم ودجت جديد' لإنشاء وتخصيص ويدجت بأشكال وخطوط وألوان وخلفيات زجاجية مميزة.",
                                    fontSize = 12.sp,
                                    color = Color(0xCCFFFFFF),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF00E5FF).copy(alpha = 0.6f)),
                            elevation = 8.dp
                        ) {
                            Column {
                                // Widget Order & Title Header
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x3300E5FF))
                                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF00E5FF)
                                            )
                                        }
                                        Text(
                                            text = config.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }

                                    // Move Up / Down
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GlassIconButton(
                                            icon = Icons.Default.ArrowUpward,
                                            onClick = { viewModel.moveWidgetUp(config) },
                                            size = 34.dp
                                        )
                                        GlassIconButton(
                                            icon = Icons.Default.ArrowDownward,
                                            onClick = { viewModel.moveWidgetDown(config) },
                                            size = 34.dp
                                        )
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

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons Footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassButton(
                                        text = "تعديل وتصميم",
                                        icon = Icons.Default.Palette,
                                        accentColor = Color(0xFF00E5FF),
                                        onClick = { onNavigateToDesigner(config) }
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        GlassIconButton(
                                            icon = Icons.Default.Share,
                                            size = 38.dp,
                                            accentGlow = Color(0xFFFFD54F),
                                            onClick = {
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
                                            }
                                        )

                                        GlassIconButton(
                                            icon = Icons.Default.ContentCopy,
                                            size = 38.dp,
                                            accentGlow = Color(0xFF00E5FF),
                                            onClick = { viewModel.copyWidgetConfig(config) }
                                        )

                                        GlassIconButton(
                                            icon = Icons.Default.Delete,
                                            size = 38.dp,
                                            tint = Color(0xFFEF4444),
                                            accentGlow = Color(0xFFEF4444),
                                            onClick = { viewModel.deleteWidgetConfig(config) }
                                        )
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
            title = { Text("إنشاء ودجت مخصص جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الودجت (مثال: أذكار الصباح والمساء)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("حدد التصنيف المخصص للودجت:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0D1117))
                ) {
                    Text("انتقال للمصمم الآن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun HeroPrayerSkyGlassBanner(
    schedule: com.example.data.prayer.DailyPrayerSchedule,
    onOpenPrayerScreen: () -> Unit
) {
    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFFFFD54F)),
        onClick = onOpenPrayerScreen,
        elevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFD54F))
                        .border(1.2.dp, Color(0xFFFFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "الصلاة القادمة: صلاة ${schedule.nextPrayer.nameArabic}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${schedule.cityName} • ${schedule.skyPhase.phaseNameArabic}",
                        color = Color(0xFFFFE082),
                        fontSize = 12.sp
                    )
                }
            }

            // Countdown Pill
            Box(
                modifier = Modifier
                    .clip(GlassDefaults.PillShape)
                    .background(Color(0x3300E5FF))
                    .border(1.dp, Color(0xFF00E5FF), GlassDefaults.PillShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "متبقي ${schedule.remainingFormatted}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun HeroQuickTasbeehGlassCard(
    activeItem: com.example.data.model.TasbeehEntity?,
    onIncrement: (com.example.data.model.TasbeehEntity) -> Unit,
    onOpenTasbeehScreen: () -> Unit
) {
    if (activeItem == null) return

    val accentColor = try {
        Color(android.graphics.Color.parseColor(activeItem.colorHex))
    } catch (e: Exception) {
        Color(0xFF00E5FF)
    }

    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(accentColor),
        elevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenTasbeehScreen() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المسبحة الإلكترونية السريعة",
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeItem.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = if (activeItem.targetCount > 0) "الهدف: ${activeItem.targetCount} مرة" else "عداد حر",
                    color = Color(0xAAFFFFFF),
                    fontSize = 11.sp
                )
            }

            // Quick Tap Action Counter Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor.copy(alpha = 0.4f),
                                Color(0x22FFFFFF)
                            )
                        )
                    )
                    .border(2.dp, accentColor, CircleShape)
                    .clickable { onIncrement(activeItem) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeItem.currentCount}",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "+1 تسبيح",
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickNavGlassTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        borderBrush = GlassDefaults.accentBorderGradient(accentColor.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.22f))
                    .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color(0xAAFFFFFF),
                fontSize = 9.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DeveloperGlassCard(context: Context) {
    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF00E5FF))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5FF))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المصمم والمطور: حسام أحمد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "مصر (+20) 01285610761",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(GlassDefaults.PillShape)
                        .background(Color(0x3300E5FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("دعم مباشر ✨", fontSize = 10.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            }

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
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(whatsappIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("واتساب (+20)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                GlassIconButton(
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("WhatsApp Number", "+201285610761")
                        clipboard.setPrimaryClip(clip)
                    },
                    size = 42.dp,
                    contentDescription = "نسخ الرقم"
                )
            }
        }
    }
}
