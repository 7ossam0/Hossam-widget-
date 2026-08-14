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
                    if (rawItems.isNotEmpty()) {
                        val totalCount = rawItems.size
                        val currentIndex = (currentConfig.currentContentIndex % totalCount + totalCount) % totalCount
                        items = when (currentConfig.rotationMode) {
                            "RANDOM" -> {
                                val random = java.util.Random(currentConfig.currentContentIndex.toLong() + System.currentTimeMillis() / 1000)
                                val shuffled = rawItems.toMutableList()
                                java.util.Collections.shuffle(shuffled, random)
                                shuffled
                            }
                            else -> {
                                rawItems.drop(currentIndex) + rawItems.take(currentIndex)
                            }
                        }
                    } else {
                        items = emptyList()
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

        // Check if we should render via high-fidelity Bitmap Renderer (Custom Font or specific Typography)
        val useCustomRendering = !currentConfig.customFontPath.isNullOrBlank() || currentConfig.fontFamily != "DEFAULT"

        if (useCustomRendering) {
            try {
                val displayMetrics = context.resources.displayMetrics
                val targetWidth = (displayMetrics.widthPixels - (48 * displayMetrics.density).toInt()).coerceIn(320, 600)
                val renderedBitmap = WidgetTextRenderer.renderContentItem(
                    context = context,
                    title = if (currentConfig.showTitle && item.title.isNotBlank()) item.title else null,
                    body = item.body,
                    config = currentConfig,
                    targetWidthPx = targetWidth
                )

                views.setImageViewBitmap(R.id.item_rendered_image, renderedBitmap)
                views.setViewVisibility(R.id.item_rendered_image, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.item_title, android.view.View.GONE)
                views.setViewVisibility(R.id.item_body, android.view.View.GONE)
            } catch (e: Throwable) {
                e.printStackTrace()
                fallbackStandardTextView(views, item, currentConfig)
            }
        } else {
            fallbackStandardTextView(views, item, currentConfig)
        }

        // Set fill intent for item click
        val fillInIntent = Intent().apply {
            putExtra(WidgetManagerHelper.EXTRA_WIDGET_ID, appWidgetId)
            putExtra("content_id", item.id)
        }
        views.setOnClickFillInIntent(R.id.item_container, fillInIntent)

        return views
    }

    private fun fallbackStandardTextView(
        views: RemoteViews,
        item: ContentItemEntity,
        currentConfig: WidgetConfigEntity
    ) {
        views.setViewVisibility(R.id.item_rendered_image, android.view.View.GONE)

        // Calculate Gravity based on alignment & RTL direction
        val gravity = when (currentConfig.textAlignment) {
            "RIGHT" -> if (currentConfig.directionRtl) {
                android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            } else {
                android.view.Gravity.RIGHT or android.view.Gravity.CENTER_VERTICAL
            }
            "LEFT" -> if (currentConfig.directionRtl) {
                android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            } else {
                android.view.Gravity.LEFT or android.view.Gravity.CENTER_VERTICAL
            }
            else -> android.view.Gravity.CENTER
        }

        views.setInt(R.id.item_title, "setGravity", gravity)
        views.setInt(R.id.item_body, "setGravity", gravity)

        // Alignment Span
        val alignmentSpan = when (currentConfig.textAlignment) {
            "CENTER" -> android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_CENTER)
            "RIGHT" -> if (currentConfig.directionRtl) {
                android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_NORMAL)
            } else {
                android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_OPPOSITE)
            }
            "LEFT" -> if (currentConfig.directionRtl) {
                android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_OPPOSITE)
            } else {
                android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_NORMAL)
            }
            else -> android.text.style.AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_CENTER)
        }

        // Title
        if (item.title.isNotBlank() && currentConfig.showTitle) {
            views.setViewVisibility(R.id.item_title, android.view.View.VISIBLE)
            val titleSpannable = SpannableString(item.title)
            titleSpannable.setSpan(alignmentSpan, 0, item.title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            titleSpannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, item.title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            views.setTextViewText(R.id.item_title, titleSpannable)
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
        spannable.setSpan(alignmentSpan, 0, bodyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

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

        views.setViewVisibility(R.id.item_body, android.view.View.VISIBLE)
        views.setTextViewText(R.id.item_body, spannable)

        try {
            views.setTextColor(R.id.item_body, Color.parseColor(currentConfig.textColorHex))
        } catch (e: Exception) {
            views.setTextColor(R.id.item_body, Color.WHITE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                views.setTextViewTextSize(R.id.item_body, android.util.TypedValue.COMPLEX_UNIT_SP, currentConfig.fontSize.toFloat())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
