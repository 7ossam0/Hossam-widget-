package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var jsonExportString by remember { mutableStateOf("") }
    var jsonImportString by remember { mutableStateOf("") }
    var showManualImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // SAF Create Document Launcher (Save to File)
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val success = viewModel.exportToFileUri(uri)
                if (success) {
                    Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح في ملف على الهاتف 💾", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "فشل حفظ الملف", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // SAF Open Document Launcher (Restore from File)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val success = viewModel.importFromFileUri(uri)
                if (success) {
                    Toast.makeText(context, "تم استرجاع النسخة الاحتياطية بنجاح 📂", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "تعذر استيراد الملف", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("النسخ الاحتياطي والاستعادة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
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
            // Direct File Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ نسخة احتياطية في ملف (.json)", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "حفظ جميع الأدعية، الأذكار، النصوص، والتصاميم المخصصة كملف على ذاكرة الهاتف أو Google Drive لاسترجاعها في أي وقت دون الحاجة لنسخ أكواد يدوية.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportFileLauncher.launch("Athkar_Backup_$timestamp.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ ملف النسخة الاحتياطية على الهاتف")
                    }
                }
            }

            // Direct File Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استعادة نسخة احتياطية من ملف (.json)", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "اختر ملف النسخة الاحتياطية المحفوظ سابقاً بصيغة JSON من جهازك لاستعادة كافة البيانات فوراً.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختيار ملف واستعادته من الهاتف")
                    }
                }
            }

            // Secondary: Manual Text Copy / Paste
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("النسخ اليدوي كنص (اختياري)", fontWeight = FontWeight.Bold)
                    Text(
                        "يمكنك أيضاً إنشاء كود نصي مباشر لنسخه إلى الحافظة أو لصقه يدوياً.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    jsonExportString = viewModel.exportDataJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Backup JSON", jsonExportString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ كود JSON للحافظة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ كود JSON", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showManualImportDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("لصق كود JSON", fontSize = 11.sp)
                        }
                    }

                    if (jsonExportString.isNotEmpty()) {
                        OutlinedTextField(
                            value = jsonExportString,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("بيانات النسخة الاحتياطية") },
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                    Text("إعادة تعيين للبيانات الافتراضية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(
                        "استعادة البيانات والتصنيفات الأصلية (الأذكار والأدعية والقرآن والاقتباسات النموذجية).",
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

    if (showManualImportDialog) {
        AlertDialog(
            onDismissRequest = { showManualImportDialog = false },
            title = { Text("استيراد كود JSON يدوياً", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الصق نص النسخة الاحتياطية أدناه:")
                    OutlinedTextField(
                        value = jsonImportString,
                        onValueChange = { jsonImportString = it },
                        placeholder = { Text("{\"categories\": [...], \"contentItems\": [...]}") },
                        minLines = 5,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.importDataJson(jsonImportString)
                            if (success) {
                                showManualImportDialog = false
                                jsonImportString = ""
                            }
                        }
                    },
                    enabled = jsonImportString.isNotBlank()
                ) {
                    Text("استيراد الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualImportDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("تأكيد إعادة التعيين", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("هل أنت متأكد من رغبتك في مسح كافة النصوص الحالية واستعادة البيانات الافتراضية؟ لا يمكن التراجع عن هذا الإجراء إلا بوجود نسخة احتياطية.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDataToDefaults()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، إعادة تعيين")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
