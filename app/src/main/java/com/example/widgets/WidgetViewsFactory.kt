package com.example.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import com.example.data.repository.WidgetRepository
import kotlinx.coroutines.runBlocking

class WidgetViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private val repository = WidgetRepository(context)
    private var config: WidgetConfigEntity? = null
    private var items: List<ContentItemEntity> = emptyList()

    override fun onCreate() {
        // Initial setup
    }

    override fun onDataSetChanged() {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        
        runBlocking {
            try {
                repository.seedDatabaseIfEmpty()
                config = repository.getWidgetConfigById(appWidgetId)
                val currentConfig = config
                if (currentConfig != null) {
                    val rawItems = repository.getItemsForWidgetConfig(currentConfig)
                    items = when (currentConfig.rotationMode) {
                        "RANDOM" -> rawItems.shuffled()
                        else -> rawItems
                    }
                } else {
                    // Default fallback if config not found
                    items = repository.getItemsForWidgetConfig(
                        WidgetConfigEntity(appWidgetId = appWidgetId)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                items = emptyList()
            }
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= items.size) {
            return RemoteViews(context.packageName, R.layout.widget_list_item)
        }

        val item = items[position]
        val currentConfig = config ?: WidgetConfigEntity(appWidgetId = appWidgetId)

        val views = RemoteViews(context.packageName, R.layout.widget_list_item)

        // Title
        if (item.title.isNotBlank() && currentConfig.showTitle) {
            views.setViewVisibility(R.id.item_title, android.view.View.VISIBLE)
            views.setTextViewText(R.id.item_title, item.title)
            try {
                views.setTextColor(R.id.item_title, Color.parseColor(currentConfig.titleColorHex))
            } catch (e: Exception) {
                views.setTextColor(R.id.item_title, Color.parseColor("#F59E0B"))
            }
        } else {
            views.setViewVisibility(R.id.item_title, android.view.View.GONE)
        }

        // Body Text
        val bodyText = item.body
        val spannable = SpannableString(bodyText)

        if (currentConfig.isItalic && currentConfig.fontWeight == "BOLD") {
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, bodyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else if (currentConfig.fontWeight == "BOLD") {
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, bodyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else if (currentConfig.isItalic) {
            spannable.setSpan(StyleSpan(android.graphics.Typeface.ITALIC), 0, bodyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (currentConfig.isUnderline) {
            spannable.setSpan(UnderlineSpan(), 0, bodyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        views.setTextViewText(R.id.item_body, spannable)

        try {
            views.setTextColor(R.id.item_body, Color.parseColor(currentConfig.textColorHex))
        } catch (e: Exception) {
            views.setTextColor(R.id.item_body, Color.WHITE)
        }

        // Set font size safely on supported Android versions (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                views.setTextViewTextSize(R.id.item_body, android.util.TypedValue.COMPLEX_UNIT_SP, currentConfig.fontSize.toFloat())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // Set fill intent for item click
        val fillInIntent = Intent().apply {
            putExtra(WidgetManagerHelper.EXTRA_WIDGET_ID, appWidgetId)
            putExtra("content_id", item.id)
        }
        views.setOnClickFillInIntent(R.id.item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
