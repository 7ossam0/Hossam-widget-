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

        val hasCustomFont = !currentConfig.customFontPath.isNullOrBlank()

        if (hasCustomFont) {
            // Render custom font file via StaticLayout Bitmap at EXACT widget width
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 260).coerceAtLeast(100)
                val density = context.resources.displayMetrics.density
                val paddingPx = (currentConfig.padding * 2 * density).toInt()
                val targetWidth = (minWidthDp * density - paddingPx).toInt().coerceIn(200, 1200)

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
                renderNativeTextViews(views, item, currentConfig)
            }
        } else {
            // Render crisp, native subpixel TextViews with full rich-text Spans
            renderNativeTextViews(views, item, currentConfig)
        }

        // Set fill intent for item click
        val fillInIntent = Intent().apply {
            putExtra(WidgetManagerHelper.EXTRA_WIDGET_ID, appWidgetId)
            putExtra("content_id", item.id)
        }
        views.setOnClickFillInIntent(R.id.item_container, fillInIntent)

        return views
    }

    private fun renderNativeTextViews(
        views: RemoteViews,
        item: ContentItemEntity,
        currentConfig: WidgetConfigEntity
    ) {
        views.setViewVisibility(R.id.item_rendered_image, android.view.View.GONE)

        // Calculate Gravity based on alignment & RTL direction
        val titleGravity = when (currentConfig.titleAlignment) {
            "RIGHT" -> android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            "LEFT" -> android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            else -> android.view.Gravity.CENTER
        }

        val bodyGravity = when (currentConfig.textAlignment) {
            "RIGHT" -> android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            "LEFT" -> android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            else -> android.view.Gravity.CENTER
        }

        views.setInt(R.id.item_title, "setGravity", titleGravity)
        views.setInt(R.id.item_body, "setGravity", bodyGravity)

        // Title Rendering
        if (item.title.isNotBlank() && currentConfig.showTitle) {
            views.setViewVisibility(R.id.item_title, android.view.View.VISIBLE)
            val isTitleBold = currentConfig.titleFontWeight == "BOLD"
            val titleSpanned = com.example.ui.components.RichTextHelper.htmlToSpanned(
                htmlText = item.title,
                baseIsBold = isTitleBold,
                baseIsItalic = false,
                baseIsUnderline = false
            )
            views.setTextViewText(R.id.item_title, titleSpanned)
            views.setTextViewTextSize(
                R.id.item_title,
                android.util.TypedValue.COMPLEX_UNIT_SP,
                currentConfig.titleFontSize.toFloat()
            )
            try {
                views.setTextColor(R.id.item_title, Color.parseColor(currentConfig.titleColorHex))
            } catch (e: Exception) {
                views.setTextColor(R.id.item_title, Color.parseColor("#F59E0B"))
            }
        } else {
            views.setViewVisibility(R.id.item_title, android.view.View.GONE)
        }

        // Body Rendering with Full Granular HTML Support
        views.setViewVisibility(R.id.item_body, android.view.View.VISIBLE)
        val isBodyBold = currentConfig.fontWeight == "BOLD"
        val isBodyItalic = currentConfig.isItalic
        val isBodyUnderline = currentConfig.isUnderline

        val bodySpanned = com.example.ui.components.RichTextHelper.htmlToSpanned(
            htmlText = item.body,
            baseIsBold = isBodyBold,
            baseIsItalic = isBodyItalic,
            baseIsUnderline = isBodyUnderline
        )

        views.setTextViewText(R.id.item_body, bodySpanned)
        views.setTextViewTextSize(
            R.id.item_body,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            currentConfig.fontSize.toFloat()
        )

        try {
            views.setTextColor(R.id.item_body, Color.parseColor(currentConfig.textColorHex))
        } catch (e: Exception) {
            views.setTextColor(R.id.item_body, Color.WHITE)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
