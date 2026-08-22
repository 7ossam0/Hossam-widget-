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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.data.model.CustomFontEntity
import com.example.data.model.WidgetConfigEntity
import com.example.ui.components.*
import com.example.ui.theme.AppCustomFontFamily
import com.example.viewmodel.MainViewModel

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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("المحتوى والتحديد", "الخط والمقاس", "التحكم بالألوان", "الهيكل والزوايا", "التناوب والتحديث")

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("استوديو تصميم الودجت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                        Text("NEUMORPHIC LIVE DESIGNER", fontSize = 9.sp, letterSpacing = 1.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color(0xFFF0F6FC))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerWidgetRefreshBroadcast(config.appWidgetId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث المعاينة", tint = Color(0xFF00E5FF))
                    }
                    Button(
                        onClick = {
                            viewModel.saveWidgetConfig(config)
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0D1117)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ التعديلات", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(paddingValues)
        ) {
            // Floating Neumorphic Live Preview Pod
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(26.dp),
                glowColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المعاينة الحية للودجت بالخط المرفق الأساسي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFF8B949E)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = "LIVE FONT PREVIEW ⚡",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

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

            // Interactive Tab Navigation Row with Soft Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    NeumorphicPillButton(
                        text = title,
                        isSelected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
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
                    0 -> ContentSourceAndSelectionTab(
                        config = config,
                        categories = categories,
                        contentItems = contentItems,
                        previewItems = previewItems,
                        onUpdateItem = { viewModel.updateContentItem(it) },
                        onUpdateConfig = { config = it }
                    )
                    1 -> TypographyAndDialTab(
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

@Composable
private fun ContentSourceAndSelectionTab(
    config: WidgetConfigEntity,
    categories: List<CategoryEntity>,
    contentItems: List<ContentItemEntity>,
    previewItems: List<ContentItemEntity>,
    onUpdateItem: (ContentItemEntity) -> Unit,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
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
        // Selection & Granular Control of What Shows on Widget
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            glowColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التحكم بما يعرض على هذا الودجت", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    }
                }

                Text("حدد نمط عرض المحتوى للودجت:", fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeumorphicPillButton(
                        text = "تصنيف كامل",
                        isSelected = config.contentMode == "CATEGORY_ALL",
                        onClick = { onUpdateConfig(config.copy(contentMode = "CATEGORY_ALL")) }
                    )
                    NeumorphicPillButton(
                        text = "نص واحد محدد",
                        isSelected = config.contentMode == "SINGLE",
                        onClick = { onUpdateConfig(config.copy(contentMode = "SINGLE")) }
                    )
                }

                if (config.contentMode == "CATEGORY_ALL") {
                    Text("اختر التصنيف المخصص للودجت:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = config.categoryId == null,
                            onClick = { onUpdateConfig(config.copy(categoryId = null)) },
                            label = { Text("جميع التصنيفات", fontFamily = AppCustomFontFamily) }
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = config.categoryId == cat.id,
                                onClick = { onUpdateConfig(config.copy(categoryId = cat.id)) },
                                label = { Text(cat.name, fontFamily = AppCustomFontFamily) }
                            )
                        }
                    }
                }

                if (config.contentMode == "SINGLE") {
                    Text("اختر النص الثابت من القائمة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        contentItems.forEach { item ->
                            val isSelected = config.singleContentId == item.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF161B22)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onUpdateConfig(config.copy(singleContentId = item.id)) }
                                    )
                                    Text(
                                        text = item.title.ifEmpty { item.body.take(45) + "..." },
                                        fontSize = 12.sp,
                                        fontFamily = AppCustomFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFF0F6FC)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live In-Place Granular Text Designer Section
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            glowColor = Color(0xFFA371F7).copy(alpha = 0.2f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFA371F7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التحرير والتلوين الحر للنص المعروض", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    }
                }

                // In-place Item Editor Form
                selectedItemForEdit?.let { currentItem ->
                    var titleText by remember(currentItem.id) { mutableStateOf(currentItem.title) }
                    var bodyTextFieldValue by remember(currentItem.id) { mutableStateOf(TextFieldValue(currentItem.body)) }
                    var hasSavedFeedback by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { newTitle ->
                            titleText = newTitle
                            val updated = currentItem.copy(title = newTitle)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        },
                        label = { Text("عنوان النص الفرعي", fontFamily = AppCustomFontFamily) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("نص المحتوى (تحديد الكلمات لتلوينها وتنسيقها):", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    OutlinedTextField(
                        value = bodyTextFieldValue,
                        onValueChange = { newBodyVal ->
                            bodyTextFieldValue = newBodyVal
                            val updated = currentItem.copy(body = newBodyVal.text)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 180.dp)
                    )

                    GranularRichTextEditorToolbar(
                        textFieldValue = bodyTextFieldValue,
                        onValueChange = { newBodyVal ->
                            bodyTextFieldValue = newBodyVal
                            val updated = currentItem.copy(body = newBodyVal.text)
                            selectedItemForEdit = updated
                            onUpdateItem(updated)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasSavedFeedback) {
                            Text("✓ تم الحفظ بنجاح", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                val updated = currentItem.copy(title = titleText, body = bodyTextFieldValue.text)
                                onUpdateItem(updated)
                                hasSavedFeedback = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF0D1117)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ تعديل النص", fontSize = 12.sp, fontFamily = AppCustomFontFamily)
                        }
                    }
                } ?: run {
                    Text("لا يوجد نص محدد حالياً للتعديل.", fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E))
                }
            }
        }
    }
}

@Composable
private fun TypographyAndDialTab(
    config: WidgetConfigEntity,
    customFonts: List<CustomFontEntity>,
    onPickCustomFont: () -> Unit,
    onDeleteCustomFont: (CustomFontEntity) -> Unit,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Circular Dial Controller for Font Size
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            glowColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "التحكم الدائري في مقاس الخط (Smart Dial)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC)
                )
                Spacer(modifier = Modifier.height(10.dp))

                CircularDialSlider(
                    value = config.fontSize.toFloat(),
                    onValueChange = { onUpdateConfig(config.copy(fontSize = it.toInt())) },
                    valueRange = 10f..38f,
                    unit = "sp",
                    title = "حجم الخط",
                    subtitle = "اسحب لتكبير وتصغير الخط مباشرة"
                )
            }
        }

        // Font Family Selection
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FontDownload, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نوع الخط (Font Family)", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                }

                val fontFamilies = listOf(
                    "DEFAULT" to "الخط المرفق الأساسي (Default F5 / Tajawal)",
                    "CAIRO" to "القاهرة (Cairo)",
                    "AMIRI" to "الأميري (Amiri)",
                    "NOTO_KUFI" to "كوفي (Noto Kufi)"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fontFamilies.forEach { (key, label) ->
                        FilterChip(
                            selected = (config.fontFamily == key || (key == "DEFAULT" && config.fontFamily == "TAJAWAL")) && config.customFontPath.isNullOrBlank(),
                            onClick = { onUpdateConfig(config.copy(fontFamily = key, customFontPath = null)) },
                            label = { Text(label, fontSize = 11.sp, fontFamily = AppCustomFontFamily) }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF30363D))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الخطوط المخصصة من الهاتف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                        Text("استيراد خطوط TTF أو OTF مباشرة", fontSize = 10.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E))
                    }
                    FilledTonalButton(
                        onClick = onPickCustomFont,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF21262D)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة خط من الهاتف", fontSize = 11.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF00E5FF))
                    }
                }

                if (customFonts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        customFonts.forEach { font ->
                            val isSelected = config.customFontPath == font.filePath
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF161B22),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF30363D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onUpdateConfig(config.copy(fontFamily = "CUSTOM", customFontPath = font.filePath)) }
                                        )
                                        Column {
                                            Text(font.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                                            Text(font.fileName, fontSize = 10.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E))
                                        }
                                    }
                                    IconButton(onClick = { onDeleteCustomFont(font) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFF85149), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
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
    NeumorphicCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ColorPickerRow(
                label = "لون خلفية الودجت الأساسي",
                selectedColorHex = config.backgroundColorHex,
                onColorSelected = { onUpdateConfig(config.copy(backgroundColorHex = it, gradientStartColorHex = it, gradientEndColorHex = it)) }
            )

            // Transparency / Opacity Section
            Text("شفافية الخلفية (${(config.backgroundOpacity * 100).toInt()}%)", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
            Slider(
                value = config.backgroundOpacity,
                onValueChange = { onUpdateConfig(config.copy(backgroundOpacity = it)) },
                valueRange = 0f..1f
            )

            HorizontalDivider(color = Color(0xFF30363D))

            ColorPickerRow(
                label = "لون النص الرئيسي (Body Text)",
                selectedColorHex = config.textColorHex,
                onColorSelected = { onUpdateConfig(config.copy(textColorHex = it)) }
            )

            HorizontalDivider(color = Color(0xFF30363D))

            ColorPickerRow(
                label = "لون عنوان الودجت (Title Color)",
                selectedColorHex = config.titleColorHex,
                onColorSelected = { onUpdateConfig(config.copy(titleColorHex = it)) }
            )

            HorizontalDivider(color = Color(0xFF30363D))

            Text("نمط التدرج الضوئي (Gradient Style):", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.gradientDirection == "NONE",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "NONE")) },
                    label = { Text("بدون تدرج", fontFamily = AppCustomFontFamily) }
                )
                FilterChip(
                    selected = config.gradientDirection == "TOP_BOTTOM",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "TOP_BOTTOM")) },
                    label = { Text("رأسي متوهج", fontFamily = AppCustomFontFamily) }
                )
                FilterChip(
                    selected = config.gradientDirection == "LEFT_RIGHT",
                    onClick = { onUpdateConfig(config.copy(gradientDirection = "LEFT_RIGHT")) },
                    label = { Text("أفقي ناعم", fontFamily = AppCustomFontFamily) }
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
    NeumorphicCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Widget Lock Feature
            Surface(
                color = if (config.isLocked) Color(0xFF491C1C) else Color(0xFF161B22),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (config.isLocked) Color(0xFFF85149) else Color(0xFF30363D))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (config.isLocked) "الودجت مقفول (Locked) 🔒" else "قفل الويدجت من الفتح 🔓",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFFF0F6FC)
                        )
                        Text(
                            text = "منع فتح التطبيق أو إعدادات التصميم عند الضغط على الودجت في الشاشة الرئيسية.",
                            fontSize = 10.sp,
                            fontFamily = AppCustomFontFamily,
                            color = Color(0xFF8B949E)
                        )
                    }
                    Switch(
                        checked = config.isLocked,
                        onCheckedChange = { onUpdateConfig(config.copy(isLocked = it)) }
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF30363D))

            Text("انحناء الزوايا (Corner Radius: ${config.cornerRadius} dp)", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
            Slider(
                value = config.cornerRadius.toFloat(),
                onValueChange = { onUpdateConfig(config.copy(cornerRadius = it.toInt())) },
                valueRange = 0f..36f
            )

            Text("الهامش الداخلي (Padding: ${config.padding} dp)", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
            Slider(
                value = config.padding.toFloat(),
                onValueChange = { onUpdateConfig(config.copy(padding = it.toInt())) },
                valueRange = 4f..28f
            )

            ColorPickerRow(
                label = "لون إطار الودجت والتوهج",
                selectedColorHex = config.borderColorHex,
                onColorSelected = { onUpdateConfig(config.copy(borderColorHex = it)) }
            )
        }
    }
}

@Composable
private fun RotationAndUpdatesTab(
    config: WidgetConfigEntity,
    onUpdateConfig: (WidgetConfigEntity) -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("طريقة تناوب وتغيير المحتوى", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = config.rotationMode == "MANUAL",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "MANUAL")) },
                    label = { Text("يدوي (عند الضغط ⟳)", fontFamily = AppCustomFontFamily) }
                )
                FilterChip(
                    selected = config.rotationMode == "SEQUENTIAL",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "SEQUENTIAL")) },
                    label = { Text("تتابعي تلقائي", fontFamily = AppCustomFontFamily) }
                )
                FilterChip(
                    selected = config.rotationMode == "RANDOM",
                    onClick = { onUpdateConfig(config.copy(rotationMode = "RANDOM")) },
                    label = { Text("عشوائي", fontFamily = AppCustomFontFamily) }
                )
            }

            if (config.rotationMode != "MANUAL") {
                Text("فترة التحديث التلقائي:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
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
                            label = { Text(label, fontSize = 11.sp, fontFamily = AppCustomFontFamily) }
                        )
                    }
                }
            }
        }
    }
}
