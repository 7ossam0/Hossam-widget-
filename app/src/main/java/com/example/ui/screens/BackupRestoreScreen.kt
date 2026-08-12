package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var jsonExportString by remember { mutableStateOf("") }
    var jsonImportString by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("النسخ الاحتياطي والاستيراد", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تصدير النسخة الاحتياطية (Export JSON)", fontWeight = FontWeight.Bold)
                    Text(
                        "تستطيع تصدير جميع النصوص، التصنيفات، وإعدادات الودجت المصممة إلى نص JSON لاستعادتها لاحقاً أو نقلها لهاتف آخر.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                jsonExportString = viewModel.exportDataJson()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء ملف التصدير الآن")
                    }

                    if (jsonExportString.isNotEmpty()) {
                        OutlinedTextField(
                            value = jsonExportString,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("بيانات التصدير (JSON)") },
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("استيراد نسخة احتياطية (Import JSON)", fontWeight = FontWeight.Bold)
                    Text(
                        "استرجاع البيانات المحفوظة سابقاً من كود JSON.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد بيانات JSON")
                    }
                }
            }

            // Reset Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إعادة تعيين للبيانات التجريبية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(
                        "استعادة البيانات والتصنيفات الافتراضية (الأذكار والأدعية والقرآن والاقتباسات).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة تعيين البيانات الافتراضية")
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("استيراد نسخة JSON") },
            text = {
                OutlinedTextField(
                    value = jsonImportString,
                    onValueChange = { jsonImportString = it },
                    label = { Text("الصق كود الـ JSON هنا") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.importDataJson(jsonImportString)
                            showImportDialog = false
                        }
                    },
                    enabled = jsonImportString.isNotBlank()
                ) {
                    Text("استيراد الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("تأكيد إعادة التعيين") },
            text = { Text("هل أنت تأكد من إعادة تعيين كافة البيانات إلى المحتوى الافتراضي؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDataToDefaults()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
