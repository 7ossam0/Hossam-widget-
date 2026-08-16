package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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

    var showTemplateDialog by remember { mutableStateOf(false) }

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

    // Ready-made Formatting Presets
    val formattingPresets = listOf(
        FormattingPreset(
            id = "quran_gold",
            title = "آية قرآنية مذهبة",
            description = "أقواس المصحف بلون ذهبي وخط غامق",
            template = "﴿ <font color=\"#F59E0B\"><b>{text}</b></font> ﴾",
            defaultPlaceholder = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ"
        ),
        FormattingPreset(
            id = "hadith_green",
            title = "حديث شريف مع السند",
            description = "أقواس تنصيص بلون زمردي مع تخريج سفلي",
            template = "« <font color=\"#10B981\"><b>{text}</b></font> » <small><font color=\"#94A3B8\">[رواه البخاري]</font></small>",
            defaultPlaceholder = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"
        ),
        FormattingPreset(
            id = "dhikr_counter",
            title = "ذكر وتسبيح مع العداد",
            description = "نص ملون مع عداد مظلل أصفر بارز",
            template = "<b><font color=\"#0284C7\">{text}</font></b> <mark style=\"background-color:#FEF08A\"><font color=\"#D97706\"><b>33×</b></font></mark>",
            defaultPlaceholder = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
        ),
        FormattingPreset(
            id = "duaa_blessing",
            title = "دعاء مع البسملة والصلاة",
            description = "بسملة كهرمانية ودعاء متبوع بـ ﷺ",
            template = "<font color=\"#D97706\"><b>بِسْمِ اللَّـهِ الرَّحْمَـٰنِ الرَّحِيمِ</b></font><br/>{text} <font color=\"#10B981\">ﷺ</font>",
            defaultPlaceholder = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ"
        ),
        FormattingPreset(
            id = "highlight_quote",
            title = "اقتباس مظلل لافت",
            description = "خلفية صفراء بارزة مع خط أسود عريض",
            template = "<span style=\"background-color:#FEF08A\"><font color=\"#1E293B\"><b>« {text} »</b></font></span>",
            defaultPlaceholder = "مَن سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ"
        ),
        FormattingPreset(
            id = "title_bullet_points",
            title = "عنوان بارز مع نقاط",
            description = "عنوان بلون ذهبي كبير ونقاط فرعية ملونة",
            template = "<big><font color=\"#F59E0B\"><b>★ {text}</b></font></big><br/>• <font color=\"#10B981\">النقطة الأولى...</font><br/>• <font color=\"#10B981\">النقطة الثانية...</font>",
            defaultPlaceholder = "فضل أذكار الصباح والمساء"
        )
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

                // 9. Presets & Ready Templates
                ToolbarIconButton(
                    label = "✨",
                    contentDescription = "قوالب تنسيق جاهزة",
                    isActive = activeSubMenu == GranularSubMenu.PRESETS,
                    onClick = {
                        activeSubMenu = if (activeSubMenu == GranularSubMenu.PRESETS) null else GranularSubMenu.PRESETS
                    }
                )

                // 10. Advanced Code & Template Composer Dialog
                ToolbarIconButton(
                    icon = Icons.Default.Code,
                    contentDescription = "إدراج نص داخل كود جاهز",
                    onClick = {
                        showTemplateDialog = true
                    }
                )

                VerticalDivider(modifier = Modifier.height(26.dp).padding(horizontal = 2.dp))

                // 11. Quran Brackets Direct Shortcut
                ToolbarButton(
                    text = "﴿ ﴾",
                    contentDescription = "أقواس الآيات القرآنية",
                    onClick = {
                        onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "﴿", "﴾"))
                    }
                )

                // 12. Quote Brackets
                ToolbarButton(
                    text = "« »",
                    contentDescription = "أقواس التنصيص",
                    onClick = {
                        onValueChange(RichTextHelper.insertIslamicDecor(textFieldValue, "«", "»"))
                    }
                )

                VerticalDivider(modifier = Modifier.height(26.dp).padding(horizontal = 2.dp))

                // 13. Clear Formatting
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

                            GranularSubMenu.PRESETS -> {
                                Text(
                                    text = "اختر قالب تنسيق جاهز (سيتم تطبيق التنسيق على النص المحدد أو إدراجه فوراً):",
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
                                    formattingPresets.forEach { preset ->
                                        Surface(
                                            onClick = {
                                                val selectedText = textFieldValue.getSelectedText().text
                                                val contentToInsert = if (selectedText.isNotEmpty()) {
                                                    preset.template.replace("{text}", selectedText)
                                                } else {
                                                    preset.template.replace("{text}", preset.defaultPlaceholder)
                                                }
                                                onValueChange(RichTextHelper.insertTextAtCursor(textFieldValue, contentToInsert))
                                                activeSubMenu = null
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                Text(preset.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(preset.description, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            null -> {}
                        }
                    }
                }
            }

            // Dialog for inserting custom text inside ready-made formatting template
            if (showTemplateDialog) {
                PresetTemplateComposerDialog(
                    presets = formattingPresets,
                    initialText = textFieldValue.getSelectedText().text,
                    onDismiss = { showTemplateDialog = false },
                    onInsert = { formattedCode ->
                        onValueChange(RichTextHelper.insertTextAtCursor(textFieldValue, formattedCode))
                        showTemplateDialog = false
                    }
                )
            }
        }
    }
}

data class FormattingPreset(
    val id: String,
    val title: String,
    val description: String,
    val template: String,
    val defaultPlaceholder: String
)

private enum class GranularSubMenu {
    SIZING, TEXT_COLOR, HIGHLIGHT, ISLAMIC, PRESETS
}

@Composable
fun PresetTemplateComposerDialog(
    presets: List<FormattingPreset>,
    initialText: String,
    onDismiss: () -> Unit,
    onInsert: (String) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(presets.first()) }
    var userCustomText by remember { mutableStateOf(if (initialText.isNotBlank()) initialText else selectedPreset.defaultPlaceholder) }
    var selectedColorHex by remember { mutableStateOf("#F59E0B") }

    val formattedResult = remember(selectedPreset, userCustomText, selectedColorHex) {
        val targetText = if (userCustomText.isNotBlank()) userCustomText else selectedPreset.defaultPlaceholder
        selectedPreset.template.replace("{text}", targetText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B))
                Text("إضافة نص داخل كود تنسيق جاهز", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("1. اختر نوع قالب التنسيق:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = selectedPreset.id == preset.id,
                            onClick = {
                                selectedPreset = preset
                                if (userCustomText.isBlank() || presets.any { it.defaultPlaceholder == userCustomText }) {
                                    userCustomText = preset.defaultPlaceholder
                                }
                            },
                            label = { Text(preset.title, fontSize = 11.sp) }
                        )
                    }
                }

                Text("2. اكتب أو الصق النص المراد وضعه داخل القالب:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = userCustomText,
                    onValueChange = { userCustomText = it },
                    label = { Text("النص المطلوب تنسيقه") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("3. معاينة شكل النص بالتنسيق المختار:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = RichTextHelper.htmlToAnnotatedString(formattedResult),
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Show raw generated code preview
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedResult,
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onInsert(formattedResult) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إدراج في المحتوى")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
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
