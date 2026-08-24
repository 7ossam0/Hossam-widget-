package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var azimuth by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }

    val qiblaBearing = remember(selectedCity) {
        QiblaCalculator.calculateQiblaBearing(selectedCity.latitude, selectedCity.longitude).toFloat()
    }
    val distanceKm = remember(selectedCity) {
        QiblaCalculator.calculateDistanceToMakkahKm(selectedCity.latitude, selectedCity.longitude).toInt()
    }

    // Hardware Compass Sensor Listener
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (deg < 0) deg += 360f
                    azimuth = deg
                    pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    azimuth = event.values[0]
                    pitch = event.values[1]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (magneticSensor != null) {
            sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "AzimuthAnimation"
    )

    // Calculate angle difference to Qibla
    val angleDiff = (qiblaBearing - animatedAzimuth + 540) % 360 - 180
    val isFacingQibla = abs(angleDiff) <= 5.0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("القبلة ومكة المكرمة", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            text = "${selectedCity.cityName} • زاوية القبلة: ${qiblaBearing.toInt()}°",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { isArMode = !isArMode },
                        modifier = Modifier.testTag("toggle_ar_mode_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            if (isArMode) Icons.Default.Explore else Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArMode) "البوصلة" else "الواقع المعزز AR", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFacingQibla)
                        Color(0xFF065F46).copy(alpha = 0.9f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isFacingQibla) Icons.Filled.CheckCircle else Icons.Default.Explore,
                                contentDescription = null,
                                tint = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isFacingQibla) "أنت تواجه القبلة تماماً! ✨" else "قم بتدوير الهاتف نحو القبلة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "المسافة إلى الكعبة: $distanceKm كم",
                                fontSize = 12.sp,
                                color = if (isFacingQibla) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${qiblaBearing.toInt()}°",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (isFacingQibla) Color(0xFF34D399) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Visual: AR View or Compass Dial
            if (isArMode) {
                ArCameraCompassView(
                    angleDiff = angleDiff,
                    isFacingQibla = isFacingQibla,
                    distanceKm = distanceKm,
                    pitch = pitch
                )
            } else {
                CelestialCompassDial(
                    animatedAzimuth = animatedAzimuth,
                    qiblaBearing = qiblaBearing,
                    isFacingQibla = isFacingQibla
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // City info & calibration tip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "للحصول على أعلى دقة، حرّك هاتفك بشكل الرقم (8) في الهواء لمعايرة مستشعر البوصلة والمجال المغناطيسي.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CelestialCompassDial(
    animatedAzimuth: Float,
    qiblaBearing: Float,
    isFacingQibla: Boolean
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = if (isFacingQibla) 3.dp else 1.dp,
                color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Compass Dial
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedAzimuth)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Draw North, South, East, West ticks
            for (i in 0 until 360 step 15) {
                val angleRad = Math.toRadians(i.toDouble())
                val isMajor = i % 90 == 0
                val tickLength = if (isMajor) 18.dp.toPx() else 8.dp.toPx()

                val startX = (center.x + (radius - tickLength) * Math.sin(angleRad)).toFloat()
                val startY = (center.y - (radius - tickLength) * Math.cos(angleRad)).toFloat()
                val endX = (center.x + radius * Math.sin(angleRad)).toFloat()
                val endY = (center.y - radius * Math.cos(angleRad)).toFloat()

                drawLine(
                    color = if (i == 0) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                )
            }
        }

        // Qibla Indicator Arrow pointing to Kaaba Bearing relative to north
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(qiblaBearing - animatedAzimuth),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFacingQibla) Color(0xFF10B981) else Color(0xFFD97706)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕋", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الكعبة",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFacingQibla) Color(0xFF10B981) else Color(0xFFD97706)
                )
            }
        }

        // Center hub
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${animatedAzimuth.toInt()}°",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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
            .height(300.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        // Simulated Camera Feed Overlay & Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            // Horizon line
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(0f, center.y + pitch * 2),
                end = Offset(size.width, center.y + pitch * 2),
                strokeWidth = 1.5.dp.toPx()
            )

            // Crosshair Target Box
            drawCircle(
                color = if (isFacingQibla) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                radius = 48.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Animated Kaaba Target on Screen
        val xOffset = (-angleDiff * 8f).coerceIn(-140f, 140f)

        Box(
            modifier = Modifier
                .offset(x = xOffset.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFacingQibla) Color(0xFF059669) else Color(0xFF1E293B).copy(alpha = 0.9f))
                .border(2.dp, if (isFacingQibla) Color(0xFF34D399) else Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🕋", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFacingQibla) "القبلة في المرمى! 🎯" else "الكعبة المشرفة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "$distanceKm كم",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
