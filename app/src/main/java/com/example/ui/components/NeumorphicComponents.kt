package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCustomFontFamily
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deep Luxury Neumorphic Card with 3D Depth, Inner Glow & Ambient Rim Lighting
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    backgroundColor: Color = Color(0xFF161B22),
    borderColor: Color = Color(0xFF30363D),
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = glowColor ?: Color(0xFF000000),
                spotColor = glowColor ?: Color(0xFF00E5FF).copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.95f),
                        Color(0xFF0D1117)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor?.copy(alpha = 0.6f) ?: Color(0xFF388BFD).copy(alpha = 0.3f),
                        borderColor.copy(alpha = 0.3f),
                        Color(0x10FFFFFF)
                    )
                ),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * Smart-Home Inspired High-Precision Circular Arc Dial / Slider
 */
@Composable
fun CircularDialSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 10f..40f,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    trackColor: Color = Color(0xFF21262D),
    progressGradient: List<Color> = listOf(Color(0xFF00E5FF), Color(0xFF388BFD), Color(0xFFA371F7)),
    unit: String = "sp",
    title: String = "مقياس الخط",
    subtitle: String = "اسحب للتحكم المباشر",
    valueFormatter: (Float) -> String = { it.toInt().toString() }
) {
    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = normalizedValue, label = "dialProgress")

    val startAngle = 135f
    val sweepTotalAngle = 270f

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val touchX = change.position.x - (size.toPx() / 2)
                    val touchY = change.position.y - (size.toPx() / 2)
                    var angle = Math.toDegrees(atan2(touchY.toDouble(), touchX.toDouble())).toFloat()
                    if (angle < 0) angle += 360f

                    val relativeAngle = (angle - startAngle + 360f) % 360f
                    if (relativeAngle <= sweepTotalAngle) {
                        val progress = (relativeAngle / sweepTotalAngle).coerceIn(0f, 1f)
                        val newValue = valueRange.start + progress * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner 3D Neumorphic Embossed Disc
        Surface(
            shape = CircleShape,
            color = Color(0xFF161B22),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF21262D)),
            modifier = Modifier
                .size(size - 44.dp)
                .shadow(12.dp, CircleShape, spotColor = Color(0xFF00E5FF).copy(alpha = 0.25f))
        ) {}

        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background Outer Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotalAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Glowing Sweep Gradient Arc
            drawArc(
                brush = Brush.sweepGradient(progressGradient),
                startAngle = startAngle,
                sweepAngle = (animatedProgress * sweepTotalAngle).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glowing Thumb Handle
            val thumbAngleRad = Math.toRadians((startAngle + animatedProgress * sweepTotalAngle).toDouble())
            val radius = (this.size.width - strokeWidth) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val thumbX = center.x + radius * cos(thumbAngleRad).toFloat()
            val thumbY = center.y + radius * sin(thumbAngleRad).toFloat()

            // Outer thumb halo glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF), Color.Transparent)
                ),
                radius = 16.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )

            // Inner solid thumb knob
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        // Center Content Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF8B949E),
                fontFamily = AppCustomFontFamily,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = valueFormatter(value),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC)
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 14.sp,
                        color = Color(0xFF00E5FF),
                        fontFamily = AppCustomFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF58A6FF),
                fontFamily = AppCustomFontFamily
            )
        }
    }
}

/**
 * Modern Neumorphic Toggle Capsule / Pill Button
 */
@Composable
fun NeumorphicPillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    activeColor: Color = Color(0xFF00E5FF)
) {
    val containerBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                activeColor.copy(alpha = 0.28f),
                Color(0xFF388BFD).copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF161B22),
                Color(0xFF1C2128)
            )
        )
    }

    val borderColor = if (isSelected) activeColor else Color(0xFF30363D)
    val contentColor = if (isSelected) activeColor else Color(0xFF8B949E)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isSelected) activeColor else Color.Transparent
            )
            .clip(RoundedCornerShape(18.dp))
            .background(containerBrush)
            .border(1.2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontFamily = AppCustomFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Neumorphic Tile with Icon & Power Indicator (like Smart Home controls)
 */
@Composable
fun NeumorphicControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF)
) {
    NeumorphicCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        backgroundColor = if (isActive) Color(0xFF1C2128) else Color(0xFF161B22),
        glowColor = if (isActive) accentColor.copy(alpha = 0.35f) else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActive) accentColor.copy(alpha = 0.2f) else Color(0xFF21262D),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) accentColor else Color(0xFF30363D)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isActive) accentColor else Color(0xFF8B949E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isActive) accentColor else Color(0xFF30363D),
                    modifier = Modifier.size(8.dp)
                ) {}
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFF8B949E)
                )
            }
        }
    }
}
