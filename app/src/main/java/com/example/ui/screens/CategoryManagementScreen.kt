package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.ui.components.ColorPickerRow
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val contentItems by viewModel.contentItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة التصنيفات", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingCategory = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة تصنيف جديد") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "التصنيفات المتاحة لتنظيم الأدعية والأذكار والاقتباسات:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    val itemCount = contentItems.count { it.categoryId == cat.id }
                    val catColor = remember(cat.colorHex) {
                        try { Color(android.graphics.Color.parseColor(cat.colorHex)) } catch (e: Exception) { Color.Blue }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("يحتوي على $itemCount نصاً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    editingCategory = cat
                                    showAddDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf(editingCategory?.name ?: "") }
        var colorHex by remember { mutableStateOf(editingCategory?.colorHex ?: "#3B82F6") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingCategory == null) "إضافة تصنيف جديد" else "تعديل التصنيف") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم التصنيف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ColorPickerRow(
                        label = "لون المميز للتصنيف",
                        selectedColorHex = colorHex,
                        onColorSelected = { colorHex = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (editingCategory != null) {
                                viewModel.updateCategory(editingCategory!!.copy(name = name, colorHex = colorHex))
                            } else {
                                viewModel.addCategory(name, colorHex)
                            }
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
