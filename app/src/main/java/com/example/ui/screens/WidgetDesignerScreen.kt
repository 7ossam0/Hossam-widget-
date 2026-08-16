package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.data.model.CustomFontEntity
import com.example.data.model.WidgetConfigEntity
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.GranularRichTextEditorToolbar
import com.example.ui.components.RichTextHelper
import com.example.ui.components.WidgetLivePreviewCard
import com.example.viewmodel.MainViewModel
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetDesignerScreen(
    initialConfig: WidgetConfigEntity,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    var config by remember { mutableStateOf(initialConfig) }

    val categories by viewModel.categories.collectAsState()
    val contentItems by viewModel.contentItems.collectAsState()
    val customFonts by viewModel.customFonts.collectAsState()
    val context = LocalContext.current

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri)
            viewModel.importCustomFont(uri, fileName) { imported ->
                config = config.copy(
                    fontFamily = "CUSTOM",
                    customFontPath = imported.filePath
                )
            }
        }
    }

    var previewItems by remember { mutableStateOf<List<ContentItemEntity>>(emptyList()) }

    LaunchedEffect(config) {
        previewItems = viewModel.getItemsForWidget(config)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // Default to Content & Live Rich Text Styler tab
    val tabs = listOf("المحتوى والتحرير الحر", "تنسيق الخطوط", "الألوان", "الهيكل", "التناوب")

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مصمم الودجت - Live Designer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث المعاينة")
                    }
                    Button(
                        onClick = {
                            viewModel.saveWidgetConfig(config)
                            onBackClick()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ التعديلات")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pinned Live Preview Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معاينة مباشرة للودجت على شاشة الهاتف (Live Preview)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val categoryName = remember(config.categoryId, categories) {
                        categories.find { it.id == config.categoryId }?.name ?: "الكل"
                    }

                    WidgetLivePreviewCard(
                        config = config,
                        items = previewItems,
                        categoryName = categoryName,
                        onRefreshClick = {
                            config = config.copy(currentContentIndex = config.currentContentIndex + 1)
                        },
                        onNextClick = {
                            config = config.copy(currentContentIndex = config.currentContentIndex + 1)
                        }
                    )
                }
            }

            // Custom Tab Navigation for Design Settings
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Scrollable Options Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> ContentSourceAndLiveEditorTab(
                        config = config,
                        categories = categories,
                        contentItems = contentItems,
                        previewItems = previewItems,
                        onUpdateItem = { viewModel.updateContentItem(it) },
                        onUpdateConfig = { config = it }
                    )
                    1 -> TypographyTab(
                        config = config,
                        customFonts = customFonts,
                        onPickCustomFont = { fontPickerLauncher.launch("*/*") },
                        onDeleteCustomFont = { viewModel.deleteCustomFont(it) },
                        onUpdateConfig = { config = it }
                    )
                    2 -> ColorsTab(config) { config = it }
                    3 -> LayoutAndBordersTab(config) { config = it }
                    4 -> RotationAndUpdatesTab(config) { config = it }
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "custom_font.ttf"
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex) ?: "custom_font.ttf"
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentSourceAndLiveEditorTab(
    config: WidgetConfigEntity,
    categories: List<CategoryEntity>,
    contentItems: List<ContentItemEntity>,
    previewItems: List<ContentItemEntity>,
    onUpdateItem: (ContentItemEntity) -> Unit,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    // Determine active item to edit in place
    val activeItem = remember(previewItems, config.currentContentIndex, config.singleContentId) {
        if (previewItems.isNotEmpty()) {
            val safeIdx = (config.currentContentIndex % previewItems.size).let { if (it < 0) it + previewItems.size else it }
            previewItems[safeIdx]
        } else if (config.singleContentId != null) {
            contentItems.find { it.id == config.singleContentId }
        } else {
            contentItems.firstOrNull()
        }
    }

    var selectedItemForEdit by remember { mutableStateOf<ContentItemEntity?>(null) }

    LaunchedEffect(activeItem) {
        if (selectedItemForEdit == null || selectedItemForEdit?.id != activeItem?.id) {
            selectedItemForEdit = activeItem
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // --- 1. Live In-Place Granular Text Designer Section ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التحرير والتصميم الحر للنص المعروض", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "معاينة وتعديل فوري ⚡",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "حدد أي كلمة أو سطر بالأسفل لتغيير لونه أو حجمه أو اتجاهه أو تمييزه بحرية تامة دون التأثير على باقي النص:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Item Switcher if multiple items exist
                if (previewItems.size > 1) {
                    Text("اختر النص للتعديل السريع:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        previewItems.forEachIndexed { index, item ->
                            val isCurrent = selectedItemForEdit?.id == item.id
                            FilterChip(
                                selected = isCurrent,
                                onClick = {
                                    selectedItemForEdit = item
                                    onUpdateConfig(config.copy(currentContentIndex = index))
                                },
                                label = {
                                    Text(
                                        text = item.title.ifEmpty { "نص #${index + 1}" },
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // In-place Item Editor Form
                selectedItemForEdit?.let { currentItem ->
                    var titleText by remember(currentItem.id) { mutableStateOf(currentItem.title) }
                    var bodyTextFieldValue by remember(currentItem.id) { mutableStateOf(TextFieldValue(currentItem.body)) }
                    var hasSavedFeedback by remember { mutableStateOf(false) }

                    // Title Field
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { newTitle ->
                            titleText = newTitle
                            val updated = currentItem.copy(title = newTitle)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        },
                        label = { Text("عنوان النص الفرعي (Title)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Body Field with Rich Text Editor
                    Text("نص المحتوى (تحديد الكلمات للتنسيق المباشر):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = bodyTextFieldValue,
                        onValueChange = { newBodyVal ->
                            bodyTextFieldValue = newBodyVal
                            val updated = currentItem.copy(body = newBodyVal.text)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        },
                        label = { Text("محتوى النص / الدعاء / الذكر") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                    )

                    // Rich Text Granular Toolbar attached directly under the body editor
                    GranularRichTextEditorToolbar(
                        textFieldValue = bodyTextFieldValue,
                        onValueChange = { newBodyVal ->
                            bodyTextFieldValue = newBodyVal
                            val updated = currentItem.copy(body = newBodyVal.text)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        }
                    )

                    // Save and Confirm Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasSavedFeedback) {
                            Text(
                                "✓ تم تطبيق وحفظ التعديلات على الودجت بنجاح",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                val updated = currentItem.copy(title = titleText, body = bodyTextFieldValue.text)
                                onUpdateItem(updated)
                                hasSavedFeedback = true
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ تعديل هذا النص", fontSize = 12.sp)
                        }
                    }
                } ?: run {
                    Text(
                        "لا يوجد نص محدد حالياً، يرجى اختيار تصنيف أو إضافة نصوص من شاشة المحتوى والأذكار.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // --- 2. Widget Source Settings (Category / Single Content Mode) ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات مصدر الودجت والاسم العام", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                OutlinedTextField(
                    value = config.name,
                    onValueChange = { onUpdateConfig(config.copy(name = it)) },
                    label = { Text("اسم الودجت في القائمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("نمط عرض المحتوى", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.contentMode == "CATEGORY_ALL",
                        onClick = { onUpdateConfig(config.copy(contentMode = "CATEGORY_ALL")) },
                        label = { Text("تصنيف كامل (تناوب)") }
                    )
                    FilterChip(
                        selected = config.contentMode == "SINGLE",
                        onClick = { onUpdateConfig(config.copy(contentMode = "SINGLE")) },
                        label = { Text("نص واحد ثابت") }
                    )
                }

                if (config.contentMode == "CATEGORY_ALL") {
                    Text("اختر التصنيف المخصص للودجت", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = config.categoryId == null,
                            onClick = { onUpdateConfig(config.copy(categoryId = null)) },
                            label = { Text("جميع التصنيفات") }
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = config.categoryId == cat.id,
                                onClick = { onUpdateConfig(config.copy(categoryId = cat.id)) },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }

                if (config.contentMode == "SINGLE") {
                    Text("اختر النص الثابت من القائمة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val currentSingleItem = contentItems.find { it.id == config.singleContentId }
                    Text(
                        text = currentSingleItem?.title?.ifEmpty { currentSingleItem.body } ?: "لم يتم اختيار نص بعد",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        contentItems.forEach { item ->
                            val isSelected = config.singleContentId == item.id
                            ListItem(
                                headlineContent = { Text(item.title.ifEmpty { item.body.take(30) + "..." }, fontSize = 12.sp) },
                                leadingContent = {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onUpdateConfig(config.copy(singleContentId = item.id)) }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypographyTab(
    config: WidgetConfigEntity,
    customFonts: List<CustomFontEntity>,
    onPickCustomFont: () -> Unit,
    onDeleteCustomFont: (CustomFontEntity) -> Unit,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // 1. Title Styling Section (تنسيق العنوان بشكل مستقل)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Title, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنسيق العنوان (Title)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    FilterChip(
                        selected = config.showTitle,
                        onClick = { onUpdateConfig(config.copy(showTitle = !config.showTitle)) },
                        label = { Text(if (config.showTitle) "العنوان مفعّل" else "العنوان مخفي", fontSize = 10.sp) }
                    )
                }

                if (config.showTitle) {
                    // Title Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حجم خط العنوان:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text("${config.titleFontSize} sp", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Slider(
                        value = config.titleFontSize.toFloat(),
                        onValueChange = { onUpdateConfig(config.copy(titleFontSize = it.toInt())) },
                        valueRange = 10f..30f,
                        steps = 20
                    )

                    // Title Alignment
                    Text("محاذاة العنوان:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = config.titleAlignment == "RIGHT",
                            onClick = { onUpdateConfig(config.copy(titleAlignment = "RIGHT")) },
                            leadingIcon = { Icon(Icons.Default.FormatAlignRight, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("يمين") }
                        )
                        FilterChip(
                            selected = config.titleAlignment == "CENTER",
                            onClick = { onUpdateConfig(config.copy(titleAlignment = "CENTER")) },
                            leadingIcon = { Icon(Icons.Default.FormatAlignCenter, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("وسط") }
                        )
                        FilterChip(
                            selected = config.titleAlignment == "LEFT",
                            onClick = { onUpdateConfig(config.copy(titleAlignment = "LEFT")) },
                            leadingIcon = { Icon(Icons.Default.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("يسار") }
                        )
                    }

                    // Title Weight
                    Text("سُمك خط العنوان:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = config.titleFontWeight == "BOLD",
                            onClick = { onUpdateConfig(config.copy(titleFontWeight = "BOLD")) },
                            label = { Text("عريض (Bold)") }
                        )
                        FilterChip(
                            selected = config.titleFontWeight == "NORMAL",
                            onClick = { onUpdateConfig(config.copy(titleFontWeight = "NORMAL")) },
                            label = { Text("عادي (Normal)") }
                        )
                    }

                    // Title Color
                    ColorPickerRow(
                        label = "لون العنوان",
                        selectedColorHex = config.titleColorHex,
                        onColorSelected = { onUpdateConfig(config.copy(titleColorHex = it)) }
                    )
                }
            }
        }

        // 2. Body Content Styling Section (تنسيق نص الدعاء والمحتوى بشكل مستقل)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنسيق نص الدعاء والذكر (Body Content)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Body Font Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("حجم خط النص الأساسي:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("${config.fontSize} sp", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Slider(
                    value = config.fontSize.toFloat(),
                    onValueChange = { onUpdateConfig(config.copy(fontSize = it.toInt())) },
                    valueRange = 10f..32f,
                    steps = 22
                )

                // Body Alignment
                Text("محاذاة نص الدعاء:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.textAlignment == "RIGHT",
                        onClick = { onUpdateConfig(config.copy(textAlignment = "RIGHT")) },
                        leadingIcon = { Icon(Icons.Default.FormatAlignRight, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("يمين (RTL)") }
                    )
                    FilterChip(
                        selected = config.textAlignment == "CENTER",
                        onClick = { onUpdateConfig(config.copy(textAlignment = "CENTER")) },
                        leadingIcon = { Icon(Icons.Default.FormatAlignCenter, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("وسط") }
                    )
                    FilterChip(
                        selected = config.textAlignment == "LEFT",
                        onClick = { onUpdateConfig(config.copy(textAlignment = "LEFT")) },
                        leadingIcon = { Icon(Icons.Default.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("يسار") }
                    )
                }

                // Body Color
                ColorPickerRow(
                    label = "لون النص الأساسي",
                    selectedColorHex = config.textColorHex,
                    onColorSelected = { onUpdateConfig(config.copy(textColorHex = it)) }
                )

                // Body Formatting Chips
                Text("تأثيرات الخط:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.fontWeight == "BOLD",
                        onClick = {
                            val newWeight = if (config.fontWeight == "BOLD") "NORMAL" else "BOLD"
                            onUpdateConfig(config.copy(fontWeight = newWeight))
                        },
                        label = { Text("عريض Bold") }
                    )
                    FilterChip(
                        selected = config.isItalic,
                        onClick = { onUpdateConfig(config.copy(isItalic = !config.isItalic)) },
                        label = { Text("مائل Italic") }
                    )
                    FilterChip(
                        selected = config.isUnderline,
                        onClick = { onUpdateConfig(config.copy(isUnderline = !config.isUnderline)) },
                        label = { Text("سطر سفلي") }
                    )
                }

                HorizontalDivider()

                // Line Spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تباعد الأسطر:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("${String.format(java.util.Locale.US, "%.1f", config.lineSpacing)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = config.lineSpacing,
                    onValueChange = { onUpdateConfig(config.copy(lineSpacing = it)) },
                    valueRange = 0.9f..2.5f,
                    steps = 15
                )

                // Letter Spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تباعد الحروف:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("${String.format(java.util.Locale.US, "%.1f", config.letterSpacing)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = config.letterSpacing,
                    onValueChange = { onUpdateConfig(config.copy(letterSpacing = it)) },
                    valueRange = -1.0f..3.0f,
                    steps = 8
                )
            }
        }

        // 3. Font Family Section (نوع الخط والخطوط المستوردة)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FontDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نوع الخط (Font Family)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Built-in Arabic fonts
                Text("الخطوط المدمجة الأساسية:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                val fontFamilies = listOf(
                    "TAJAWAL" to "تجول (Tajawal)",
                    "CAIRO" to "القاهرة (Cairo)",
                    "AMIRI" to "الأميري (Amiri)",
                    "NOTO_KUFI" to "كوفي (Noto Kufi)",
                    "DEFAULT" to "الافتراضي"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    fontFamilies.forEach { (key, label) ->
                        FilterChip(
                            selected = config.fontFamily == key && config.customFontPath.isNullOrBlank(),
                            onClick = { onUpdateConfig(config.copy(fontFamily = key, customFontPath = null)) },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Custom Imported Fonts Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الخطوط المخصصة من جهازك:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("استيراد أي خط بصيغة TTF أو OTF", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = onPickCustomFont,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة خط من الهاتف", fontSize = 11.sp)
                    }
                }

                if (customFonts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        customFonts.forEach { font ->
                            val isSelected = config.customFontPath == font.filePath
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                onUpdateConfig(config.copy(fontFamily = "CUSTOM", customFontPath = font.filePath))
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(font.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(font.fileName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (isSelected) {
                                                onUpdateConfig(config.copy(fontFamily = "TAJAWAL", customFontPath = null))
                                            }
                                            onDeleteCustomFont(font)
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الخط", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "لم تقم باستيراد خطوط من الجهاز بعد. اضغط على 'إضافة خط من الهاتف' لاختيار أي ملف خط .ttf أو .otf من جهازك.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorsTab(
    config: WidgetConfigEntity,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ColorPickerRow(
                label = "لون خلفية الودجت",
                selectedColorHex = config.backgroundColorHex,
                onColorSelected = { onUpdateConfig(config.copy(backgroundColorHex = it, gradientStartColorHex = it, gradientEndColorHex = it)) }
            )

            // Transparency / Opacity Section
            Text("شفافية الخلفية (الدرجة: ${(config.backgroundOpacity * 100).toInt()}%)", fontWeight = FontWeight.Bold)
            val opacityPresets = listOf(
                0.0f to "شفاف (0%)",
                0.3f to "خفيف (30%)",
                0.6f to "متوسط (60%)",
                0.85f to "داكن (85%)",
                1.0f to "معتم (100%)"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                opacityPresets.forEach { (alphaVal, label) ->
                    FilterChip(
                        selected = kotlin.math.abs(config.backgroundOpacity - alphaVal) < 0.05f,
                        onClick = { onUpdateConfig(config.copy(backgroundOpacity = alphaVal)) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
            Slider(
                value = config.backgroundOpacity,
                onValueChange = { onUpdateConfig(config.copy(backgroundOpacity = it)) },
                valueRange = 0f..1f
            )

            HorizontalDivider()

            ColorPickerRow(
                label = "لون النص الرئيسي (Body)",
                selectedColorHex = config.textColorHex,
                onColorSelected = { onUpdateConfig(config.copy(textColorHex = it)) }
            )

            HorizontalDivider()

            ColorPickerRow(
                label = "لون عنوان الودجت (Title)",
                selectedColorHex = config.titleColorHex,
                onColorSelected = { onUpdateConfig(config.copy(titleColorHex = it)) }
            )

            HorizontalDivider()

            Text("نمط التدرج (Gradient)", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.gradientDirection == "NONE",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "NONE")) },
                    label = { Text("بدون تدرج") }
                )
                FilterChip(
                    selected = config.gradientDirection == "TOP_BOTTOM",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "TOP_BOTTOM")) },
                    label = { Text("من الأعلى للأسفل") }
                )
                FilterChip(
                    selected = config.gradientDirection == "LEFT_RIGHT",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "LEFT_RIGHT")) },
                    label = { Text("من اليمين لليسار") }
                )
            }

            if (config.gradientDirection != "NONE") {
                ColorPickerRow(
                    label = "لون التدرج النهائي",
                    selectedColorHex = config.gradientEndColorHex ?: "#0F172A",
                    onColorSelected = { onUpdateConfig(config.copy(gradientEndColorHex = it)) }
                )
            }
        }
    }
}

@Composable
private fun LayoutAndBordersTab(
    config: WidgetConfigEntity,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Widget Lock Feature
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (config.isLocked)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (config.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (config.isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.isLocked) "الودجت مقفول (Locked)" else "قفل الويدجت من الفتح",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (config.isLocked)
                                "مفعّل: عند الضغط على الودجت في الشاشة الرئيسية لن يتم فتح إعدادات التصميم أو التطبيق."
                            else
                                "عند التفعيل، لن يؤدي الضغط على الودجت في الشاشة الرئيسية لفتح التطبيق أو إعدادات التصميم.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config.isLocked,
                        onCheckedChange = { onUpdateConfig(config.copy(isLocked = it)) }
                    )
                }
            }

            HorizontalDivider()

            Text("انحناء الزوايا (Corner Radius: ${config.cornerRadius} dp)", fontWeight = FontWeight.Bold)
            Slider(
                value = config.cornerRadius.toFloat(),
                onValueChange = { onUpdateConfig(config.copy(cornerRadius = it.toInt())) },
                valueRange = 0f..32f
            )

            Text("الهامش الداخلي (Padding: ${config.padding} dp)", fontWeight = FontWeight.Bold)
            Slider(
                value = config.padding.toFloat(),
                onValueChange = { onUpdateConfig(config.copy(padding = it.toInt())) },
                valueRange = 4f..24f
            )

            ColorPickerRow(
                label = "لون إطار الودجت",
                selectedColorHex = config.borderColorHex,
                onColorSelected = { onUpdateConfig(config.copy(borderColorHex = it)) }
            )

            Text("عناصر العرض في الودجت", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.showTitle,
                    onClick = { onUpdateConfig(config.copy(showTitle = !config.showTitle)) },
                    label = { Text("إظهار العنوان") }
                )
                FilterChip(
                    selected = config.showCategory,
                    onClick = { onUpdateConfig(config.copy(showCategory = !config.showCategory)) },
                    label = { Text("إظهار التصنيف") }
                )
                FilterChip(
                    selected = config.showDate,
                    onClick = { onUpdateConfig(config.copy(showDate = !config.showDate)) },
                    label = { Text("إظهار التاريخ") }
                )
            }
        }
    }
}

@Composable
private fun RotationAndUpdatesTab(
    config: WidgetConfigEntity,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("طريقة تناوب وتغيير المحتوى", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.rotationMode == "MANUAL",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "MANUAL")) },
                    label = { Text("يدوي (عند الضغط ⟳)") }
                )
                FilterChip(
                    selected = config.rotationMode == "SEQUENTIAL",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "SEQUENTIAL")) },
                    label = { Text("تتابعي تلقائي") }
                )
                FilterChip(
                    selected = config.rotationMode == "RANDOM",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "RANDOM")) },
                    label = { Text("عشوائي") }
                )
            }

            if (config.rotationMode != "MANUAL") {
                Text("فترة التحديث التلقائي", fontWeight = FontWeight.Bold)
                val intervals = listOf(
                    15 to "15 دقيقة",
                    30 to "30 دقيقة",
                    60 to "ساعة",
                    180 to "3 ساعات",
                    1440 to "يومياً"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    intervals.forEach { (mins, label) ->
                        FilterChip(
                            selected = config.rotationIntervalMinutes == mins,
                            onClick = { onUpdateConfig(config.copy(rotationIntervalMinutes = mins)) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}
