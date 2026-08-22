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
import androidx.compose.runtime.remember
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
import com.example.ui.theme.AppCustomFontFamily
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
    val bgColor = rememberParsedColor(config.backgroundColorHex, Color(0xFF161B22))
    val gradStart = rememberParsedColor(config.gradientStartColorHex, bgColor)
    val gradEnd = rememberParsedColor(config.gradientEndColorHex, bgColor)
    val textColor = rememberParsedColor(config.textColorHex, Color.White)
    val titleColor = rememberParsedColor(config.titleColorHex, Color(0xFF00E5FF))
    val borderColor = rememberParsedColor(config.borderColorHex, Color(0x3300E5FF))

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

    val titleFontWeight = when (config.titleFontWeight) {
        "NORMAL" -> FontWeight.Normal
        else -> FontWeight.Bold
    }

    val textFontStyle = if (config.isItalic) FontStyle.Italic else FontStyle.Normal
    val textDecoration = if (config.isUnderline) TextDecoration.Underline else TextDecoration.None

    val textAlign = when (config.textAlignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val titleTextAlign = when (config.titleAlignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val selectedFontFamily = remember(config.fontFamily, config.customFontPath) {
        if (!config.customFontPath.isNullOrBlank()) {
            try {
                val file = java.io.File(config.customFontPath)
                if (file.exists()) {
                    val typeface = android.graphics.Typeface.createFromFile(file)
                    FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
                } else {
                    AppCustomFontFamily
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                AppCustomFontFamily
            }
        } else {
            when (config.fontFamily) {
                "CAIRO" -> FontFamily.Serif
                "AMIRI" -> FontFamily.Serif
                "NOTO_KUFI" -> FontFamily.SansSerif
                else -> AppCustomFontFamily // Default to the stylish Tajawal/F5 font!
            }
        }
    }

    val displayItems = remember(items, config.currentContentIndex, config.rotationMode) {
        if (items.isEmpty()) emptyList()
        else {
            val validIndex = (config.currentContentIndex % items.size).let { if (it < 0) it + items.size else it }
            listOf(items[validIndex])
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Widget Header Row (Category / Custom Name / Date)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.showCategory && !categoryName.isNullOrBlank()) {
                    Text(
                        text = categoryName,
                        color = titleColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = selectedFontFamily
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (config.showDate) {
                    val currentDate = remember {
                        val sdf = SimpleDateFormat("EEEE d MMMM", Locale("ar"))
                        sdf.format(Date())
                    }
                    Text(
                        text = currentDate,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = selectedFontFamily
                    )
                }
            }

            // Widget Content Body
            if (displayItems.isEmpty()) {
                Text(
                    text = "لا توجد أذكار مضافة لعرضها",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = config.fontSize.sp,
                    fontFamily = selectedFontFamily,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val item = displayItems.first()

                if (item.title.isNotBlank()) {
                    Text(
                        text = RichTextHelper.htmlToAnnotatedString(item.title, titleColor),
                        color = titleColor,
                        fontSize = (config.fontSize + 2).sp,
                        fontWeight = titleFontWeight,
                        fontFamily = selectedFontFamily,
                        textAlign = titleTextAlign,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = RichTextHelper.htmlToAnnotatedString(item.body, textColor),
                    color = textColor,
                    fontSize = config.fontSize.sp,
                    fontWeight = textFontWeight,
                    fontStyle = textFontStyle,
                    textDecoration = textDecoration,
                    fontFamily = selectedFontFamily,
                    textAlign = textAlign,
                    lineHeight = (config.fontSize * config.lineSpacing).sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            // Widget Interactive Action Bar (Next / Refresh)
            if (onNextClick != null || onRefreshClick != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onRefreshClick != null) {
                        IconButton(
                            onClick = onRefreshClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث",
                                tint = titleColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (onNextClick != null) {
                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = "التالي",
                                tint = titleColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberParsedColor(colorHex: String?, defaultColor: Color): Color {
    return remember(colorHex) {
        if (colorHex.isNullOrBlank()) defaultColor
        else {
            try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                defaultColor
            }
        }
    }
}
