package com.example.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RichTextEditorToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPalette by remember { mutableStateOf(false) }
    var showHighlightPalette by remember { mutableStateOf(false) }
    var showIslamicSymbols by remember { mutableStateOf(false) }

    val textColors = listOf(
        "#F59E0B" to "ذهبي",
        "#10B981" to "زمردي",
        "#0284C7" to "أزرق",
        "#EF4444" to "أحمر",
        "#9333EA" to "بنفسجي",
        "#D97706" to "برتقالي",
        "#FFFFFF" to "أبيض",
        "#1E293B" to "داكن"
    )

    val highlightColors = listOf(
        "#FEF08A" to "أصفر خفيف",
        "#A7F3D0" to "أخضر نعناعي",
        "#BAE6FD" to "سماوي",
        "#FECDD3" to "وردي",
        "#E9D5FF" to "لافندر"
    )

    val islamicPhrases = listOf(
        "ﷺ",
        "ﷻ",
        "رضي الله عنه",
        "سبحانه وتعالى",
        "تعالى",
        "عليه السلام",
        "بسم الله الرحمن الرحيم"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Main Toolbar Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            ToolbarButton(
                text = "B",
                isBold = true,
                contentDescription = "عريض Bold",
                onClick = {
                    onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<b>", "</b>"))
                }
            )

            // Italic
            ToolbarButton(
                text = "I",
                isItalic = true,
                contentDescription = "مائل Italic",
                onClick = {
                    onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<i>", "</i>"))
                }
            )

            // Underline
            ToolbarButton(
                text = "U",
                isUnderline = true,
                contentDescription = "تحته خط Underline",
                onClick = {
                    onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<u>", "</u>"))
                }
            )

            // Larger Size
            ToolbarButton(
                text = "A⁺",
                contentDescription = "تكبير الكلمة",
                onClick = {
                    onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<big>", "</big>"))
                }
            )

            // Smaller Size
            ToolbarButton(
                text = "A⁻",
                contentDescription = "تصغير الكلمة",
                onClick = {
                    onValueChange(RichTextHelper.applyTagToSelection(textFieldValue, "<small>", "</small>"))
                }
            )

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Quranic Bracket
            ToolbarButton(
                text = "﴿ ﴾",
                contentDescription = "أقواس قرآنية",
                onClick = {
                    onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "﴿", "﴾"))
                }
            )

            // Quote Bracket
            ToolbarButton(
                text = "« »",
                contentDescription = "أقواس تنصيص",
                onClick = {
                    onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "«", "»"))
                }
            )

            // Islamic Phrases Dropdown Toggle
            FilledTonalIconButton(
                onClick = {
                    showIslamicSymbols = !showIslamicSymbols
                    showColorPalette = false
                    showHighlightPalette = false
                },
                modifier = Modifier.size(36.dp)
            ) {
                Text("🕌", fontSize = 14.sp)
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Color Palette Toggle
            FilledTonalIconButton(
                onClick = {
                    showColorPalette = !showColorPalette
                    showHighlightPalette = false
                    showIslamicSymbols = false
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = "تلوين الكلمة المحددة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Highlight Toggle
            FilledTonalIconButton(
                onClick = {
                    showHighlightPalette = !showHighlightPalette
                    showColorPalette = false
                    showIslamicSymbols = false
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.FormatColorFill,
                    contentDescription = "تظليل خلفية الكلمة",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Clear Formatting
            IconButton(
                onClick = {
                    onValueChange(RichTextHelper.stripHtmlTags(textFieldValue))
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.FormatClear,
                    contentDescription = "مسح التنسيق",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Expanded Color Palette
        if (showColorPalette) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "اختر لوناً لتلوين الكلمة أو الجملة المحددة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        textColors.forEach { (hex, name) ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color.Black
                            }

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
                                        showColorPalette = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(name, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Expanded Highlight Palette
        if (showHighlightPalette) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "اختر لون تظليل لخلفية الكلمة المحددة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        highlightColors.forEach { (hex, name) ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color.Yellow
                            }

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
                                        showHighlightPalette = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Expanded Islamic Phrases
        if (showIslamicSymbols) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "رموز وعبارات إسلامية جاهزة للإدراج:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        islamicPhrases.forEach { phrase ->
                            SuggestionChip(
                                onClick = {
                                    onValueChange(RichTextHelper.insertTextAtCursor(textFieldValue, phrase))
                                    showIslamicSymbols = false
                                },
                                label = { Text(phrase, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    isUnderline: Boolean = false
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
