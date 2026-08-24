package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.view.Surface
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.prayer.CityLocation
import com.example.data.prayer.PrayerTimeCalculator
import com.example.data.qibla.QiblaCalculator
import com.example.viewmodel.MainViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaARScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()

    var isArMode by remember { mutableStateOf(false) }
    var rawAzimuth by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var showCitySelector by remember { mutableStateOf(false) }

    // Calculate Magnetic Declination via GeomagneticField using selected/GPS coordinates
    val geoField = remember(selectedCity) {
        try {
            GeomagneticField(
                selectedCity.latitude.toFloat(),
                selectedCity.longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
    val magneticDeclination = remember(geoField) { geoField?.declination ?: 0f }

    // Accurate Great Circle Bearing and Geodesic Distance to Kaaba
    val qiblaBearing = remember(selectedCity) {
        QiblaCalculator.calculateQiblaBearing(selectedCity.latitude, selectedCity.longitude).toFloat()
    }
    val distanceKm = remember(selectedCity) {
        QiblaCalculator.calculateDistanceToMakkahKm(selectedCity.latitude, selectedCity.longitude).toInt()
    }
    val cardinalDirection = remember(qiblaBearing) {
        QiblaCalculator.getCompassDirectionArabic(qiblaBearing.toDouble())
    }

    // Hardware Compass Sensor Listener with tilt compensation, GeomagneticField declination and low-pass smoothing
    DisposableEffect(context, magneticDeclination) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        // Low-pass filter smoothing coefficient
        val alpha = 0.2f
        var smoothedAzimuth = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rotationMatrix = FloatArray(9)
                val adjustedMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    // Remap coordinate system for portrait screen orientation
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        adjustedMatrix
                    )
                    SensorManager.getOrientation(adjustedMatrix, orientation)

                    val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val magneticAzimuth = (deg + 360f) % 360f
                    // Correct for true geographic north using magnetic declination
                    val trueNorthAzimuth = (magneticAzimuth + magneticDeclination + 360f) % 360f

                    // Smooth angular transition avoiding 0/360 wrap glitch
                    val diff = (trueNorthAzimuth - smoothedAzimuth + 540f) % 360f - 180f
                    smoothedAzimuth = (smoothedAzimuth + alpha * diff + 360f) % 360f

                    rawAzimuth = smoothedAzimuth
                    pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    hasGeomagnetic = true
                }

                if (rotationSensor == null && hasGravity && hasGeomagnetic) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            adjustedMatrix
                        )
                        SensorManager.getOrientation(adjustedMatrix, orientation)
                        val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        val magneticAzimuth = (deg + 360f) % 360f
                        val trueNorthAzimuth = (magneticAzimuth + magneticDeclination + 360f) % 360f

                        val diff = (trueNorthAzimuth - smoothedAzimuth + 540f) % 360f - 180f
                        smoothedAzimuth = (smoothedAzimuth + alpha * diff + 360f) % 360f

                        rawAzimuth = smoothedAzimuth
                        pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Dynamic rotation animation with continuous angle tracking
    val animatedAzimuth by animateFloatAsState(
        targetValue = rawAzimuth,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "QiblaAzimuthAnimation"
    )

    // Angle offset difference between device heading and Kaaba bearing (-180..180)
    val angleDiff = (qiblaBearing - animatedAzimuth + 540f) % 360f - 180f
    val isFacingQibla = abs(angleDiff) <= 4.0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("بوصلة القبلة المشرفة", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text(
                            text = "${selectedCity.cityName} • زاوية القبلة: ${qiblaBearing.toInt()}° ($cardinalDirection)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("qibla_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
                    }
                },
                actions = {
                    // Quick City Selector Button
                    FilledTonalButton(
                        onClick = { showCitySelector = true },
                        modifier = Modifier.testTag("change_city_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedCity.cityName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Toggle AR vs Compass View
                    IconButton(
                        onClick = { isArMode = !isArMode },
                        modifier = Modifier.testTag("toggle_ar_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (isArMode) Icons.Default.Explore else Icons.Default.CameraAlt,
                            contentDescription = if (isArMode) "وضع البوصلة" else "وضع الكاميرا والواقع المعزز",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status & Direction Guidance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isFacingQibla) 12.dp else 2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFacingQibla)
                        Color(0xFF047857)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                border = if (isFacingQibla) BorderStroke(2.dp, Color(0xFF34D399)) else null
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isFacingQibla) "🕋" else "🧭", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isFacingQibla)
                                        "أنت تواجه القبلة المشرفة بدقة! ✨"
                                    else if (angleDiff > 0)
                                        "أدر هاتفك ${abs(angleDiff).toInt()}° إلى اليمين ➡️"
                                    else
                                        "أدر هاتفك ${abs(angleDiff).toInt()}° إلى اليسار ⬅️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "المسافة إلى الكعبة: $distanceKm كم • ${selectedCity.cityName}",
                                    fontSize = 12.sp,
                                    color = if (isFacingQibla) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Qibla Angle Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFacingQibla) Color(0xFF064E3B) else MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${qiblaBearing.toInt()}°",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isFacingQibla) Color(0xFF6EE7B7) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "القبلة",
                                    fontSize = 10.sp,
                                    color = if (isFacingQibla) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Compass or AR Visual View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isArMode) {
                    ArCameraCompassView(
                        angleDiff = angleDiff,
                        isFacingQibla = isFacingQibla,
                        distanceKm = distanceKm,
                        pitch = pitch
                    )
                } else {
                    AccurateCelestialCompass(
                        animatedAzimuth = animatedAzimuth,
                        qiblaBearing = qiblaBearing,
                        isFacingQibla = isFacingQibla,
                        cardinalDirection = cardinalDirection
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calibration and Accuracy Footer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.TipsAndUpdates,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لضمان أعلى دقة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "ضع الهاتف بشكل أفقي مسطح وبعيداً عن الأجسام المغناطيسية أو المعادن، وحرّكه بشكل الرقم (8) في الهواء للمعايرة.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // City Selector BottomSheet Modal
    if (showCitySelector) {
        CitySelectionModal(
            currentCity = selectedCity,
            onCitySelected = { city ->
                viewModel.selectCity(city)
                showCitySelector = false
            },
            onDismiss = { showCitySelector = false }
        )
    }
}

/**
 * بوصلة فلكية دقيقة ومحكمة تُظهر الشمال الجغرافي واتجاه الكعبة المشرفة بدقة متناهية
 */
@Composable
private fun AccurateCelestialCompass(
    animatedAzimuth: Float,
    qiblaBearing: Float,
    isFacingQibla: Boolean,
    cardinalDirection: String
) {
    Box(
        modifier = Modifier
            .size(310.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        if (isFacingQibla) Color(0xFF064E3B).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = if (isFacingQibla) 3.5.dp else 1.5.dp,
                color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Rotating Compass Dial with Degree marks and North indicator
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedAzimuth)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Draw 360 degree ticks
            for (i in 0 until 360 step 5) {
                val angleRad = Math.toRadians(i.toDouble())
                val isMajor = i % 30 == 0
                val isCardinal = i % 90 == 0
                val tickLength = if (isCardinal) 20.dp.toPx() else if (isMajor) 12.dp.toPx() else 6.dp.toPx()

                val startX = (center.x + (radius - tickLength) * Math.sin(angleRad)).toFloat()
                val startY = (center.y - (radius - tickLength) * Math.cos(angleRad)).toFloat()
                val endX = (center.x + radius * Math.sin(angleRad)).toFloat()
                val endY = (center.y - radius * Math.cos(angleRad)).toFloat()

                val tickColor = when (i) {
                    0 -> Color(0xFFEF4444) // North in Red
                    90 -> Color(0xFF0284C7) // East
                    180 -> Color(0xFF64748B) // South
                    270 -> Color(0xFFD97706) // West
                    else -> Color.Gray.copy(alpha = 0.4f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isCardinal) 3.5.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }
        }

        // 2. Cardinal Labels overlay rotating with dial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedAzimuth)
        ) {
            // North (شمال)
            Text(
                text = "ش (N)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
            )
            // South (جنوب)
            Text(
                text = "ج (S)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
            )
            // East (شرق)
            Text(
                text = "ق (E)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF0284C7),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
            )
            // West (غرب)
            Text(
                text = "غ (W)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFD97706),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 22.dp)
            )
        }

        // 3. Kaaba Pointer Needle (Fixed relative to compass heading)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(qiblaBearing - animatedAzimuth),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Golden / Emerald Kaaba Emblem
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFacingQibla) Color(0xFF10B981) else Color(0xFFD97706),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.5.dp, if (isFacingQibla) Color(0xFF6EE7B7) else Color(0xFFFDE68A)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🕋", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "الكعبة المشرفة",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFacingQibla) Color(0xFF10B981) else Color(0xFFB45309)
                )
            }
        }

        // 4. Center Heading Hub with Live Degrees and Direction
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            border = BorderStroke(2.dp, if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier.size(80.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedAzimuth.toInt()}°",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = QiblaCalculator.getCompassDirectionArabic(animatedAzimuth.toDouble()).split(" ")[0],
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * وضع الواقع المعزز والكاميرا مع مؤشر الهدف الكروي
 */
@Composable
private fun ArCameraCompassView(
    angleDiff: Float,
    isFacingQibla: Boolean,
    distanceKm: Int,
    pitch: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, if (isFacingQibla) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            // Horizon and Tilt line
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(0f, center.y + pitch * 2),
                end = Offset(size.width, center.y + pitch * 2),
                strokeWidth = 2.dp.toPx()
            )

            // Crosshair Target Grid
            drawCircle(
                color = if (isFacingQibla) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                radius = 56.dp.toPx(),
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // Horizontal Tracking of Kaaba Target Box
        val xOffset = (-angleDiff * 9f).coerceIn(-130f, 130f)

        Box(
            modifier = Modifier
                .offset(x = xOffset.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isFacingQibla) Color(0xFF059669) else Color(0xFF1E293B).copy(alpha = 0.95f))
                .border(2.dp, if (isFacingQibla) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🕋", fontSize = 34.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFacingQibla) "الهدف في المرمى! 🎯" else "الكعبة المشرفة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "$distanceKm كم",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * نافذة اختيار المدينة السريعة لتحديث زاوية القبلة الفلكية فوراً مع إمكانية التحديد بالـ GPS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelectionModal(
    currentCity: CityLocation,
    onCitySelected: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }
    val cities = PrayerTimeCalculator.PRESET_CITIES

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val loc = detectCurrentLocation(context)
            if (loc != null) {
                onCitySelected(
                    CityLocation(
                        cityName = "موقعي الحالي (GPS)",
                        country = "إحداثيات حية",
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        timeZoneOffsetHours = (java.util.TimeZone.getDefault().rawOffset / 3600000.0)
                    )
                )
                onDismiss()
            } else {
                locationError = "يرجى تشغيل خدمة الموقع (GPS) في هاتفك لتحديث الإحداثيات."
            }
        } else {
            locationError = "يتطلب تحديد الموقع منح إذن الوصول إلى GPS."
        }
    }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) cities
        else cities.filter {
            it.cityName.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تحديد موقع القبلة 📍",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "يتم حساب زاوية القبلة والانحراف المغناطيسي بدقة",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // زر تحديد الموقع التلقائي عبر الـ GPS
            FilledTonalButton(
                onClick = {
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        val loc = detectCurrentLocation(context)
                        if (loc != null) {
                            onCitySelected(
                                CityLocation(
                                    cityName = "موقعي الحالي (GPS)",
                                    country = "إحداثيات حية",
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    timeZoneOffsetHours = (java.util.TimeZone.getDefault().rawOffset / 3600000.0)
                                )
                            )
                            onDismiss()
                        } else {
                            locationError = "يرجى التأكد من تشغيل خدمة الموقع (GPS)."
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تحديد موقعي التلقائي عبر الـ GPS", fontWeight = FontWeight.Bold)
            }

            if (locationError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = locationError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن مدينة أو دولة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCities, key = { "${it.cityName}_${it.country}" }) { city ->
                    val isSelected = city.cityName == currentCity.cityName
                    val cityQiblaBearing = QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude).toInt()
                    val cityDist = QiblaCalculator.calculateDistanceToMakkahKm(city.latitude, city.longitude).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCitySelected(city) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.cityName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${city.country} • خط العرض: ${city.latitude}°, خط الطول: ${city.longitude}°",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$cityQiblaBearing°",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$cityDist كم",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun detectCurrentLocation(context: Context): Location? {
    return try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return null

        val gpsLoc = if (hasFine && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else null

        val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } else null

        val passiveLoc = try {
            locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (e: Exception) {
            null
        }

        gpsLoc ?: netLoc ?: passiveLoc
    } catch (e: Exception) {
        null
    }
}
