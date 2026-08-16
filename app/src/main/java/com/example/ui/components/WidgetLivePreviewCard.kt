package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WidgetLivePreviewCard(
    config: WidgetConfigEntity,
    items: List<ContentItemEntity>,
    categoryName: String? = null,
    onRefreshClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Parse Colors safely
    val bgColor = rememberParsedColor(config.backgroundColorHex, Color(0xFF1E293B))
    val gradStart = rememberParsedColor(config.gradientStartColorHex, bgColor)
    val gradEnd = rememberParsedColor(config.gradientEndColorHex, bgColor)
    val textColor = rememberParsedColor(config.textColorHex, Color.White)
    val titleColor = rememberParsedColor(config.titleColorHex, Color(0xFFF59E0B))
    val borderColor = rememberParsedColor(config.borderColorHex, Color(0x33FFFFFF))

    val cornerRadius = config.cornerRadius.dp
    val padding = config.padding.dp

    val backgroundBrush = when (config.gradientDirection) {
        "TOP_BOTTOM" -> Brush.verticalGradient(listOf(gradStart.copy(alpha = config.backgroundOpacity), gradEnd.copy(alpha = config.backgroundOpacity)))
        "LEFT_RIGHT" -> Brush.horizontalGradient(listOf(gradStart.copy(alpha = config.backgroundOpacity), gradEnd.copy(alpha = config.backgroundOpacity)))
        "DIAGONAL" -> Brush.linearGradient(listOf(gradStart.copy(alpha = config.backgroundOpacity), gradEnd.copy(alpha = config.backgroundOpacity)))
        else -> Brush.linearGradient(listOf(bgColor.copy(alpha = config.backgroundOpacity), bgColor.copy(alpha = config.backgroundOpacity)))
    }

    val textFontWeight = when (config.fontWeight) {
        "BOLD" -> FontWeight.Bold
        "MEDIUM" -> FontWeight.Medium
        else -> FontWeight.Normal
    }

    val textFontStyle = if (config.isItalic) FontStyle.Italic else FontStyle.Normal
    val textDecoration = if (config.isUnderline) TextDecoration.Underline else TextDecoration.None

    val textAlign = when (config.textAlignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val selectedFontFamily = androidx.compose.runtime.remember(config.fontFamily, config.customFontPath) {
        if (!config.customFontPath.isNullOrBlank()) {
            try {
                val file = java.io.File(config.customFontPath)
                if (file.exists()) {
                    val typeface = android.graphics.Typeface.createFromFile(file)
                    FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
                } else {
                    FontFamily.Default
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                FontFamily.Default
            }
        } else {
            when (config.fontFamily) {
                "CAIRO" -> FontFamily.Serif
                "AMIRI" -> FontFamily.Serif
                "NOTO_KUFI" -> FontFamily.SansSerif
                else -> FontFamily.Default
            }
        }
    }

    val displayItems = androidx.compose.runtime.remember(items, config.currentContentIndex, config.rotationMode) {
        if (items.isNotEmpty()) {
            val totalCount = items.size
            val currentIndex = (config.currentContentIndex % totalCount + totalCount) % totalCount
            when (config.rotationMode) {
                "RANDOM" -> {
                    val random = java.util.Random(config.currentContentIndex.toLong())
                    val shuffled = items.toMutableList()
                    java.util.Collections.shuffle(shuffled, random)
                    shuffled
                }
                else -> {
                    items.drop(currentIndex) + items.take(currentIndex)
                }
            }
        } else {
            emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundBrush)
            .border(config.borderWidth.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(padding)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (config.showTitle) {
                    Text(
                        text = config.name,
                        color = titleColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (config.showCategory && !categoryName.isNull_orEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = categoryName ?: "",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onRefreshClick?.invoke() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNextClick?.invoke() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NavigateNext,
                            contentDescription = "التالي",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White.copy(alpha = 0.2f)
            )

            // Scrollable Content Area
            if (displayItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد محتوى محدد للودجت",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayItems, key = { it.id }) { item ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = when (config.textAlignment) {
                                "LEFT" -> Alignment.Start
                                "RIGHT" -> Alignment.End
                                else -> Alignment.CenterHorizontally
                            }
                        ) {
                            if (item.title.isNotBlank() && config.showTitle) {
                                val annotatedTitle = RichTextHelper.htmlToAnnotatedString(
                                    htmlText = item.title,
                                    defaultColor = titleColor,
                                    defaultFontSize = (config.fontSize - 1).coerceAtLeast(10).sp
                                )
                                Text(
                                    text = annotatedTitle,
                                    color = titleColor,
                                    fontSize = (config.fontSize - 1).coerceAtLeast(10).sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = textAlign,
                                    fontFamily = selectedFontFamily
                                )
                            }
                            val annotatedBody = RichTextHelper.htmlToAnnotatedString(
                                htmlText = item.body,
                                defaultColor = textColor,
                                defaultFontSize = config.fontSize.sp
                            )
                            Text(
                                text = annotatedBody,
                                color = textColor,
                                fontSize = config.fontSize.sp,
                                fontWeight = textFontWeight,
                                fontStyle = textFontStyle,
                                textDecoration = textDecoration,
                                textAlign = textAlign,
                                lineHeight = (config.fontSize * config.lineSpacing).sp,
                                fontFamily = selectedFontFamily,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Footer Date
            if (config.showDate) {
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
                Text(
                    text = dateFormat.format(Date()),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun rememberParsedColor(colorHex: String?, fallback: Color): Color {
    return try {
        if (colorHex.isNull_orEmpty()) fallback
        else Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        fallback
    }
}

private fun String?.isNull_orEmpty(): Boolean = this == null || this.isEmpty()
