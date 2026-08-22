package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.prayer.CelestialSkyPhase
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

data class SkyAtmosphereColors(
    val topGradient: Color,
    val middleGradient: Color,
    val bottomGradient: Color,
    val sunColor: Color,
    val sunGlowColor: Color,
    val moonColor: Color,
    val moonGlowColor: Color,
    val starsAlpha: Float,
    val cloudsAlpha: Float,
    val horizonGlowAlpha: Float
)

object CelestialSkyThemes {
    fun getColorsForPhase(phase: CelestialSkyPhase): SkyAtmosphereColors {
        return when (phase) {
            CelestialSkyPhase.DEEP_NIGHT -> SkyAtmosphereColors(
                topGradient = Color(0xFF030712),
                middleGradient = Color(0xFF0B1329),
                bottomGradient = Color(0xFF131D38),
                sunColor = Color.Transparent,
                sunGlowColor = Color.Transparent,
                moonColor = Color(0xFFF1F5F9),
                moonGlowColor = Color(0x6694A3B8),
                starsAlpha = 0.95f,
                cloudsAlpha = 0.15f,
                horizonGlowAlpha = 0.10f
            )
            CelestialSkyPhase.FAJR_DAWN -> SkyAtmosphereColors(
                topGradient = Color(0xFF1E1B4B),
                middleGradient = Color(0xFF3B1E54),
                bottomGradient = Color(0xFF7E22CE),
                sunColor = Color(0xFFFFB74D),
                sunGlowColor = Color(0x66FF7043),
                moonColor = Color(0xFFF8FAFC),
                moonGlowColor = Color(0x33CBD5E1),
                starsAlpha = 0.40f,
                cloudsAlpha = 0.30f,
                horizonGlowAlpha = 0.65f
            )
            CelestialSkyPhase.SUNRISE_GLOW -> SkyAtmosphereColors(
                topGradient = Color(0xFF1E3A8A),
                middleGradient = Color(0xFFB45309),
                bottomGradient = Color(0xFFF97316),
                sunColor = Color(0xFFFFD54F),
                sunGlowColor = Color(0xAAFFA726),
                moonColor = Color.Transparent,
                moonGlowColor = Color.Transparent,
                starsAlpha = 0.0f,
                cloudsAlpha = 0.40f,
                horizonGlowAlpha = 0.90f
            )
            CelestialSkyPhase.MORNING_DUHA -> SkyAtmosphereColors(
                topGradient = Color(0xFF0284C7),
                middleGradient = Color(0xFF38BDF8),
                bottomGradient = Color(0xFFBAE6FD),
                sunColor = Color(0xFFFFF9C4),
                sunGlowColor = Color(0x99FFE082),
                moonColor = Color.Transparent,
                moonGlowColor = Color.Transparent,
                starsAlpha = 0.0f,
                cloudsAlpha = 0.50f,
                horizonGlowAlpha = 0.40f
            )
            CelestialSkyPhase.NOON_DHUHR -> SkyAtmosphereColors(
                topGradient = Color(0xFF0369A1),
                middleGradient = Color(0xFF0EA5E9),
                bottomGradient = Color(0xFF7DD3FC),
                sunColor = Color(0xFFFFFFFF),
                sunGlowColor = Color(0xB3FFF59D),
                moonColor = Color.Transparent,
                moonGlowColor = Color.Transparent,
                starsAlpha = 0.0f,
                cloudsAlpha = 0.45f,
                horizonGlowAlpha = 0.35f
            )
            CelestialSkyPhase.AFTERNOON_ASR -> SkyAtmosphereColors(
                topGradient = Color(0xFF0C4A6E),
                middleGradient = Color(0xFF0284C7),
                bottomGradient = Color(0xFFFDBA74),
                sunColor = Color(0xFFFFE082),
                sunGlowColor = Color(0x99FFB74D),
                moonColor = Color.Transparent,
                moonGlowColor = Color.Transparent,
                starsAlpha = 0.0f,
                cloudsAlpha = 0.45f,
                horizonGlowAlpha = 0.60f
            )
            CelestialSkyPhase.GOLDEN_HOUR -> SkyAtmosphereColors(
                topGradient = Color(0xFF1E293B),
                middleGradient = Color(0xFF9A3412),
                bottomGradient = Color(0xFFEA580C),
                sunColor = Color(0xFFFFCA28),
                sunGlowColor = Color(0xCCFF7043),
                moonColor = Color.Transparent,
                moonGlowColor = Color.Transparent,
                starsAlpha = 0.10f,
                cloudsAlpha = 0.55f,
                horizonGlowAlpha = 0.95f
            )
            CelestialSkyPhase.SUNSET_MAGHRIB -> SkyAtmosphereColors(
                topGradient = Color(0xFF0F172A),
                middleGradient = Color(0xFF581C87),
                bottomGradient = Color(0xFFBE185D),
                sunColor = Color(0xFFFF7043),
                sunGlowColor = Color(0x88D81B60),
                moonColor = Color(0xFFF1F5F9),
                moonGlowColor = Color(0x44E2E8F0),
                starsAlpha = 0.45f,
                cloudsAlpha = 0.40f,
                horizonGlowAlpha = 0.80f
            )
            CelestialSkyPhase.DUSK_TWILIGHT -> SkyAtmosphereColors(
                topGradient = Color(0xFF090D1A),
                middleGradient = Color(0xFF1E1B4B),
                bottomGradient = Color(0xFF311042),
                sunColor = Color.Transparent,
                sunGlowColor = Color.Transparent,
                moonColor = Color(0xFFF8FAFC),
                moonGlowColor = Color(0x66CBD5E1),
                starsAlpha = 0.75f,
                cloudsAlpha = 0.25f,
                horizonGlowAlpha = 0.35f
            )
            CelestialSkyPhase.NIGHT_ISHA -> SkyAtmosphereColors(
                topGradient = Color(0xFF020617),
                middleGradient = Color(0xFF0A0F24),
                bottomGradient = Color(0xFF101935),
                sunColor = Color.Transparent,
                sunGlowColor = Color.Transparent,
                moonColor = Color(0xFFF8FAFC),
                moonGlowColor = Color(0x77E2E8F0),
                starsAlpha = 0.90f,
                cloudsAlpha = 0.20f,
                horizonGlowAlpha = 0.15f
            )
        }
    }
}

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,
    val phaseOffset: Float
)

@Composable
fun DynamicCelestialSky(
    modifier: Modifier = Modifier,
    phase: CelestialSkyPhase = CelestialSkyPhase.NOON_DHUHR,
    simulatedHourFraction: Float? = null // 0.0f to 24.0f or null for real-time
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celestial_anim")

    // Slow atmospheric breathing & cloud drift
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_drift"
    )

    // Sun & Moon pulse
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

    // Stars twinkling clock
    val twinkleClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle_clock"
    )

    // Generate fixed random stars
    val stars = remember {
        val random = Random(42)
        List(60) {
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat() * 0.70f, // Upper 70% of sky
                radius = random.nextFloat() * 1.8f + 0.8f,
                baseAlpha = random.nextFloat() * 0.5f + 0.5f,
                twinkleSpeed = random.nextFloat() * 2.0f + 1.0f,
                phaseOffset = random.nextFloat() * 6.28f
            )
        }
    }

    // Time calculation for celestial positions
    val currentHourFraction = simulatedHourFraction ?: remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f + cal.get(Calendar.SECOND) / 3600f
    }

    val atmosphere = CelestialSkyThemes.getColorsForPhase(phase)

    // Smooth gradient transitions
    val animatedTop by androidx.compose.animation.animateColorAsState(atmosphere.topGradient, label = "top_col")
    val animatedMid by androidx.compose.animation.animateColorAsState(atmosphere.middleGradient, label = "mid_col")
    val animatedBot by androidx.compose.animation.animateColorAsState(atmosphere.bottomGradient, label = "bot_col")

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(animatedTop, animatedMid, animatedBot)
                )
            )
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Twinkling Stars if night / dawn / dusk
        if (atmosphere.starsAlpha > 0.05f) {
            stars.forEach { star ->
                val twinkle = (sin(twinkleClock * star.twinkleSpeed + star.phaseOffset) + 1f) / 2f
                val alpha = (star.baseAlpha * (0.4f + 0.6f * twinkle) * atmosphere.starsAlpha).coerceIn(0f, 1f)
                val starCenter = Offset(star.xRatio * width, star.yRatio * height)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.radius,
                    center = starCenter
                )
                if (star.radius > 1.8f && alpha > 0.6f) {
                    // Star cross sparkle
                    val arm = star.radius * 2.5f
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.4f),
                        start = Offset(starCenter.x - arm, starCenter.y),
                        end = Offset(starCenter.x + arm, starCenter.y),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.4f),
                        start = Offset(starCenter.x, starCenter.y - arm),
                        end = Offset(starCenter.x, starCenter.y + arm),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // 2. Draw Sun if Daytime
        if (phase.isDaytime && atmosphere.sunColor != Color.Transparent) {
            // Sun orbital arc: 6:00 (left) to 12:00 (top center) to 18:00 (right)
            val dayProgress = ((currentHourFraction - 6.0f) / 12.0f).coerceIn(0.0f, 1.0f)
            val sunX = width * (0.15f + 0.70f * dayProgress)
            val sunY = height * (0.45f - 0.30f * sin(dayProgress * PI.toFloat()))

            val sunRadius = 26.dp.toPx()

            // Outer coronal glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        atmosphere.sunGlowColor,
                        atmosphere.sunGlowColor.copy(alpha = atmosphere.sunGlowColor.alpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(sunX, sunY),
                    radius = sunRadius * 3.5f * sunPulse
                ),
                center = Offset(sunX, sunY),
                radius = sunRadius * 3.5f * sunPulse
            )

            // Inner solar body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        atmosphere.sunColor,
                        atmosphere.sunColor.copy(alpha = 0.8f)
                    ),
                    center = Offset(sunX, sunY),
                    radius = sunRadius
                ),
                center = Offset(sunX, sunY),
                radius = sunRadius
            )
        }

        // 3. Draw Moon if Nighttime
        if (!phase.isDaytime && atmosphere.moonColor != Color.Transparent) {
            // Moon orbital position
            val nightHour = if (currentHourFraction < 6.0f) currentHourFraction + 24.0f else currentHourFraction
            val nightProgress = ((nightHour - 18.0f) / 12.0f).coerceIn(0.0f, 1.0f)
            val moonX = width * (0.80f - 0.60f * nightProgress)
            val moonY = height * (0.35f - 0.20f * sin(nightProgress * PI.toFloat()))

            val moonRadius = 22.dp.toPx()

            // Moonlight halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        atmosphere.moonGlowColor,
                        atmosphere.moonGlowColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(moonX, moonY),
                    radius = moonRadius * 3.2f
                ),
                center = Offset(moonX, moonY),
                radius = moonRadius * 3.2f
            )

            // Crescent Moon Path
            drawMoonCrescent(
                center = Offset(moonX, moonY),
                radius = moonRadius,
                moonColor = atmosphere.moonColor
            )
        }

        // 4. Draw Floating Atmospheric Clouds
        if (atmosphere.cloudsAlpha > 0.05f) {
            drawAtmosphericClouds(
                width = width,
                height = height,
                driftOffset = cloudDrift,
                alpha = atmosphere.cloudsAlpha,
                isDay = phase.isDaytime
            )
        }

        // 5. Subtle Horizon Gradient Fog
        if (atmosphere.horizonGlowAlpha > 0.05f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        atmosphere.bottomGradient.copy(alpha = atmosphere.horizonGlowAlpha * 0.6f),
                        atmosphere.bottomGradient.copy(alpha = atmosphere.horizonGlowAlpha)
                    ),
                    startY = height * 0.60f,
                    endY = height
                ),
                topLeft = Offset(0f, height * 0.60f),
                size = androidx.compose.ui.geometry.Size(width, height * 0.40f)
            )
        }
    }
}

private fun DrawScope.drawMoonCrescent(
    center: Offset,
    radius: Float,
    moonColor: Color
) {
    // Full Moon base disc
    drawCircle(
        color = moonColor,
        radius = radius,
        center = center
    )

    // Inner crater texture / shadow overlay to make a realistic glowing crescent
    drawCircle(
        color = Color(0xFF070B18).copy(alpha = 0.88f),
        radius = radius * 0.90f,
        center = Offset(center.x - radius * 0.45f, center.y - radius * 0.15f)
    )

    // Silver rim highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawAtmosphericClouds(
    width: Float,
    height: Float,
    driftOffset: Float,
    alpha: Float,
    isDay: Boolean
) {
    val cloudColor = if (isDay) Color.White.copy(alpha = alpha * 0.35f) else Color(0xFF94A3B8).copy(alpha = alpha * 0.25f)

    // Cloud wave 1
    val offset1 = (driftOffset % (width + 300f)) - 150f
    drawCircle(
        color = cloudColor,
        radius = 70.dp.toPx(),
        center = Offset(offset1, height * 0.30f)
    )
    drawCircle(
        color = cloudColor,
        radius = 90.dp.toPx(),
        center = Offset(offset1 + 60.dp.toPx(), height * 0.32f)
    )
    drawCircle(
        color = cloudColor,
        radius = 65.dp.toPx(),
        center = Offset(offset1 + 120.dp.toPx(), height * 0.33f)
    )

    // Cloud wave 2 (slower, lower altitude)
    val offset2 = ((driftOffset * 0.6f) % (width + 400f)) - 200f
    val cloudColor2 = if (isDay) Color.White.copy(alpha = alpha * 0.22f) else Color(0xFF64748B).copy(alpha = alpha * 0.18f)
    drawCircle(
        color = cloudColor2,
        radius = 80.dp.toPx(),
        center = Offset(offset2 + width * 0.5f, height * 0.52f)
    )
    drawCircle(
        color = cloudColor2,
        radius = 110.dp.toPx(),
        center = Offset(offset2 + width * 0.5f + 80.dp.toPx(), height * 0.54f)
    )
}
