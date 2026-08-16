package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.ui.components.RichTextEditorToolbar
import com.example.ui.components.RichTextHelper
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentManagementScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val contentItems by viewModel.contentItems.collectAsState()
    val selectedCatId by viewModel.selectedCategoryIdFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ContentItemEntity?>(null) }

    val filteredItems = remember(contentItems, selectedCatId, searchQuery, showFavoritesOnly) {
        contentItems.filter { item ->
            val matchCat = selectedCatId == null || item.categoryId == selectedCatId
            val matchSearch = searchQuery.isBlank() || item.title.contains(searchQuery, true) || item.body.contains(searchQuery, true)
            val matchFav = !showFavoritesOnly || item.isFavorite
            matchCat && matchSearch && matchFav
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("محتوى الودجت والأذكار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (showFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showBulkImportDialog = true }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "استيراد نصوص متعددة", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingItem = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة نص جديد") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن أذكار، دعاء، أو نص...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "مسح") } }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("الكل (${contentItems.size})") }
                    )
                }
                items(categories) { cat ->
                    val count = contentItems.count { it.categoryId == cat.id }
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { viewModel.setCategoryFilter(cat.id) },
                        label = { Text("${cat.name} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد نصوص مطابقة للبحث", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val cat = categories.find { it.id == item.categoryId }
                        val catName = cat?.name ?: "بدون تصنيف"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isActive) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = catName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (item.repeatCount > 1) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "تكرار: ${item.repeatCount} مرات",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                                            Icon(
                                                if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "المفضلة",
                                                tint = if (item.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(onClick = {
                                            editingItem = item
                                            showAddEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        IconButton(onClick = { viewModel.deleteContentItem(item) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                if (item.title.isNotBlank()) {
                                    Text(
                                        text = RichTextHelper.htmlToAnnotatedString(item.title),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = RichTextHelper.htmlToAnnotatedString(item.body),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("حالة العرض بالودجت: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Switch(
                                            checked = item.isActive,
                                            onCheckedChange = { viewModel.toggleActive(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditContentDialog(
            item = editingItem,
            categories = categories,
            selectedDefaultCategoryId = selectedCatId,
            onDismiss = { showAddEditDialog = false },
            onSave = { title, body, catId, isFav, repCount ->
                if (editingItem != null) {
                    viewModel.updateContentItem(
                        editingItem!!.copy(
                            title = title,
                            body = body,
                            categoryId = catId,
                            isFavorite = isFav,
                            repeatCount = repCount
                        )
                    )
                } else {
                    viewModel.addContentItem(title, body, catId, isFav, repCount)
                }
                showAddEditDialog = false
            }
        )
    }

    if (showBulkImportDialog) {
        BulkImportDialog(
            categories = categories,
            selectedDefaultCategoryId = selectedCatId,
            onDismiss = { showBulkImportDialog = false },
            onImport = { itemsList, catId ->
                viewModel.addBulkContentItems(itemsList, catId)
                showBulkImportDialog = false
            }
        )
    }
}

@Composable
private fun AddEditContentDialog(
    item: ContentItemEntity?,
    categories: List<CategoryEntity>,
    selectedDefaultCategoryId: Long?,
    onDismiss: () -> Unit,
    onSave: (title: String, body: String, categoryId: Long?, isFavorite: Boolean, repeatCount: Int) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var bodyFieldValue by remember { mutableStateOf(TextFieldValue(item?.body ?: "")) }
    var categoryId by remember { mutableStateOf(item?.categoryId ?: selectedDefaultCategoryId) }
    var isFavorite by remember { mutableStateOf(item?.isFavorite ?: false) }
    var repeatCount by remember { mutableIntStateOf(item?.repeatCount ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (item == null) "إضافة وتنسيق نص جديد" else "تعديل وتنسيق النص",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "أدوات التنسيق المتقدمة (Word Style):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Interactive Rich Text Formatting Toolbar
                RichTextEditorToolbar(
                    textFieldValue = bodyFieldValue,
                    onValueChange = { bodyFieldValue = it }
                )

                OutlinedTextField(
                    value = bodyFieldValue,
                    onValueChange = { bodyFieldValue = it },
                    label = { Text("محتوى النص أو الدعاء *") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Formatted Text Preview
                if (bodyFieldValue.text.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "معاينة التنسيق والألوان المطبقة:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = RichTextHelper.htmlToAnnotatedString(bodyFieldValue.text),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Dua Repetitions Count
                Text("عدد مرات التكرار:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                val repeatPresets = listOf(1, 3, 7, 33, 100)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeatPresets.forEach { count ->
                        FilterChip(
                            selected = repeatCount == count,
                            onClick = { repeatCount = count },
                            label = { Text("$count مرة", fontSize = 11.sp) }
                        )
                    }
                }

                Text("التصنيف:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = categoryId == null,
                            onClick = { categoryId = null },
                            label = { Text("بدون تصنيف") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                    Text("إضافة إلى المفضلة", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bodyFieldValue.text.isNotBlank()) {
                        onSave(title, bodyFieldValue.text, categoryId, isFavorite, repeatCount)
                    }
                },
                enabled = bodyFieldValue.text.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun BulkImportDialog(
    categories: List<CategoryEntity>,
    selectedDefaultCategoryId: Long?,
    onDismiss: () -> Unit,
    onImport: (itemsList: List<Pair<String, String>>, categoryId: Long?) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(selectedDefaultCategoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استيراد نصوص متعددة دفعة واحدة (Bulk Import)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "الصق مجموعة من الأدعية أو النصوص هنا مفصولة بسطر فارغ مزدوج أو كل نص بسطر، وسيقوم التطبيق بتقسيمها وإضافتها تلقائياً:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("مثال:\nاللهم إني أسألك علماً نافعاً\n\nاللهم اغفر لي ولوالدي\n\nسبحان الله وبحمده") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("التصنيف المخصص للنصوص:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = categoryId == null,
                            onClick = { categoryId = null },
                            label = { Text("بدون تصنيف") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lines = rawText.split(Regex("\n\n+|\r\n\r\n+|---"))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val parsedPairs = lines.map { content ->
                        val parts = content.lines().filter { it.isNotBlank() }
                        if (parts.size > 1 && parts.first().length < 35) {
                            parts.first() to parts.drop(1).joinToString("\n")
                        } else {
                            "" to content
                        }
                    }

                    if (parsedPairs.isNotEmpty()) {
                        onImport(parsedPairs, categoryId)
                    }
                },
                enabled = rawText.isNotBlank()
            ) {
                Text("إضافة الكل الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
