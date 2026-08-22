package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.components.GranularRichTextEditorToolbar
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.RichTextHelper
import com.example.ui.theme.AppCustomFontFamily
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
                title = {
                    Text(
                        "إدارة الأذكار والمحتوى",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppCustomFontFamily,
                        color = Color(0xFFF0F6FC)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color(0xFFF0F6FC))
                    }
                },
                actions = {
                    IconButton(onClick = { showBulkImportDialog = true }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "استيراد نصوص متعددة", tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingItem = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0D1117)) },
                text = { Text("إضافة ذكر جديد", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFF0D1117)) },
                containerColor = Color(0xFF00E5FF),
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث في نصوص الأذكار أو العناوين...", fontSize = 12.sp, fontFamily = AppCustomFontFamily) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color(0xFF8B949E)) } }
                } else null,
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF161B22),
                    unfocusedContainerColor = Color(0xFF161B22),
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF30363D)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("الكل (${contentItems.size})", fontFamily = AppCustomFontFamily) }
                    )
                }
                items(categories) { cat ->
                    val count = contentItems.count { it.categoryId == cat.id }
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = { viewModel.setCategoryFilter(cat.id) },
                        label = { Text("${cat.name} ($count)", fontFamily = AppCustomFontFamily) }
                    )
                }
            }

            // List of Items
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "لا توجد أذكار مطابقة",
                        fontFamily = AppCustomFontFamily,
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val itemCat = categories.find { it.id == item.categoryId }
                        NeumorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0xFF161B22)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (itemCat != null) {
                                        Surface(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                itemCat.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = AppCustomFontFamily,
                                                color = Color(0xFF00E5FF),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row {
                                        IconButton(onClick = { viewModel.toggleFavorite(item) }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "مفضلة",
                                                tint = if (item.isFavorite) Color(0xFFF85149) else Color(0xFF8B949E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = {
                                            editingItem = item
                                            showAddEditDialog = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteContentItem(item) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFF85149), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                if (item.title.isNotBlank()) {
                                    Text(
                                        text = RichTextHelper.htmlToAnnotatedString(item.title, Color(0xFF00E5FF)),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = AppCustomFontFamily,
                                        color = Color(0xFF00E5FF)
                                    )
                                }

                                Text(
                                    text = RichTextHelper.htmlToAnnotatedString(item.body, Color(0xFFF0F6FC)),
                                    fontSize = 13.sp,
                                    fontFamily = AppCustomFontFamily,
                                    color = Color(0xFFF0F6FC),
                                    lineHeight = 20.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        var titleVal by remember { mutableStateOf(editingItem?.title ?: "") }
        var bodyVal by remember { mutableStateOf(TextFieldValue(editingItem?.body ?: "")) }
        var selectedCatIdForNew by remember { mutableStateOf(editingItem?.categoryId ?: categories.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    if (editingItem == null) "إضافة ذكر جديد" else "تعديل الذكر",
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = titleVal,
                        onValueChange = { titleVal = it },
                        label = { Text("العنوان (اختياري)", fontFamily = AppCustomFontFamily) },
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

                    Text("حدد التصنيف:", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCatIdForNew == cat.id,
                                onClick = { selectedCatIdForNew = cat.id },
                                label = { Text(cat.name, fontFamily = AppCustomFontFamily) }
                            )
                        }
                    }

                    Text("نص المحتوى (يدعم التحديد والتلوين):", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    OutlinedTextField(
                        value = bodyVal,
                        onValueChange = { bodyVal = it },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp)
                    )

                    GranularRichTextEditorToolbar(
                        textFieldValue = bodyVal,
                        onValueChange = { bodyVal = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bodyVal.text.isNotBlank()) {
                            if (editingItem == null) {
                                viewModel.addContentItem(
                                    title = titleVal,
                                    body = bodyVal.text,
                                    categoryId = selectedCatIdForNew
                                )
                            } else {
                                viewModel.updateContentItem(
                                    editingItem!!.copy(
                                        categoryId = selectedCatIdForNew,
                                        title = titleVal,
                                        body = bodyVal.text
                                    )
                                )
                            }
                            showAddEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0D1117)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("حفظ", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }

    // Bulk Import Dialog
    if (showBulkImportDialog) {
        var bulkText by remember { mutableStateOf("") }
        var bulkCatId by remember { mutableStateOf(categories.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showBulkImportDialog = false },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(24.dp),
            title = { Text("استيراد أذكار متعددة دفعة واحدة", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الصق النصوص هنا مفصولة بسطر فارغ بين كل ذكر والآخر:", fontSize = 11.sp, fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E))
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF30363D)
                        ),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )

                    Text("اختر التصنيف للإضافة إليه:", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = bulkCatId == cat.id,
                                onClick = { bulkCatId = cat.id },
                                label = { Text(cat.name, fontFamily = AppCustomFontFamily) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val items = bulkText.split("\n\n").filter { it.isNotBlank() }.map { "" to it.trim() }
                        viewModel.addBulkContentItems(items, bulkCatId)
                        showBulkImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0D1117)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("استيراد الكل", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkImportDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }
}
