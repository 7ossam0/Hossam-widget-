package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import com.example.data.prayer.DailyPrayerSchedule
import com.example.data.prayer.PrayerTime
import com.example.ui.components.*
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090D14))) {
        // 1. Dynamic Celestial Sky Live Background
        DynamicCelestialSky(
            phase = prayerSchedule.skyPhase,
            simulatedHourFraction = simulatedHour
        )

        // Refined deep luxury backdrop gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x35090D14),
                            Color(0x75090D14),
                            Color(0xEE090D14)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBarHeader(
                    schedule = prayerSchedule,
                    onRefresh = { viewModel.triggerWidgetRefreshBroadcast() },
                    onContactSupport = {
                        try {
                            val whatsappIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/201285610761")
                            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            context.startActivity(whatsappIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF090D14)) },
                    text = { Text("تصميم ودجت جديد", fontWeight = FontWeight.Bold, color = Color(0xFF090D14)) },
                    containerColor = Color(0xFFE5C07B),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFE5C07B))
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
            ) {
                // 1. Hero Atmospheric Prayer Times Card (Images 2 & 3 & 4 inspiration)
                item {
                    OrganizedPrayerHeroCard(
                        schedule = prayerSchedule,
                        onOpenPrayer = onNavigateToPrayer
                    )
                }

                // 2. Quran Reading & Weekly Habit Streak Card (Images 4 & 1 inspiration)
                item {
                    OrganizedQuranHeroBanner(
                        onOpenQuran = onNavigateToQuran
                    )
                }

                // 3. Structured Services & Features Grid (Images 1, 2, 4 inspiration)
                item {
                    OrganizedServicesGrid(
                        onNavigateToQuran = onNavigateToQuran,
                        onNavigateToPrayer = onNavigateToPrayer,
                        onNavigateToQibla = onNavigateToQibla,
                        onNavigateToTasbeeh = onNavigateToTasbeeh,
                        onNavigateToTasks = onNavigateToTasks,
                        onNavigateToAiWisdom = onNavigateToAiWisdom,
                        onNavigateToContent = onNavigateToContent,
                        onNavigateToBackup = onNavigateToBackup
                    )
                }

                // 4. Interactive Quick Tasbeeh Card (Fast Dhikr)
                if (activeTasbeeh != null) {
                    item {
                        OrganizedQuickTasbeehCard(
                            activeItem = activeTasbeeh!!,
                            onIncrement = { item -> viewModel.incrementTasbeeh(item.id) },
                            onOpenTasbeeh = onNavigateToTasbeeh
                        )
                    }
                }

                // 5. Daily Ayah & Spiritual Reflection Spotlight
                item {
                    OrganizedDailyAyahSpotlightCard(context = context)
                }

                // 6. Home Screen Widgets Studio Hub Header & Search
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33E5C07B))
                                        .border(1.dp, Color(0xFFE5C07B), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Widgets,
                                        contentDescription = null,
                                        tint = Color(0xFFE5C07B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "ودجات الشاشة الرئيسية (${widgetConfigs.size})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "تخصيص ومعاينة حية فورية",
                                        fontSize = 10.sp,
                                        color = Color(0xAAFFFFFF)
                                    )
                                }
                            }

                            TextButton(
                                onClick = onNavigateToCategories,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE5C07B))
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("التصنيفات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (widgetConfigs.size > 2) {
                            OutlinedTextField(
                                value = widgetSearchQuery,
                                onValueChange = { widgetSearchQuery = it },
                                placeholder = { Text("بحث بين الودجات المصممة...", fontSize = 12.sp, color = Color(0x88FFFFFF)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFFE5C07B)
                                    )
                                },
                                trailingIcon = if (widgetSearchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { widgetSearchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "مسح",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                } else null,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x20151C28),
                                    unfocusedContainerColor = Color(0x15151C28),
                                    focusedBorderColor = Color(0xFFE5C07B),
                                    unfocusedBorderColor = Color(0x25FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 7. Active Widgets List or Clean Empty State
                if (filteredWidgetConfigs.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            borderBrush = GlassDefaults.accentBorderGradient(Color(0xFFE5C07B).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = Color(0xFFE5C07B)
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
                                    "اضغط على زر 'تصميم ودجت جديد' لإنشاء ودجت أنيق لشاشتك الرئيسية بألوان زجاجية وخطوط عربية مخصصة.",
                                    fontSize = 12.sp,
                                    color = Color(0xCCFFFFFF),
                                    textAlign = TextAlign.Center
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
                            borderBrush = GlassDefaults.accentBorderGradient(Color(0xFFE5C07B).copy(alpha = 0.45f)),
                            elevation = 6.dp
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
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x33E5C07B))
                                                .border(1.dp, Color(0xFFE5C07B), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFE5C07B)
                                            )
                                        }
                                        Text(
                                            text = config.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
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
                                            size = 32.dp
                                        )
                                        GlassIconButton(
                                            icon = Icons.Default.ArrowDownward,
                                            onClick = { viewModel.moveWidgetDown(config) },
                                            size = 32.dp
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

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons Footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassButton(
                                        text = "تعديل وتصميم",
                                        icon = Icons.Default.Palette,
                                        accentColor = Color(0xFFE5C07B),
                                        onClick = { onNavigateToDesigner(config) }
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        GlassIconButton(
                                            icon = Icons.Default.Share,
                                            size = 36.dp,
                                            accentGlow = Color(0xFFE5C07B),
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
                                            size = 36.dp,
                                            accentGlow = Color(0xFF00E5FF),
                                            onClick = { viewModel.copyWidgetConfig(config) }
                                        )

                                        GlassIconButton(
                                            icon = Icons.Default.Delete,
                                            size = 36.dp,
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

                // 8. Developer & Contact Support Footer Card
                item {
                    OrganizedDeveloperCard(context = context)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C07B), contentColor = Color(0xFF090D14))
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

/**
 * شريط العنوان العلوي المنظم مع التاريخ الهجري والميلادي وشعار بيان
 */
@Composable
private fun HomeTopBarHeader(
    schedule: DailyPrayerSchedule,
    onRefresh: () -> Unit,
    onContactSupport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE5C07B), Color(0xFFB8860B))
                        )
                    )
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mosque,
                    contentDescription = null,
                    tint = Color(0xFF090D14),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "بَيَان",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BAYAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFE5C07B)
                    )
                }
                Text(
                    text = "${schedule.cityName} • ${schedule.skyPhase.phaseNameArabic}",
                    fontSize = 11.sp,
                    color = Color(0xBBFFFFFF)
                )
            }
        }

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x251C2534),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30E5C07B)),
                modifier = Modifier.clickable { onContactSupport() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "دعم مباشر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            GlassIconButton(
                icon = Icons.Default.Refresh,
                onClick = onRefresh,
                contentDescription = "تحديث الكل",
                size = 38.dp,
                accentGlow = Color(0xFFE5C07B)
            )
        }
    }
}

/**
 * بطاقة مواقيت الصلاة المنظمة المستوحاة من الصورة 2 و 3
 */
@Composable
private fun OrganizedPrayerHeroCard(
    schedule: DailyPrayerSchedule,
    onOpenPrayer: () -> Unit
) {
    val gradientColors = when (schedule.nextPrayer.id) {
        "fajr" -> listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
        "sunrise", "dhuhr" -> listOf(Color(0xFF0C4A6E), Color(0xFF075985), Color(0xFF0369A1))
        "asr" -> listOf(Color(0xFF78350F), Color(0xFF92400E), Color(0xFFB45309))
        "maghrib" -> listOf(Color(0xFF4C1D95), Color(0xFF581C87), Color(0xFF831843))
        else -> listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF1E1B4B))
    }

    val prayersList = remember(schedule) {
        listOf(schedule.fajr, schedule.sunrise, schedule.dhuhr, schedule.asr, schedule.maghrib, schedule.isha)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPrayer() }
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFE5C07B).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0x80E5C07B), Color(0x20FFFFFF), Color(0x80E5C07B))))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header row: Next prayer title & pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33E5C07B))
                                .border(1.dp, Color(0xFFE5C07B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFE5C07B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "الصلاة القادمة",
                                fontSize = 11.sp,
                                color = Color(0xCCFFFFFF)
                            )
                            Text(
                                text = "صلاة ${schedule.nextPrayer.nameArabic}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Next Prayer Time Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x50E5C07B))
                    ) {
                        Text(
                            text = schedule.nextPrayer.timeFormatted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE5C07B),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Center Countdown Timer Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x25000000))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المتبقي للأذان",
                            fontSize = 11.sp,
                            color = Color(0xAAFFFFFF)
                        )
                        Text(
                            text = schedule.remainingFormatted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "عرض المواقيت",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFE5C07B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Horizontal 5-Prayer Timeline Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    prayersList.forEach { item ->
                        val isNext = item.id == schedule.nextPrayer.id
                        PrayerTimeMiniItem(
                            item = item,
                            isNext = isNext
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeMiniItem(
    item: PrayerTime,
    isNext: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNext) Color(0xFFE5C07B) else Color(0x25000000),
            border = if (isNext) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.nameArabic,
                    fontSize = 11.sp,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isNext) Color(0xFF090D14) else Color.White
                )
                Text(
                    text = item.timeFormatted.replace(" ص", "").replace(" م", ""),
                    fontSize = 10.sp,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = if (isNext) Color(0xFF090D14) else Color(0xBBFFFFFF)
                )
            }
        }
    }
}

/**
 * بطاقة الورد القرآني وختمة التلاوة المنظمة المستوحاة من الصورة 4 و 1
 */
@Composable
private fun OrganizedQuranHeroBanner(
    onOpenQuran: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenQuran() }
            .shadow(10.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x35E5C07B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B1017))
                            .border(1.dp, Color(0x40E5C07B), RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_quran_hero_1787539290776),
                            contentDescription = "المصحف الشريف",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x2210B981)
                        ) {
                            Text(
                                text = "متابعة الورد اليومي",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "المصحف الشريف والتفسير",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "الرسم العثماني • ١١٤ سورة كاملة",
                            fontSize = 11.sp,
                            color = Color(0xAAFFFFFF)
                        )
                    }
                }

                FilledIconButton(
                    onClick = onOpenQuran,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFE5C07B),
                        contentColor = Color(0xFF090D14)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "فتح المصحف",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1FFFFFFF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Weekly Reading Streak Indicators (Image 4 inspiration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
                days.forEachIndexed { idx, dayName ->
                    val isDone = idx in 0..4 // Visual active streak
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Color(0xFF10B981) else Color(0x20FFFFFF))
                                .border(1.dp, if (isDone) Color(0xFF10B981) else Color(0x30FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = dayName.take(3),
                            fontSize = 9.sp,
                            color = if (isDone) Color(0xFF10B981) else Color(0x77FFFFFF),
                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/**
 * شبكة الخدمات الأساسية المرتبة بأناقة وتناغم (Organized Services Grid)
 */
@Composable
private fun OrganizedServicesGrid(
    onNavigateToQuran: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAiWisdom: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "الخدمات والخصائص الروحية",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Row 1: المصحف الشريف | القبلة AR | مواقيت الصلاة | المسبحة الذكية
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrganizedServiceTile(
                title = "المصحف",
                subtitle = "تفسير ميسر",
                icon = Icons.Default.MenuBook,
                accentColor = Color(0xFF10B981),
                onClick = onNavigateToQuran,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "القبلة AR",
                subtitle = "بوصلة فلكية",
                icon = Icons.Default.Explore,
                accentColor = Color(0xFF00E5FF),
                onClick = onNavigateToQibla,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "مواقيت الصلاة",
                subtitle = "الأذان والنداء",
                icon = Icons.Default.Mosque,
                accentColor = Color(0xFFE5C07B),
                onClick = onNavigateToPrayer,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "المسبحة",
                subtitle = "تسبيح ذكي",
                icon = Icons.Default.Adjust,
                accentColor = Color(0xFFEC4899),
                onClick = onNavigateToTasbeeh,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: مهام الطاعات | بيان الذكي | حصن المسلم | النسخ الاحتياطي
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrganizedServiceTile(
                title = "مهام الطاعات",
                subtitle = "جدول العبادات",
                icon = Icons.Default.EventAvailable,
                accentColor = Color(0xFFF59E0B),
                onClick = onNavigateToTasks,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "بيان الذكي",
                subtitle = "تدبر وبحث",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFA855F7),
                onClick = onNavigateToAiWisdom,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "حصن المسلم",
                subtitle = "أذكار وأدعية",
                icon = Icons.Default.Article,
                accentColor = Color(0xFF6366F1),
                onClick = onNavigateToContent,
                modifier = Modifier.weight(1f)
            )

            OrganizedServiceTile(
                title = "النسخ السحابي",
                subtitle = "تصدير وحفظ",
                icon = Icons.Default.Backup,
                accentColor = Color(0xFF14B8A6),
                onClick = onNavigateToBackup,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OrganizedServiceTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131A26),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0x99FFFFFF),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * بطاقة التسبيح السريع التفاعلية
 */
@Composable
private fun OrganizedQuickTasbeehCard(
    activeItem: com.example.data.model.TasbeehEntity,
    onIncrement: (com.example.data.model.TasbeehEntity) -> Unit,
    onOpenTasbeeh: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(activeItem.colorHex))
    } catch (e: Exception) {
        Color(0xFFE5C07B)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF131A26),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenTasbeeh() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المسبحة الإلكترونية السريعة",
                        color = Color(0xCCFFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeItem.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = if (activeItem.targetCount > 0) "الهدف: ${activeItem.targetCount} تسبيحة" else "تسبيح مستمر",
                    color = Color(0x99FFFFFF),
                    fontSize = 11.sp
                )
            }

            // Interactive Circle Tap Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor.copy(alpha = 0.35f),
                                Color(0x22131A26)
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
                        fontSize = 18.sp
                    )
                    Text(
                        text = "+1",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * بطاقة آية وتدبر اليوم
 */
@Composable
private fun OrganizedDailyAyahSpotlightCard(context: Context) {
    val ayahText = "﴿إِنَّ ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّـٰلِحَـٰتِ سَيَجْعَلُ لَهُمُ ٱلرَّحْمَـٰنُ وُدًّۭا﴾"
    val surahRef = "سورة مريم - الآية ٩٦"
    val tafsirNote = "أي سيجعل الله لهم محبة وقبولاً في قلوب عباده الصالحين في الدنيا والآخرة."

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF131A26),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x40E5C07B)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE5C07B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "آية وتدبر اليوم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5C07B)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Daily Ayah", "$ayahText\n$surahRef\n$tafsirNote")
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color(0xBBFFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "آية وتدبر")
                                putExtra(Intent.EXTRA_TEXT, "$ayahText\n$surahRef\n$tafsirNote")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الآية"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = Color(0xBBFFFFFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = ayahText,
                fontFamily = com.example.ui.theme.QuranHafsFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "$surahRef • $tafsirNote",
                fontSize = 11.sp,
                color = Color(0xAAFFFFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * بطاقة دعم المطور المباشرة
 */
@Composable
private fun OrganizedDeveloperCard(context: Context) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF131A26),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x25E5C07B)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x22E5C07B))
                            .border(1.dp, Color(0xFFE5C07B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFE5C07B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المصمم والمطور: حسام أحمد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "مصر (+20) 01285610761",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B)
                        )
                    }
                }

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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
