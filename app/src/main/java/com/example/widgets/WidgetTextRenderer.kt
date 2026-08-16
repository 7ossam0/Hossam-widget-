package com.example.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.example.data.model.WidgetConfigEntity
import java.io.File

object WidgetTextRenderer {

    fun getTypeface(config: WidgetConfigEntity, isBold: Boolean = false, isItalic: Boolean = false): Typeface {
        // 1. Try Custom Font File
        if (!config.customFontPath.isNullOrBlank()) {
            try {
                val file = File(config.customFontPath)
                if (file.exists() && file.canRead()) {
                    val baseTypeface = Typeface.createFromFile(file)
                    val style = when {
                        isItalic && isBold -> Typeface.BOLD_ITALIC
                        isBold -> Typeface.BOLD
                        isItalic -> Typeface.ITALIC
                        else -> Typeface.NORMAL
                    }
                    return Typeface.create(baseTypeface, style)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // 2. Fallback to System/Named Typefaces
        val baseTypeface = when (config.fontFamily) {
            "CAIRO", "AMIRI" -> Typeface.SERIF
            "NOTO_KUFI", "TAJAWAL" -> Typeface.SANS_SERIF
            "MONOSPACE" -> Typeface.MONOSPACE
            else -> Typeface.DEFAULT
        }

        val style = when {
            isItalic && isBold -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        return Typeface.create(baseTypeface, style)
    }

    fun renderContentItem(
        context: Context,
        title: String?,
        body: String,
        config: WidgetConfigEntity,
        targetWidthPx: Int = 800
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val scaledDensity = context.resources.displayMetrics.scaledDensity

        val width = targetWidthPx.coerceAtLeast(300)

        // Body Typeface
        val bodyTypeface = getTypeface(
            config = config,
            isBold = config.fontWeight == "BOLD",
            isItalic = config.isItalic
        )

        // Title Typeface
        val titleTypeface = getTypeface(
            config = config,
            isBold = config.titleFontWeight == "BOLD",
            isItalic = false
        )

        // Title Alignment
        val titleAlignment = when (config.titleAlignment) {
            "RIGHT" -> Layout.Alignment.ALIGN_NORMAL
            "LEFT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        // Body Alignment
        val bodyAlignment = when (config.textAlignment) {
            "RIGHT" -> Layout.Alignment.ALIGN_NORMAL
            "LEFT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val textDirection = TextDirectionHeuristics.RTL

        // Title Layout Setup
        var titleLayout: StaticLayout? = null
        if (!title.isNullOrBlank() && config.showTitle) {
            val spannedTitle = com.example.ui.components.RichTextHelper.htmlToSpanned(title)
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = titleTypeface
                textSize = config.titleFontSize * scaledDensity
                try {
                    color = Color.parseColor(config.titleColorHex)
                } catch (e: Exception) {
                    color = Color.parseColor("#F59E0B")
                }
                if (config.isUnderline) {
                    flags = flags or Paint.UNDERLINE_TEXT_FLAG
                }
            }

            titleLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(spannedTitle, 0, spannedTitle.length, titlePaint, width)
                    .setAlignment(titleAlignment)
                    .setTextDirection(textDirection)
                    .setLineSpacing(0f, 1.15f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(spannedTitle, titlePaint, width, titleAlignment, 1.15f, 0f, true)
            }
        }

        // Body Layout Setup
        val spannedBody = com.example.ui.components.RichTextHelper.htmlToSpanned(body)
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = bodyTypeface
            textSize = config.fontSize * scaledDensity
            try {
                color = Color.parseColor(config.textColorHex)
            } catch (e: Exception) {
                color = Color.WHITE
            }
            if (config.isUnderline) {
                flags = flags or Paint.UNDERLINE_TEXT_FLAG
            }
            if (config.letterSpacing != 0f) {
                letterSpacing = config.letterSpacing / 10f
            }
        }

        val bodyLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(spannedBody, 0, spannedBody.length, bodyPaint, width)
                .setAlignment(bodyAlignment)
                .setTextDirection(textDirection)
                .setLineSpacing(0f, config.lineSpacing.coerceIn(0.8f, 2.5f))
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(spannedBody, bodyPaint, width, bodyAlignment, config.lineSpacing, 0f, true)
        }

        val spacingBetween = if (titleLayout != null) (8 * density).toInt() else 0
        val topBottomPadding = (6 * density).toInt()

        val totalHeight = (titleLayout?.height ?: 0) + spacingBetween + bodyLayout.height + (topBottomPadding * 2)
        val validHeight = totalHeight.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(width, validHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        var currentY = topBottomPadding.toFloat()

        if (titleLayout != null) {
            canvas.save()
            canvas.translate(0f, currentY)
            titleLayout.draw(canvas)
            canvas.restore()
            currentY += titleLayout.height + spacingBetween
        }

        canvas.save()
        canvas.translate(0f, currentY)
        bodyLayout.draw(canvas)
        canvas.restore()

        return bitmap
    }
}
