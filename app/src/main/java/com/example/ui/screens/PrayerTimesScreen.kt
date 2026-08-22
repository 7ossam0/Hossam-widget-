package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.CityLocation
import com.example.data.prayer.PrayerTime
import com.example.data.prayer.PrayerTimeCalculator
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
