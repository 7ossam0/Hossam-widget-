package com.example.ui.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.audio.AudioStorageManager
import com.example.data.audio.AudioType
import com.example.data.audio.CustomAudioItem
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.CityLocation
import com.example.data.prayer.PrayerTime
import com.example.data.prayer.PrayerTimeCalculator
import com.example.services.PrayerNotificationHelper
import com.example.ui.components.DynamicCelestialSky
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassDefaults
import com.example.ui.components.GlassIconButton
import com.example.viewmodel.MainViewModel

@Composable
fun PrayerTimesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val schedule by viewModel.prayerSchedule.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedMethod by viewModel.selectedCalculationMethod.collectAsStateWithLifecycle()
    val simulatedHour by viewModel.simulatedHour.collectAsStateWithLifecycle()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showSkyExplorer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Dynamic Celestial Sky Background (Live 24h cycle)
        DynamicCelestialSky(
            phase = schedule.skyPhase,
            simulatedHourFraction = simulatedHour
        )

        // Subtle dark translucent gradient overlay to keep text hyper-readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x22000000),
                            Color(0x44000000),
                            Color(0x77000000)
                        )
                    )
                )
        )

        // 2. Main Glass Content
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PrayerTopAppBar(
                    schedule = schedule,
                    selectedCity = selectedCity,
                    onCityClick = { showCityDialog = true },
                    onNavigateBack = onNavigateBack,
                    onToggleSkyExplorer = { showSkyExplorer = !showSkyExplorer }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
            ) {
                // Interactive Sky Explorer Slider (Expandable)
                if (showSkyExplorer) {
                    item {
                        SkyExplorerGlassCard(
                            simulatedHour = simulatedHour,
                            onHourChange = { viewModel.setSimulatedHour(it) },
                            onResetRealTime = { viewModel.setSimulatedHour(null) }
                        )
                    }
                }

                // Next Prayer Hero Glass Card
                item {
                    NextPrayerHeroCard(
                        schedule = schedule,
                        onMethodClick = { showMethodDialog = true }
                    )
                }

                // Daily Prayers Schedule Grid
                item {
                    Text(
                        text = "مواقيت صلوات اليوم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                item {
                    PrayerScheduleGrid(schedule = schedule)
                }

                // Qibla Compass Glass Card
                item {
                    QiblaCompassCard(
                        schedule = schedule,
                        cityName = selectedCity.cityName
                    )
                }

                // Adhan, Audio Upload & Notification Settings
                item {
                    AdhanAndNotificationSettingsCard()
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        CitySelectionDialog(
            currentCity = selectedCity,
            onSelectCity = {
                viewModel.selectCity(it)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }

    // Method Selection Dialog
    if (showMethodDialog) {
        MethodSelectionDialog(
            currentMethod = selectedMethod,
            onSelectMethod = {
                viewModel.setCalculationMethod(it)
                showMethodDialog = false
            },
            onDismiss = { showMethodDialog = false }
        )
    }
}

@Composable
private fun PrayerTopAppBar(
    schedule: com.example.data.prayer.DailyPrayerSchedule,
    selectedCity: CityLocation,
    onCityClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onToggleSkyExplorer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GlassIconButton(
            icon = Icons.Default.ArrowForward,
            onClick = onNavigateBack,
            contentDescription = "رجوع",
            size = 42.dp
        )

        // City Selector Chip
        GlassChip(
            text = "${selectedCity.cityName} (${selectedCity.country})",
            isSelected = true,
            icon = Icons.Default.LocationOn,
            onClick = onCityClick,
            accentColor = Color(0xFF00E5FF)
        )

        // Sky Explorer Toggle
        GlassIconButton(
            icon = Icons.Default.WbSunny,
            onClick = onToggleSkyExplorer,
            contentDescription = "محاكي السماء",
            size = 42.dp,
            accentGlow = Color(0xFFFFB74D)
        )
    }
}

@Composable
private fun SkyExplorerGlassCard(
    simulatedHour: Float?,
    onHourChange: (Float) -> Unit,
    onResetRealTime: () -> Unit
) {
    val currentVal = simulatedHour ?: 12f
    GlassCard(
        backgroundBrush = Brush.verticalGradient(
            listOf(Color(0x44000000), Color(0x33000000))
        ),
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFFFFB74D))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "محاكي السماء التفاعلي",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            if (simulatedHour != null) {
                TextButton(onClick = onResetRealTime) {
                    Text(
                        text = "الوقت الفعلي ⏱️",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Text(
                    text = "مفعل: الوقت الحقيقي",
                    color = Color(0xAAFFFFFF),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val hourInt = currentVal.toInt()
        val minInt = ((currentVal - hourInt) * 60).toInt()
        Text(
            text = "الساعة المختارة للمعاينة: %02d:%02d".format(hourInt, minInt),
            color = Color.White,
            fontSize = 13.sp
        )

        Slider(
            value = currentVal,
            onValueChange = onHourChange,
            valueRange = 0f..23.9f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB74D),
                activeTrackColor = Color(0xFFFFB74D),
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
private fun NextPrayerHeroCard(
    schedule: com.example.data.prayer.DailyPrayerSchedule,
    onMethodClick: () -> Unit
) {
    GlassCard(
        elevation = 12.dp,
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF00E5FF)),
        backgroundBrush = Brush.verticalGradient(
            listOf(
                Color(0x40FFFFFF),
                Color(0x18FFFFFF),
                Color(0x10000000)
            )
        )
    ) {
        // Date & Phase Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = schedule.hijriFormatted,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Text(
                    text = schedule.dateFormatted,
                    color = Color(0xCCFFFFFF),
                    fontSize = 12.sp
                )
            }

            // Sky phase badge
            Box(
                modifier = Modifier
                    .clip(GlassDefaults.PillShape)
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x44FFFFFF), GlassDefaults.PillShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = schedule.skyPhase.phaseNameArabic,
                    color = Color(0xFFFFE082),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next prayer highlight
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "الصلاة القادمة",
                color = Color(0xCCFFFFFF),
                fontSize = 13.sp
            )

            Text(
                text = "صلاة ${schedule.nextPrayer.nameArabic}",
                fontWeight = FontWeight.Black,
                color = Color(0xFF00E5FF),
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = schedule.nextPrayer.timeFormatted,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x3300E5FF),
                                Color(0x221E1B4B),
                                Color(0x3300E5FF)
                            )
                        )
                    )
                    .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "متبقي ${schedule.remainingFormatted}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar between prayers
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { schedule.currentIntervalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFF00E5FF),
                trackColor = Color(0x33FFFFFF)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = schedule.currentPrayer?.let { "السابقة: ${it.nameArabic}" } ?: "",
                    color = Color(0xAAFFFFFF),
                    fontSize = 11.sp
                )
                Text(
                    text = "القادمة: ${schedule.nextPrayer.nameArabic}",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calculation Method footer
        TextButton(
            onClick = onMethodClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xCCFFFFFF),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "طريقة الحساب والأوقات",
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PrayerScheduleGrid(schedule: com.example.data.prayer.DailyPrayerSchedule) {
    val prayers = listOf(
        schedule.fajr to Icons.Default.NightsStay,
        schedule.sunrise to Icons.Default.WbTwilight,
        schedule.dhuhr to Icons.Default.WbSunny,
        schedule.asr to Icons.Default.LightMode,
        schedule.maghrib to Icons.Default.WbTwilight,
        schedule.isha to Icons.Default.Bedtime
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        prayers.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPair.forEach { (prayer, icon) ->
                    Box(modifier = Modifier.weight(1f)) {
                        PrayerSingleCard(prayer = prayer, icon = icon)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerSingleCard(
    prayer: PrayerTime,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val accentColor = when {
        prayer.isNext -> Color(0xFF00E5FF)
        prayer.isCurrent -> Color(0xFFFFD54F)
        prayer.isPassed -> Color(0x88FFFFFF)
        else -> Color.White
    }

    val borderBrush = if (prayer.isNext) {
        GlassDefaults.accentBorderGradient(Color(0xFF00E5FF))
    } else {
        GlassDefaults.GlassBorderGradient
    }

    val bgBrush = if (prayer.isNext) {
        Brush.verticalGradient(listOf(Color(0x3800E5FF), Color(0x1500E5FF)))
    } else {
        GlassDefaults.GlassGradient
    }

    GlassCard(
        shape = GlassDefaults.SmallCardShape,
        borderBrush = borderBrush,
        backgroundBrush = bgBrush,
        contentPadding = PaddingValues(12.dp),
        testTag = "prayer_card_${prayer.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = prayer.nameArabic,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = prayer.nameEnglish,
                        color = Color(0x99FFFFFF),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prayer.timeFormatted,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 14.sp
                )
                if (prayer.isNext) {
                    Text(
                        text = "التالية",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassCard(
    schedule: com.example.data.prayer.DailyPrayerSchedule,
    cityName: String
) {
    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF10B981))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اتجاه القبلة الشريفة",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "زاوية الاتجاه: ${"%.1f".format(schedule.qiblaAngle)}° من الشمال",
                    color = Color(0xEEFFFFFF),
                    fontSize = 13.sp
                )
                Text(
                    text = "المسافة إلى الكعبة: ${"%,.0f".format(schedule.qiblaDistanceKm)} كم",
                    color = Color(0xAAFFFFFF),
                    fontSize = 12.sp
                )
                Text(
                    text = "الموقع الحالي: $cityName",
                    color = Color(0x88FFFFFF),
                    fontSize = 11.sp
                )
            }

            // Compass Visual Indicator
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x3310B981))
                    .border(1.5.dp, Color(0xFF10B981), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "اتجاه القبلة",
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .size(36.dp)
                        .rotate(schedule.qiblaAngle.toFloat())
                )
            }
        }
    }
}

@Composable
private fun CitySelectionDialog(
    currentCity: CityLocation,
    onSelectCity: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("اختر مدينتك لمواقيت الصلاة", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(PrayerTimeCalculator.PRESET_CITIES) { city ->
                    val isSelected = city.cityName == currentCity.cityName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onSelectCity(city) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = city.cityName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = city.country,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun MethodSelectionDialog(
    currentMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("طرق حساب مواقيت الصلاة", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CalculationMethod.values().forEach { method ->
                    val isSelected = method == currentMethod
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onSelectMethod(method) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = method.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تم")
            }
        }
    )
}

@Composable
private fun AdhanAndNotificationSettingsCard() {
    val context = LocalContext.current
    var isNotificationsEnabled by remember { mutableStateOf(AudioStorageManager.isPrayerNotificationsEnabled(context)) }
    var isPrePrayerAlert by remember { mutableStateOf(AudioStorageManager.isPrePrayerAlertEnabled(context)) }
    var selectedAdhanId by remember { mutableStateOf(AudioStorageManager.getSelectedAdhanId(context)) }
    var selectedDuaId by remember { mutableStateOf(AudioStorageManager.getSelectedDuaId(context)) }
    var audioList by remember { mutableStateOf(AudioStorageManager.getAudioList(context)) }
    val currentlyPlayingId by AudioStorageManager.currentlyPlayingId.collectAsStateWithLifecycle()

    var showUploadDialog by remember { mutableStateOf(false) }
    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var uploadTargetType by remember { mutableStateOf(AudioType.ADHAN) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                pendingUploadUri = uri
                showUploadDialog = true
            }
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "تم تفعيل صلاحية الإشعارات بنجاح ✅", Toast.LENGTH_SHORT).show()
                PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
            } else {
                Toast.makeText(context, "لم يتم منح صلاحية الإشعارات", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val hasSystemPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PrayerNotificationHelper.areNotificationsEnabled(context)
    } else {
        true
    }

    GlassCard(
        borderBrush = GlassDefaults.accentBorderGradient(Color(0xFF00E5FF)),
        backgroundBrush = Brush.verticalGradient(
            listOf(
                Color(0x3500E5FF),
                Color(0x201E1B4B),
                Color(0x250F172A)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الأذان والتنبيهات الصوتية والأدعية",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "تخصيص نغمة الأذان ورفع ملفات MP3 للأدعية",
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Notification Permission Warning if not granted
            if (!hasSystemPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الإشعارات مغلقة في النظام - انقر لتفعيلها لتصلك مواقيت الأذان",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("تفعيل 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Main Switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنبيهات دخول وقت الصلاة",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "إشعار وصوت عند حان وقت الأذان",
                        color = Color(0xAAFFFFFF),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isNotificationsEnabled,
                    onCheckedChange = {
                        isNotificationsEnabled = it
                        AudioStorageManager.setPrayerNotificationsEnabled(context, it)
                        if (it) {
                            PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00E5FF)
                    )
                )
            }

            // Test Notification & Audio Button
            Button(
                onClick = {
                    PrayerNotificationHelper.sendTestNotification(
                        context,
                        title = "تجربة الأذان والإشعار 🕌",
                        body = "الله أكبر، الله أكبر - تم تفعيل نظام التنبيهات والأذان بنجاح"
                    )
                    Toast.makeText(context, "تم إرسال إشعار تجريبي وتشغيل الصوت 🔔", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تجربة إشعار الأذان والصوت الآن ⚡",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

            // Section 1: Adhan Sound Selection & Custom Upload
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "صوت الأذان المختار 🔊",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp
                )

                // Upload MP3 Button
                Button(
                    onClick = {
                        uploadTargetType = AudioType.ADHAN
                        audioPickerLauncher.launch("audio/*")
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F).copy(alpha = 0.25f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رفع ملف صوت أذان 📁",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Audio Items List for Adhan
            val adhanAudios = audioList.filter { it.type == AudioType.ADHAN || it.type == AudioType.REMINDER }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                adhanAudios.forEach { audio ->
                    val isSelected = audio.id == selectedAdhanId
                    val isPlaying = audio.id == currentlyPlayingId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else Color(0x15FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedAdhanId = audio.id
                                AudioStorageManager.setSelectedAdhanId(context, audio.id)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedAdhanId = audio.id
                                    AudioStorageManager.setSelectedAdhanId(context, audio.id)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = audio.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = audio.description,
                                    color = Color(0xAAFFFFFF),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Play/Stop Preview Button
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        AudioStorageManager.stopAudio()
                                    } else {
                                        AudioStorageManager.playAudio(context, audio)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                                    contentDescription = "استماع",
                                    tint = if (isPlaying) Color(0xFFFF5252) else Color(0xFF00E5FF),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Delete button if custom uploaded
                            if (!audio.isBuiltIn) {
                                IconButton(
                                    onClick = {
                                        AudioStorageManager.deleteCustomAudio(context, audio.id)
                                        audioList = AudioStorageManager.getAudioList(context)
                                        selectedAdhanId = AudioStorageManager.getSelectedAdhanId(context)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

            // Section 2: Dua & Azkar Audio Selector & Upload
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "صوت الأدعية والتسابيح 📿",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontSize = 14.sp
                )

                // Upload MP3 for Dua Button
                Button(
                    onClick = {
                        uploadTargetType = AudioType.DUA
                        audioPickerLauncher.launch("audio/*")
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.25f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رفع ملف صوت دعاء 📁",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Audio Items List for Dua
            val duaAudios = audioList.filter { it.type == AudioType.DUA }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                duaAudios.forEach { audio ->
                    val isSelected = audio.id == selectedDuaId
                    val isPlaying = audio.id == currentlyPlayingId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x3310B981) else Color(0x15FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF10B981) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedDuaId = audio.id
                                AudioStorageManager.setSelectedDuaId(context, audio.id)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedDuaId = audio.id
                                    AudioStorageManager.setSelectedDuaId(context, audio.id)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = audio.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = audio.description,
                                    color = Color(0xAAFFFFFF),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        AudioStorageManager.stopAudio()
                                    } else {
                                        AudioStorageManager.playAudio(context, audio)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                                    contentDescription = "استماع",
                                    tint = if (isPlaying) Color(0xFFFF5252) else Color(0xFF10B981),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            if (!audio.isBuiltIn) {
                                IconButton(
                                    onClick = {
                                        AudioStorageManager.deleteCustomAudio(context, audio.id)
                                        audioList = AudioStorageManager.getAudioList(context)
                                        selectedDuaId = AudioStorageManager.getSelectedDuaId(context)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload Confirmation Dialog
    if (showUploadDialog && pendingUploadUri != null) {
        var customAudioTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showUploadDialog = false
                pendingUploadUri = null
            },
            title = {
                Text(
                    text = if (uploadTargetType == AudioType.ADHAN) "حفظ ملف صوت الأذان 🕌" else "حفظ ملف صوت الدعاء 📿",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تم اختيار الملف الصوتي بنجاح. يرجى كتابة اسم تعريفي له:")
                    OutlinedTextField(
                        value = customAudioTitle,
                        onValueChange = { customAudioTitle = it },
                        label = { Text("عنوان الملف الصوتي") },
                        placeholder = { Text(if (uploadTargetType == AudioType.ADHAN) "مثال: أذان الشيخ عبد الباسط" else "مثال: دعاء كميل / دعاء الفرج") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingUploadUri
                        if (uri != null) {
                            val saved = AudioStorageManager.saveCustomAudio(
                                context = context,
                                uri = uri,
                                customTitle = customAudioTitle,
                                type = uploadTargetType
                            )
                            if (saved != null) {
                                audioList = AudioStorageManager.getAudioList(context)
                                if (uploadTargetType == AudioType.ADHAN) {
                                    selectedAdhanId = saved.id
                                } else {
                                    selectedDuaId = saved.id
                                }
                                Toast.makeText(context, "تم حفظ الملف واختياره بنجاح ✨", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "فشل في حفظ الملف الصوتي", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showUploadDialog = false
                        pendingUploadUri = null
                    }
                ) {
                    Text("حفظ واختيار")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUploadDialog = false
                        pendingUploadUri = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}
