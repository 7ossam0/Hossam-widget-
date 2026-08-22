package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TasbeehEntity
import com.example.ui.components.*
import com.example.ui.theme.AppCustomFontFamily
import com.example.viewmodel.MainViewModel
import com.example.widgets.TasbeehAppWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TasbeehScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val schedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val simulatedHour by viewModel.simulatedHour.collectAsStateWithLifecycle()
    val tasbeehItems by viewModel.tasbeehItems.collectAsStateWithLifecycle()
    val activeTasbeeh by viewModel.activeTasbeeh.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var soundEnabled by remember { mutableStateOf(true) }
    var hapticEnabled by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }

    // Hydraulic button scale bounce animation state
    val buttonScale = remember { Animatable(1f) }

    val currentDhikr = activeTasbeeh ?: tasbeehItems.firstOrNull()

    // Tone Generator for click sound
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            null
        }
    }

    // Vibrator
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerFeedback(isTargetReached: Boolean) {
        if (soundEnabled && toneGenerator != null) {
            try {
                if (isTargetReached) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
                } else {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                }
            } catch (e: Exception) {
                // Ignore audio failure gracefully
            }
        }
        if (hapticEnabled && vibrator != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isTargetReached) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1))
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isTargetReached) 150 else 30)
                }
            } catch (e: Exception) {
                // Ignore vibration failure
            }
        }
    }

    fun handleCountTap() {
        if (currentDhikr == null) return
        val nextCount = currentDhikr.currentCount + 1
        val target = currentDhikr.targetCount
        val reached = target > 0 && nextCount % target == 0

        coroutineScope.launch {
            // Button tactile bounce
            buttonScale.animateTo(0.92f, animationSpec = tween(60, easing = FastOutSlowInEasing))
            buttonScale.animateTo(1.04f, animationSpec = tween(80, easing = FastOutSlowInEasing))
            buttonScale.animateTo(1f, animationSpec = tween(60))
        }

        triggerFeedback(reached)
        viewModel.incrementTasbeeh(currentDhikr.id)

        if (reached) {
            showCelebration = true
            coroutineScope.launch {
                delay(2200)
                showCelebration = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dynamic Live Celestial Sky Background
        DynamicCelestialSky(
            phase = schedule.skyPhase,
            simulatedHourFraction = simulatedHour
        )

        // Dark translucent glass overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x25000000),
                            Color(0x55000000),
                            Color(0x88000000)
                        )
                    )
                )
        )

        // 2. Main Tasbeeh Glass Interface
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TasbeehTopAppBar(
                    soundEnabled = soundEnabled,
                    hapticEnabled = hapticEnabled,
                    onToggleSound = { soundEnabled = !soundEnabled },
                    onToggleHaptic = { hapticEnabled = !hapticEnabled },
                    onNavigateBack = onNavigateBack,
                    onAddNewDhikr = { showAddDialog = true }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp)
            ) {
                // Dhikr Selector Carousel
                item {
                    DhikrSelectionCarousel(
                        items = tasbeehItems,
                        activeItem = currentDhikr,
                        onSelectItem = { viewModel.selectTasbeeh(it.id) }
                    )
                }

                // Main Interactive Hydraulic Counter Glass Sphere
                item {
                    if (currentDhikr != null) {
                        InteractiveTasbeehCounter(
                            item = currentDhikr,
                            buttonScale = buttonScale.value,
                            showCelebration = showCelebration,
                            onCountTap = { handleCountTap() },
                            onResetClick = { showResetConfirm = true }
                        )
                    }
                }

                // Quick Goals & Target Selector
                item {
                    if (currentDhikr != null) {
                        TargetGoalSelector(
                            currentGoal = currentDhikr.targetCount,
                            onSelectGoal = { goal ->
                                viewModel.updateTasbeeh(currentDhikr.copy(targetCount = goal))
                            }
                        )
                    }
                }

                // Lifetime Statistics & Achievements Glass Card
                item {
                    if (currentDhikr != null) {
                        TasbeehStatsGlassCard(
                            item = currentDhikr,
                            allItems = tasbeehItems
                        )
                    }
                }

                // Widget Control & Lock Settings
                item {
                    TasbeehWidgetControlsCard(
                        context = context,
                        onRefreshWidgets = {
                            TasbeehAppWidgetProvider.updateTasbeehWidgets(context)
                        }
                    )
                }
            }
        }
    }

    // Add Custom Dhikr Dialog
    if (showAddDialog) {
        AddCustomDhikrDialog(
            onAdd = { title, subtitle, goal, color ->
                viewModel.addCustomTasbeeh(title, subtitle, goal, color)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirm && currentDhikr != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("تصفير العداد", fontWeight = FontWeight.Bold) },
            text = { Text("هل ترغب في إعادة ضبط عداد \"${currentDhikr.title}\" إلى الصفر؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTasbeeh(currentDhikr.id)
                        showResetConfirm = false
                    }
                ) {
                    Text("نعم، تصفير", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TasbeehTopAppBar(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddNewDhikr: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GlassIconButton(
            icon = Icons.Default.ArrowForward,
            onClick = onNavigateBack,
            contentDescription = "رجوع",
            size = 42.dp
        )

        Text(
            text = "المسبحة الذكية التفاعلية 📿",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 17.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Sound toggle
            GlassIconButton(
                icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                onClick = onToggleSound,
                contentDescription = "الصوت",
                size = 40.dp,
                accentGlow = if (soundEnabled) Color(0xFF00E5FF) else null
            )

            // Haptic toggle
            GlassIconButton(
                icon = if (hapticEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                onClick = onToggleHaptic,
                contentDescription = "الاهتزاز",
                size = 40.dp,
                accentGlow = if (hapticEnabled) Color(0xFF10B981) else null
            )

            // Add custom
            GlassIconButton(
                icon = Icons.Default.Add,
                onClick = onAddNewDhikr,
                contentDescription = "إضافة ذكر جديد",
                size = 40.dp,
                accentGlow = Color(0xFFFFD54F)
            )
        }
    }
}

@Composable
private fun DhikrSelectionCarousel(
    items: List<TasbeehEntity>,
    activeItem: TasbeehEntity?,
    onSelectItem: (TasbeehEntity) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items) { item ->
            val isSelected = activeItem?.id == item.id
            val accentColor = try {
                Color(android.graphics.Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                Color(0xFF00E5FF)
            }

            GlassChip(
                text = item.title,
                isSelected = isSelected,
                accentColor = accentColor,
                onClick = { onSelectItem(item) }
            )
        }
    }
}

@Composable
private fun InteractiveTasbeehCounter(
    item: TasbeehEntity,
    buttonScale: Float,
    showCelebration: Boolean,
    onCountTap: () -> Unit,
    onResetClick: () -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        Color(0xFF00E5FF)
    }

    val progress = if (item.targetCount > 0) {
        (item.currentCount % item.targetCount).toFloat() / item.targetCount.toFloat()
    } else 1.0f

    val completedRounds = if (item.targetCount > 0) item.currentCount / item.targetCount else 0

    GlassCard(
        elevation = 16.dp,
        borderBrush = GlassDefaults.accentBorderGradient(accentColor),
        backgroundBrush = Brush.verticalGradient(
            listOf(
                Color(0x35FFFFFF),
                Color(0x1AFFFFFF),
                Color(0x10000000)
            )
        ),
        contentPadding = PaddingValues(20.dp)
    ) {
        // Dhikr Title & Meaning
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.title,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            if (item.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    color = Color(0xCCFFFFFF),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Circular Hydraulic Tap Area
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .shadow(
                        elevation = if (showCelebration) 30.dp else 12.dp,
                        shape = CircleShape,
                        spotColor = accentColor
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor.copy(alpha = if (showCelebration) 0.5f else 0.28f),
                                Color(0x22FFFFFF),
                                Color(0x0A000000)
                            )
                        )
                    )
                    .border(
                        width = if (showCelebration) 3.dp else 2.dp,
                        brush = GlassDefaults.accentBorderGradient(if (showCelebration) Color(0xFFFFD54F) else accentColor),
                        shape = CircleShape
                    )
                    .clickable(onClick = onCountTap)
                    .testTag("tasbeeh_count_button"),
                contentAlignment = Alignment.Center
            ) {
                // Glowing Progress Ring Arc
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    // Background track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Progress arc
                    if (item.targetCount > 0) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accentColor.copy(alpha = 0.6f), accentColor, Color.White)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Inner Counter Numbers
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.currentCount}",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 58.sp,
                        letterSpacing = (-1).sp
                    )

                    if (item.targetCount > 0) {
                        Text(
                            text = "الهدف: ${item.targetCount}",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (completedRounds > 0) {
                            Text(
                                text = "✨ أتممت $completedRounds ${if (completedRounds == 1) "دورة" else "دورات"}",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "عداد مفتوح",
                            color = Color(0xAAFFFFFF),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "انقر للتسبيح",
                        color = Color(0x88FFFFFF),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Reset, Quick +10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    text = "تصفير العداد",
                    icon = Icons.Default.Refresh,
                    accentColor = Color(0xFFEF4444),
                    onClick = onResetClick
                )
            }
        }
    }
}

@Composable
private fun TargetGoalSelector(
    currentGoal: Int,
    onSelectGoal: (Int) -> Unit
) {
    GlassCard {
        Text(
            text = "تحديد دورة التسبيح (الهدف):",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val goals = listOf(33 to "33 مرة", 99 to "99 مرة", 100 to "100 مرة", 0 to "مفتوح ∞")
            goals.forEach { (goal, label) ->
                val isSelected = currentGoal == goal
                Box(modifier = Modifier.weight(1f)) {
                    GlassChip(
                        text = label,
                        isSelected = isSelected,
                        onClick = { onSelectGoal(goal) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TasbeehStatsGlassCard(
    item: TasbeehEntity,
    allItems: List<TasbeehEntity>
) {
    val totalAllTasbeeh = allItems.sumOf { it.totalLifetimeCount }

    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFFFFD54F))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "سجل الإنجاز والبركة",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatMetricItem(
                title = "مجموع هذا الذكر",
                value = "${item.totalLifetimeCount}",
                color = Color(0xFF00E5FF)
            )
            StatMetricItem(
                title = "إجمالي كل الأذكار",
                value = "$totalAllTasbeeh",
                color = Color(0xFF10B981)
            )
            StatMetricItem(
                title = "الدورات المكتملة",
                value = if (item.targetCount > 0) "${item.totalLifetimeCount / item.targetCount}" else "—",
                color = Color(0xFFFFD54F)
            )
        }
    }
}

@Composable
private fun StatMetricItem(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            color = color,
            fontSize = 20.sp
        )
        Text(
            text = title,
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AddCustomDhikrDialog(
    onAdd: (title: String, subtitle: String, goal: Int, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(33) }
    var colorHex by remember { mutableStateOf("#00E5FF") }

    val presetColors = listOf("#00E5FF", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#F43F5E")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة ذكر جديد للمسبحة 📿", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نص الذكر (مثال: سبحان الله وبحمده)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("فضل الذكر أو معناه (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الهدف المفضل:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(33, 99, 100, 1000).forEach { g ->
                        val isSel = goal == g
                        FilterChip(
                            selected = isSel,
                            onClick = { goal = g },
                            label = { Text("$g") }
                        )
                    }
                }

                Text("لون الهوية:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    presetColors.forEach { cHex ->
                        val c = Color(android.graphics.Color.parseColor(cHex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    if (colorHex == cHex) 3.dp else 1.dp,
                                    if (colorHex == cHex) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { colorHex = cHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, subtitle, goal, colorHex)
                },
                enabled = title.isNotBlank()
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun TasbeehWidgetControlsCard(
    context: Context,
    onRefreshWidgets: () -> Unit
) {
    var isLocked by remember { mutableStateOf(TasbeehAppWidgetProvider.isLockOpenAppEnabled(context)) }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        glowColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "التحكم في ويدجت المسبحة والشاشة الرئيسية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = AppCustomFontFamily,
                        color = Color.White
                    )
                }
            }

            // Lock App Opening Switch
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isLocked) Color(0xFF00E5FF).copy(alpha = 0.12f) else Color(0x22FFFFFF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLocked) Color(0xFF00E5FF).copy(alpha = 0.6f) else Color(0x33FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isLocked) Color(0xFF00E5FF) else Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLocked) "قفل فتح التطبيق (مُفعّل - تسبيح هادئ)" else "فتح التطبيق عند الضغط على الودجت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = AppCustomFontFamily,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isLocked)
                                "عند الضغط على أرقام الودجت أو الزر، يتم التسبيح (+1) فوراً دون مغادرة الشاشة الرئيسية أو فتح التطبيق."
                            else
                                "الضغط على الودجت سيفتح التطبيق.",
                            fontSize = 10.sp,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFFB0BEC5),
                            lineHeight = 14.sp
                        )
                    }
                    Switch(
                        checked = isLocked,
                        onCheckedChange = {
                            isLocked = it
                            TasbeehAppWidgetProvider.setLockOpenAppEnabled(context, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF0D1117),
                            checkedTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }

            // Quick Tips & Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 نصيحة: يمكنك النقر مباشرة على الأرقام للتسبيح واستخدام الأسهم ◀ ▶ للتبديل بين الأذكار.",
                    fontSize = 10.sp,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFF8B949E),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onRefreshWidgets,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF21262D),
                        contentColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحديث الويدجت", fontSize = 11.sp, fontFamily = AppCustomFontFamily)
                }
            }
        }
    }
}

