package com.example.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.WidgetConfigEntity
import com.example.data.repository.WidgetRepository
import com.example.ui.theme.WidgetStudioTheme
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val intentExtras = intent.extras
        if (intentExtras != null) {
            appWidgetId = intentExtras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        val repository = WidgetRepository(applicationContext)

        setContent {
            WidgetStudioTheme {
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    repository.seedDatabaseIfEmpty()
                    var existing = repository.getWidgetConfigById(appWidgetId)
                    if (existing == null) {
                        existing = WidgetConfigEntity(
                            appWidgetId = appWidgetId,
                            name = "ودجت الشاشة $appWidgetId"
                        )
                        repository.insertWidgetConfig(existing)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "تأكيد إضافة الودجت",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "سيتم إنشاء الودجت على الشاشة الرئيسية مباشرة. يمكنك تخصيصه لاحقاً من داخل التطبيق.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        WidgetManagerHelper.updateSingleWidget(applicationContext, appWidgetId)
                                        val resultValue = Intent().apply {
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                        }
                                        setResult(Activity.RESULT_OK, resultValue)
                                        finish()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إضافة الودجت الآن")
                            }
                        }
                    }
                }
            }
        }
    }
}
