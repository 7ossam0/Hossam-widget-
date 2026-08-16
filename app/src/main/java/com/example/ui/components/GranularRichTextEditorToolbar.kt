package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GranularRichTextEditorToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubMenu by remember { mutableStateOf<GranularSubMenu?>(null) }

    // Curated rich color palette
    val textColors = listOf(
        "#F59E0B" to "ذهبي",
        "#10B981" to "زمردي",
        "#0284C7" to "سماوي",
        "#3B82F6" to "أزرق نيلي",
        "#8B5CF6" to "بنفسجي",
        "#EC4899" to "وردي",
        "#EF4444" to "أحمر قرمزي",
        "#D97706" to "كهرماني",
        "#14B8A6" to "تركواز",
        "#FFFFFF" to "أبيض",
        "#1E293B" to "داكن كحلي"
    )

    // Highlight background colors
    val highlightColors = listOf(
        "#FEF08A" to "أصفر شمس",
        "#A7F3D0" to "أخضر نعناع",
        "#BAE6FD" to "سماوي هادئ",
        "#FECDD3" to "وردي خفيف",
        "#E9D5FF" to "لافندر ملكي",
        "#FED7AA" to "مشمشي دافئ"
    )

    // Islamic honorifics & symbols
    val islamicPhrases = listOf(
        "ﷺ" to "الصلاة على النبي",
        "ﷻ" to "جل جلاله",
        "رضي الله عنه" to "ترضي",
        "رضي الله عنها" to "ترضي للمؤنث",
        "رضي الله عنهم" to "ترضي للجمع",
        "رحمه الله" to "ترحم",
        "سبحانه وتعالى" to "تسبيح",
        "عليه السلام" to "سلام",
        "عز وجل" to "تعظيم",
        "﴿ ﴾" to "أقواس المصحف",
        "« »" to "أقواس تنصيص",
        "بسم الله الرحمن الرحيم" to "البسملة كاملة"
    )

    // Granular Font Sizes
    val fontSizes = listOf(
        "<small><small>" to "</small></small>" to "صغير جداً (0.6x)",
        "<small>" to "</small>" to "صغير (0.8x)",
        "<big>" to "</big>" to "كبير (1.25x)",
        "<big><big>" to "</big></big>" to "كبير جداً (1.5x)",
        "<big><big><big>" to "</big></big></big>" to "ضخم مميز (1.8x)"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Granular formatting indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "التنسيق الدقيق للكلمات والحروف (Granular Formatting)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (textFieldValue.getSelectedText().text.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "محدد: \"${textFieldValue.getSelectedText().text.take(15)}...\"",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Primary Interactive Toolbar Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Bold
                ToolbarButton(
                    text = "B",
                    isBold = true,
                    contentDescription = "عريض Bold",
                    onClick = {
                        onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<b>", "</b>"))
                    }
                )

                // 2. Italic
                ToolbarButton(
                    text = "I",
                    isItalic = true,
                    contentDescription = "مائل Italic",
                    onClick = {
                        onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<i>", "</i>"))
                    }
                )

                // 3. Underline
                ToolbarButton(
                    text = "U",
                    isUnderline = true,
                    contentDescription = "تسطير سفلي Underline",
                    onClick = {
                        onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<u>", "</u>"))
                    }
                )

                // 4. Strikethrough
                ToolbarButton(
                    text = "S",
                    isStrike = true,
                    contentDescription = "يتوسطه خط Strikethrough",
                    onClick = {
                        onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<s>", "</s>"))
                    }
                )

                VerticalDivider(modifier = Modifier.height(26.dp).padding(horizontal = 2.dp))

                // 5. Word Size Selector (Granular Scaling)
                ToolbarIconButton(
                    icon = Icons.Default.FormatSize,
                    contentDescription = "حجم الكلمة المحددة",
                    isActive = activeSubMenu == GranularSubMenu.SIZING,
                    onClick = {
                        activeSubMenu = if (activeSubMenu == GranularSubMenu.SIZING) null else GranularSubMenu.SIZING
                    }
                )

                // 6. Word Color Palette
                ToolbarIconButton(
                    icon = Icons.Default.Palette,
                    contentDescription = "لون الكلمة المحددة",
                    isActive = activeSubMenu == GranularSubMenu.TEXT_COLOR,
                    activeTint = Color(0xFFF59E0B),
                    onClick = {
                        activeSubMenu = if (activeSubMenu == GranularSubMenu.TEXT_COLOR) null else GranularSubMenu.TEXT_COLOR
                    }
                )

                // 7. Word Highlight Background
                ToolbarIconButton(
                    icon = Icons.Default.FormatColorFill,
                    contentDescription = "تظليل الكلمة المحددة",
                    isActive = activeSubMenu == GranularSubMenu.HIGHLIGHT,
                    activeTint = Color(0xFF10B981),
                    onClick = {
                        activeSubMenu = if (activeSubMenu == GranularSubMenu.HIGHLIGHT) null else GranularSubMenu.HIGHLIGHT
                    }
                )

                VerticalDivider(modifier = Modifier.height(26.dp).padding(horizontal = 2.dp))

                // 8. Islamic Honorifics & Brackets
                ToolbarIconButton(
                    label = "🕌",
                    contentDescription = "رموز وأقواس إسلامية",
                    isActive = activeSubMenu == GranularSubMenu.ISLAMIC,
                    onClick = {
                        activeSubMenu = if (activeSubMenu == GranularSubMenu.ISLAMIC) null else GranularSubMenu.ISLAMIC
                    }
                )

                // 9. Quran Brackets Direct Shortcut
                ToolbarButton(
                    text = "﴿ ﴾",
                    contentDescription = "أقواس الآيات القرآنية",
                    onClick = {
                        onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "﴿", "﴾"))
                    }
                )

                // 10. Quote Brackets
                ToolbarButton(
                    text = "« »",
                    contentDescription = "أقواس التنصيص",
                    onClick = {
                        onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "«", "»"))
                    }
                )

                VerticalDivider(modifier = Modifier.height(26.dp).padding(horizontal = 2.dp))

                // 11. Clear Formatting
                ToolbarIconButton(
                    icon = Icons.Default.FormatClear,
                    contentDescription = "إزالة التنسيق عن المحدد",
                    onClick = {
                        onValueChange(RichTextHelper.stripHtmlTags(textFieldValue))
                        activeSubMenu = null
                    }
                )
            }

            // Animated Sub-menus for Fine-Grained Controls
            AnimatedVisibility(
                visible = activeSubMenu != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (activeSubMenu) {
                            GranularSubMenu.SIZING -> {
                                Text(
                                    text = "اختر حجم الكلمة المحددة بدقة:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    fontSizes.forEach { (tags, label) ->
                                        val (openTag, closeTag) = tags
                                        SuggestionChip(
                                            onClick = {
                                                onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, openTag, closeTag))
                                                activeSubMenu = null
                                            },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }
                            }

                            GranularSubMenu.TEXT_COLOR -> {
                                Text(
                                    text = "اختر لون الكلمة أو العبارة المحددة:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    textColors.forEach { (hex, name) ->
                                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Black }
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    onValueChange(
                                                        RichTextHelper.applyTagToSelection(
                                                            textFieldValue,
                                                            "<font color=\"$hex\">",
                                                            "</font>"
                                                        )
                                                    )
                                                    activeSubMenu = null
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            GranularSubMenu.HIGHLIGHT -> {
                                Text(
                                    text = "اختر لون تظليل خلفية الكلمة (Highlight):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    highlightColors.forEach { (hex, name) ->
                                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Yellow }
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(color)
                                                .clickable {
                                                    onValueChange(
                                                        RichTextHelper.applyTagToSelection(
                                                            textFieldValue,
                                                            "<span style=\"background-color:$hex\">",
                                                            "</span>"
                                                        )
                                                    )
                                                    activeSubMenu = null
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(name, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            GranularSubMenu.ISLAMIC -> {
                                Text(
                                    text = "رموز، علامات صلاة، وأقواس شريفة للإدراج المباشر:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    islamicPhrases.forEach { (phrase, label) ->
                                        SuggestionChip(
                                            onClick = {
                                                if (phrase == "﴿ ﴾" || phrase == "« »") {
                                                    val parts = phrase.split(" ")
                                                    onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, parts[0], parts[1]))
                                                } else {
                                                    onValueChange(RichTextHelper.insertTextAtCursor(textFieldValue, phrase))
                                                }
                                                activeSubMenu = null
                                            },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(phrase, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("($label)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

private enum class GranularSubMenu {
    SIZING, TEXT_COLOR, HIGHLIGHT, ISLAMIC
}

@Composable
private fun ToolbarButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    isUnderline: Boolean = false,
    isStrike: Boolean = false
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = androidx.compose.ui.text.TextStyle(
                fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                textDecoration = when {
                    isUnderline -> androidx.compose.ui.text.style.TextDecoration.Underline
                    isStrike -> androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else -> androidx.compose.ui.text.style.TextDecoration.None
                }
            )
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String? = null,
    contentDescription: String,
    isActive: Boolean = false,
    activeTint: Color? = null,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
        shape = RoundedCornerShape(8.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor
        )
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (isActive) (activeTint ?: MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        } else if (label != null) {
            Text(
                label,
                fontSize = 15.sp
            )
        }
    }
}
