package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GlassDefaults {
    val CardShape = RoundedCornerShape(24.dp)
    val SmallCardShape = RoundedCornerShape(16.dp)
    val ButtonShape = RoundedCornerShape(20.dp)
    val PillShape = RoundedCornerShape(50.dp)

    val GlassBorderWidth = 1.2.dp
    val GlassGlowBorderWidth = 1.8.dp

    // Frosted glass background brush for dark/night or bright skies
    val GlassGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x38FFFFFF),
            Color(0x18FFFFFF),
            Color(0x08FFFFFF)
        )
    )

    val GlassGradientDark = Brush.verticalGradient(
        colors = listOf(
            Color(0x331E293B),
            Color(0x220F172A),
            Color(0x11020617)
        )
    )

    val GlassBorderGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x99FFFFFF),
            Color(0x33FFFFFF),
            Color(0x0DFFFFFF)
        )
    )

    fun accentBorderGradient(accentColor: Color) = Brush.verticalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.9f),
            accentColor.copy(alpha = 0.35f),
            Color(0x1AFFFFFF)
        )
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassDefaults.CardShape,
    elevation: Dp = 8.dp,
    borderBrush: Brush = GlassDefaults.GlassBorderGradient,
    backgroundBrush: Brush = GlassDefaults.GlassGradient,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(GlassDefaults.GlassBorderWidth, borderBrush, shape)
            .then(clickableModifier)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = Color(0xFF00E5FF),
    shape: Shape = GlassDefaults.ButtonShape,
    testTag: String = "glass_button"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(elevation = 6.dp, shape = shape, spotColor = accentColor.copy(alpha = 0.4f))
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.28f),
                        Color(0x22FFFFFF),
                        accentColor.copy(alpha = 0.18f)
                    )
                )
            )
            .border(
                GlassDefaults.GlassGlowBorderWidth,
                GlassDefaults.accentBorderGradient(accentColor),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    accentGlow: Color? = null,
    size: Dp = 48.dp,
    contentDescription: String? = null,
    testTag: String = "glass_icon_button"
) {
    val borderBrush = if (accentGlow != null) {
        GlassDefaults.accentBorderGradient(accentGlow)
    } else {
        GlassDefaults.GlassBorderGradient
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentGlow?.copy(alpha = 0.25f) ?: Color(0x33FFFFFF),
                        Color(0x15FFFFFF)
                    )
                )
            )
            .border(1.dp, borderBrush, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF),
    icon: ImageVector? = null,
    testTag: String = "glass_chip"
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x1FFFFFFF),
        label = "chip_bg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.85f) else Color(0x33FFFFFF),
        label = "chip_border"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(GlassDefaults.PillShape)
            .background(animatedBg)
            .border(1.dp, animatedBorder, GlassDefaults.PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                    tint = if (isSelected) Color.White else Color(0xCCFFFFFF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xDDFFFFFF),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
