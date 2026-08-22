package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.AppCustomFontFamily
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
                    Toast.makeText(context, "تمت استعادة البيانات بنجاح من الملف 🎉", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "الملف غير صالح أو تعذر الاستيراد", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "النسخ الاحتياطي والاستعادة",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Export to Phone File
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF161B22),
                glowColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير نسخة احتياطية كاملة", fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    }

                    Text(
                        "حفظ جميع الأذكار والتصنيفات وإعدادات الودجات في ملف JSON على وحدة تخزين هاتفك.",
                        fontSize = 12.sp,
                        fontFamily = AppCustomFontFamily,
                        color = Color(0xFF8B949E)
                    )

                    Button(
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            exportFileLauncher.launch("athkar_backup_$timeStamp.json")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0D1117)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ ملف النسخة على الهاتف", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                jsonExportString = viewModel.exportDataJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Athkar Backup JSON", jsonExportString)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ نص النسخة الاحتياطية إلى الحافظة 📋", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ البيانات كنص (JSON Clipboard)", fontFamily = AppCustomFontFamily, color = Color(0xFF00E5FF))
                    }
                }
            }

            // Card 2: Import / Restore
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF161B22),
                glowColor = Color(0xFFA371F7).copy(alpha = 0.25f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFFA371F7), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استعادة النسخة الاحتياطية", fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC))
                    }

                    Text(
                        "استرجاع الأذكار والودجات من ملف محفوظ مسبقاً أو عبر لصق نص الـ JSON مباشرة.",
                        fontSize = 12.sp,
                        fontFamily = AppCustomFontFamily,
                        color = Color(0xFF8B949E)
                    )

                    Button(
                        onClick = {
                            importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA371F7),
                            contentColor = Color(0xFF0D1117)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اختيار ملف نسخة احتياطية من الهاتف", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                    }

                    OutlinedButton(
                        onClick = { showManualImportDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFA371F7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("لصق نص النسخة يدوياً واستعادتها", fontFamily = AppCustomFontFamily, color = Color(0xFFA371F7))
                    }
                }
            }

            // Card 3: Reset / Clear Database
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF161B22)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إعادة تعيين التطبيق والبيانات", fontWeight = FontWeight.Bold, color = Color(0xFFF85149), fontFamily = AppCustomFontFamily)
                    Text(
                        "استرجاع الأذكار النموذجية الافتراضية وحذف البيانات المخصصة (يمكنك أخذ نسخة احتياطية أولاً).",
                        fontSize = 12.sp,
                        fontFamily = AppCustomFontFamily,
                        color = Color(0xFF8B949E)
                    )
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF85149)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعادة الأذكار والتصنيفات الافتراضية", fontFamily = AppCustomFontFamily)
                    }
                }
            }
        }
    }

    if (showManualImportDialog) {
        AlertDialog(
            onDismissRequest = { showManualImportDialog = false },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(24.dp),
            title = { Text("لصق بيانات الـ JSON", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC)) },
            text = {
                OutlinedTextField(
                    value = jsonImportString,
                    onValueChange = { jsonImportString = it },
                    placeholder = { Text("الصق كود النسخة الاحتياطية هنا...", fontFamily = AppCustomFontFamily) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0D1117),
                        unfocusedContainerColor = Color(0xFF0D1117),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF30363D)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.importDataJson(jsonImportString)
                            if (success) {
                                Toast.makeText(context, "تمت الاستعادة بنجاح 🎉", Toast.LENGTH_LONG).show()
                                showManualImportDialog = false
                            } else {
                                Toast.makeText(context, "النص غير صالح كنسخة احتياطية", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0D1117)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("استعادة البيانات", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualImportDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = Color(0xFF161B22),
            shape = RoundedCornerShape(24.dp),
            title = { Text("تأكيد استعادة الإعدادات الافتراضية؟", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily, color = Color(0xFFF0F6FC)) },
            text = { Text("سيتم مسح الأذكار الحالية وإعادة تعيين الأذكار النموذجية الافتراضية.", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDataToDefaults()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "تمت إعادة تعيين البيانات النموذجية بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF85149),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("تأكيد الاستعادة", fontWeight = FontWeight.Bold, fontFamily = AppCustomFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("إلغاء", fontFamily = AppCustomFontFamily, color = Color(0xFF8B949E)) }
            }
        )
    }
}
