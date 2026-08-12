package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.WidgetConfigEntity
import com.example.data.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WidgetStudioAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        val repository = WidgetRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.seedDatabaseIfEmpty()
                for (appWidgetId in appWidgetIds) {
                    var config = repository.getWidgetConfigById(appWidgetId)
                    if (config == null) {
                        config = WidgetConfigEntity(
                            appWidgetId = appWidgetId,
                            name = "ودجت الأذكار $appWidgetId"
                        )
                        repository.insertWidgetConfig(config)
                    }
                    updateWidgetViews(context, appWidgetManager, appWidgetId, config, repository)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (action == WidgetManagerHelper.ACTION_REFRESH_ALL || action == WidgetManagerHelper.ACTION_NEXT) {
            val pendingResult = goAsync()
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val repository = WidgetRepository(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        var config = repository.getWidgetConfigById(appWidgetId)
                        if (config != null) {
                            config = config.copy(currentContentIndex = config.currentContentIndex + 1)
                            repository.updateWidgetConfig(config)
                        }
                        updateWidgetViews(context, appWidgetManager, appWidgetId, config, repository)
                        withContext(Dispatchers.Main) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
                        }
                    } else {
                        val componentName = ComponentName(context, WidgetStudioAppWidgetProvider::class.java)
                        val ids = appWidgetManager.getAppWidgetIds(componentName)
                        for (id in ids) {
                            var cfg = repository.getWidgetConfigById(id)
                            if (cfg != null) {
                                cfg = cfg.copy(currentContentIndex = cfg.currentContentIndex + 1)
                                repository.updateWidgetConfig(cfg)
                                updateWidgetViews(context, appWidgetManager, id, cfg, repository)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val pendingResult = goAsync()
        val repository = WidgetRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (id in appWidgetIds) {
                    repository.deleteWidgetConfig(id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun updateWidgetViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            config: WidgetConfigEntity?,
            repository: WidgetRepository
        ) {
            val currentConfig = config ?: WidgetConfigEntity(appWidgetId = appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_scrollable_layout)

            // Apply Background Color & Transparency (Opacity)
            try {
                val parsedBgColor = Color.parseColor(currentConfig.backgroundColorHex)
                val alpha = (currentConfig.backgroundOpacity * 255).toInt().coerceIn(0, 255)
                val argbColor = Color.argb(
                    alpha,
                    Color.red(parsedBgColor),
                    Color.green(parsedBgColor),
                    Color.blue(parsedBgColor)
                )
                views.setInt(R.id.widget_container, "setBackgroundColor", argbColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Config Title
            if (currentConfig.showTitle) {
                views.setViewVisibility(R.id.widget_title, View.VISIBLE)
                views.setTextViewText(R.id.widget_title, currentConfig.name)
                try {
                    views.setTextColor(R.id.widget_title, Color.parseColor(currentConfig.titleColorHex))
                } catch (e: Exception) {
                    views.setTextColor(R.id.widget_title, Color.parseColor("#F59E0B"))
                }
            } else {
                views.setViewVisibility(R.id.widget_title, View.GONE)
            }

            // Category tag
            if (currentConfig.showCategory) {
                views.setViewVisibility(R.id.widget_category_tag, View.VISIBLE)
                val catName = if (currentConfig.categoryId != null) {
                    repository.getCategoryById(currentConfig.categoryId)?.name ?: "عام"
                } else {
                    "الكل"
                }
                views.setTextViewText(R.id.widget_category_tag, catName)
            } else {
                views.setViewVisibility(R.id.widget_category_tag, View.GONE)
            }

            // Date
            if (currentConfig.showDate) {
                views.setViewVisibility(R.id.widget_footer_date, View.VISIBLE)
                val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
                views.setTextViewText(R.id.widget_footer_date, dateFormat.format(Date()))
            } else {
                views.setViewVisibility(R.id.widget_footer_date, View.GONE)
            }

            // RemoteAdapter Intent for Scrollable ListView
            val serviceIntent = Intent(context, WidgetScrollService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

            // PendingIntent for Refresh button (⟳)
            val refreshIntent = Intent(context, WidgetStudioAppWidgetProvider::class.java).apply {
                action = WidgetManagerHelper.ACTION_NEXT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

            // PendingIntent for Next button
            val nextIntent = Intent(context, WidgetStudioAppWidgetProvider::class.java).apply {
                action = WidgetManagerHelper.ACTION_NEXT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 10000,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_next, nextPendingIntent)

            // PendingIntent for Header Click -> Opens App
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("appWidgetId", appWidgetId)
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header_bar, appPendingIntent)

            // Item template pending intent for clicking items
            views.setPendingIntentTemplate(R.id.widget_list_view, appPendingIntent)

            // Update Widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        }
    }
}
