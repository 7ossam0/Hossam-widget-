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
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.WidgetConfigEntity
import java.io.File

object WidgetTextRenderer {

    fun renderWidgetBackground(
        context: Context,
        config: WidgetConfigEntity,
        widthPx: Int,
        heightPx: Int
    ): Bitmap {
        val width = widthPx.coerceAtLeast(100)
        val height = heightPx.coerceAtLeast(100)
        val density = context.resources.displayMetrics.density

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cornerRadius = (config.cornerRadius * density).coerceAtLeast(0f)
        val borderWidth = (config.borderWidth * density).coerceAtLeast(0f)

        // Parse colors
        val bgColor = try { Color.parseColor(config.backgroundColorHex) } catch (e: Exception) { Color.parseColor("#161B22") }
        val startColor = try { Color.parseColor(config.gradientStartColorHex) } catch (e: Exception) { bgColor }
        val endColor = try { Color.parseColor(config.gradientEndColorHex) } catch (e: Exception) { bgColor }
        val borderColor = try { Color.parseColor(config.borderColorHex) } catch (e: Exception) { Color.parseColor("#3300E5FF") }

        val alpha = (config.backgroundOpacity.coerceIn(0f, 1f) * 255).toInt()

        fun applyAlpha(color: Int, a: Int): Int {
            return Color.argb(
                (Color.alpha(color) * (a / 255f)).toInt().coerceIn(0, 255),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

        val startColorWithAlpha = applyAlpha(startColor, alpha)
        val endColorWithAlpha = applyAlpha(endColor, alpha)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = when (config.gradientDirection) {
                "TOP_BOTTOM" -> android.graphics.LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    startColorWithAlpha, endColorWithAlpha,
                    android.graphics.Shader.TileMode.CLAMP
                )
                "LEFT_RIGHT" -> android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    startColorWithAlpha, endColorWithAlpha,
                    android.graphics.Shader.TileMode.CLAMP
                )
                "DIAGONAL" -> android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    startColorWithAlpha, endColorWithAlpha,
                    android.graphics.Shader.TileMode.CLAMP
                )
                else -> android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    startColorWithAlpha, startColorWithAlpha,
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
        }

        val rect = android.graphics.RectF(
            borderWidth / 2f,
            borderWidth / 2f,
            width - borderWidth / 2f,
            height - borderWidth / 2f
        )

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)

        // Draw Border
        if (config.borderWidth > 0) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
                color = borderColor
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
        }

        return bitmap
    }

    private fun getTypeface(
        context: Context,
        config: WidgetConfigEntity,
        isBold: Boolean = false,
        isItalic: Boolean = false
    ): Typeface {
        // 1. Check for custom imported font file
        if (!config.customFontPath.isNullOrBlank()) {
            try {
                val file = File(config.customFontPath)
                if (file.exists()) {
                    return Typeface.createFromFile(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Default app font (Tajawal / F5 font loaded from res/font)
        try {
            val fontRes = if (isBold) R.font.app_font_bold else R.font.app_font
            val tf = ResourcesCompat.getFont(context, fontRes)
            if (tf != null) {
                val style = when {
                    isItalic && isBold -> Typeface.BOLD_ITALIC
                    isBold -> Typeface.BOLD
                    isItalic -> Typeface.ITALIC
                    else -> Typeface.NORMAL
                }
                return Typeface.create(tf, style)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 3. Fallback to System Typefaces
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
            context = context,
            config = config,
            isBold = config.fontWeight == "BOLD",
            isItalic = config.isItalic
        )

        // Title Typeface
        val titleTypeface = getTypeface(
            context = context,
            config = config,
            isBold = config.titleFontWeight == "BOLD",
            isItalic = false
        )

        val textDirection = TextDirectionHeuristics.RTL

        val bodyAlignment = when (config.textAlignment) {
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val titleAlignment = when (config.titleAlignment) {
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        // Title Layout Setup
        var titleLayout: StaticLayout? = null
        if (!title.isNullOrBlank()) {
            val spannedTitle = com.example.ui.components.RichTextHelper.htmlToSpanned(title)
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = titleTypeface
                textSize = (config.fontSize + 2) * scaledDensity
                try {
                    color = Color.parseColor(config.titleColorHex)
                } catch (e: Exception) {
                    color = Color.parseColor("#00E5FF")
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
