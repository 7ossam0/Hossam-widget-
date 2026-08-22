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
import androidx.compose.material.icons.filled.Folder
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
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.AppCustomFontFamily
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
                title = {
                    Text(
                        "إدارة التصنيفات",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingCategory = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0D1117)) },
                text = { Text("إضافة تصنيف جديد", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFF0D1117)) },
                containerColor = Color(0xFF00E5FF),
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val itemCount = contentItems.count { it.categoryId == cat.id }
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (e: Exception) {
                    Color(0xFF00E5FF)
                }

                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFF161B22)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = catColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, catColor),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = AppCustomFontFamily,
                                    color = Color(0xFFF0F6FC)
                                )
                                Text(
                                    text = "$itemCount ذكر في هذا التصنيف",
                                    fontSize = 11.sp,
                                    fontFamily = AppCustomFontFamily,
                                    color = Color(0xFF8B949E)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                editingCategory = cat
                                showAddDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFF85149), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var catName by remember { mutableStateOf(editingCategory?.name ?: "") }
        var selectedColor by remember { mutableStateOf(editingCategory?.colorHex ?: "#00E5FF") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    if (editingCategory == null) "إضافة تصنيف جديد" else "تعديل التصنيف",
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppCustomFontFamily,
                    color = Color(0xFFF0F6FC)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("اسم التصنيف (مثل: أدعية من القرآن)", fontFamily = AppCustomFontFamily) },
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

                    ColorPickerRow(
                        label = "لون التصنيف المميز",
                        selectedColorHex = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            if (editingCategory == null) {
                                viewModel.addCategory(name = catName, colorHex = selectedColor)
                            } else {
                                viewModel.updateCategory(editingCategory!!.copy(name = catName, colorHex = selectedColor))
                            }
                            showAddDialog = false
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
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }
}
