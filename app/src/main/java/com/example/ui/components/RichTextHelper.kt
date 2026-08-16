package com.example.ui.components

import android.graphics.Color as AndroidColor
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import java.util.regex.Pattern

object RichTextHelper {

    /**
     * Converts HTML-formatted string to Android Spanned for StaticLayout / Canvas / RemoteViews rendering.
     */
    fun htmlToSpanned(
        htmlText: String,
        baseIsBold: Boolean = false,
        baseIsItalic: Boolean = false,
        baseIsUnderline: Boolean = false
    ): CharSequence {
        if (htmlText.isBlank()) return ""
        val spanned = try {
            // First process custom style spans (like background-color and mark tags)
            var processed = htmlText
                .replace(Regex("<mark(?: style=\"background-color:\\s*([^\"]+)\")?>(.*?)</mark>", RegexOption.DOT_MATCHES_ALL)) { match ->
                    val color = match.groups[1]?.value ?: "#FEF08A"
                    "<span style=\"background-color:$color\">${match.groups[2]?.value ?: ""}</span>"
                }
            
            val baseSpanned = HtmlCompat.fromHtml(processed, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val builder = SpannableStringBuilder(baseSpanned)

            // Handle background colors with custom regex matcher over the raw html
            val spanPattern = Pattern.compile("<span style=\"background-color:\\s*([^\"]+)\">(.*?)</span>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val matcher = spanPattern.matcher(htmlText)
            while (matcher.find()) {
                val colorHex = matcher.group(1) ?: "#FEF08A"
                val innerText = matcher.group(2)?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString() } ?: ""
                if (innerText.isNotEmpty()) {
                    val rawStr = builder.toString()
                    var start = rawStr.indexOf(innerText)
                    while (start != -1) {
                        try {
                            val parsedColor = AndroidColor.parseColor(colorHex.trim())
                            builder.setSpan(
                                BackgroundColorSpan(parsedColor),
                                start,
                                start + innerText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        } catch (e: Exception) {
                            // ignore invalid color
                        }
                        start = rawStr.indexOf(innerText, start + innerText.length)
                    }
                }
            }

            // Apply base widget typography if required
            val len = builder.length
            if (len > 0) {
                if (baseIsBold && baseIsItalic) {
                    builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (baseIsBold) {
                    builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (baseIsItalic) {
                    builder.setSpan(StyleSpan(android.graphics.Typeface.ITALIC), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (baseIsUnderline) {
                    builder.setSpan(UnderlineSpan(), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            builder
        } catch (e: Exception) {
            htmlText
        }
        return spanned
    }

    /**
     * Converts HTML string to Jetpack Compose AnnotatedString for pixel-perfect in-app and live preview rendering.
     */
    fun htmlToAnnotatedString(
        htmlText: String,
        defaultColor: Color = Color.Unspecified,
        defaultFontSize: TextUnit = TextUnit.Unspecified
    ): AnnotatedString {
        if (htmlText.isBlank()) return buildAnnotatedString { append("") }

        // If no HTML tags found, return simple text
        if (!htmlText.contains("<") || !htmlText.contains(">")) {
            return buildAnnotatedString {
                append(htmlText)
            }
        }

        return try {
            val spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val text = spanned.toString()

            buildAnnotatedString {
                append(text)

                // Apply Spans from Spanned representation
                val spans = spanned.getSpans(0, text.length, Any::class.java)
                for (span in spans) {
                    val start = spanned.getSpanStart(span).coerceIn(0, text.length)
                    val end = spanned.getSpanEnd(span).coerceIn(0, text.length)
                    if (start >= end) continue

                    when (span) {
                        is StyleSpan -> {
                            when (span.style) {
                                android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                                android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                                android.graphics.Typeface.BOLD_ITALIC -> addStyle(
                                    SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                                    start,
                                    end
                                )
                            }
                        }
                        is UnderlineSpan -> {
                            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                        }
                        is StrikethroughSpan -> {
                            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                        }
                        is ForegroundColorSpan -> {
                            val composeColor = Color(span.foregroundColor)
                            addStyle(SpanStyle(color = composeColor), start, end)
                        }
                        is BackgroundColorSpan -> {
                            val composeBgColor = Color(span.backgroundColor)
                            addStyle(SpanStyle(background = composeBgColor), start, end)
                        }
                        is RelativeSizeSpan -> {
                            val multiplier = span.sizeChange
                            if (defaultFontSize != TextUnit.Unspecified) {
                                addStyle(SpanStyle(fontSize = defaultFontSize * multiplier), start, end)
                            } else {
                                addStyle(SpanStyle(fontSize = 16.sp * multiplier), start, end)
                            }
                        }
                    }
                }

                // Also parse background colors from custom <span style="background-color:..."> or <mark>
                val bgMatcher = Pattern.compile("<(?:span style=\"background-color:|mark style=\"background-color:)\\s*([^\"]+)\">(.*?)</(?:span|mark)>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
                val m = bgMatcher.matcher(htmlText)
                while (m.find()) {
                    val colorHex = m.group(1) ?: "#FEF08A"
                    val inner = m.group(2)?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString() } ?: ""
                    if (inner.isNotEmpty()) {
                        var startPos = text.indexOf(inner)
                        while (startPos != -1) {
                            try {
                                val c = AndroidColor.parseColor(colorHex.trim())
                                addStyle(SpanStyle(background = Color(c)), startPos, startPos + inner.length)
                            } catch (e: Exception) {
                                // ignore
                            }
                            startPos = text.indexOf(inner, startPos + inner.length)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            buildAnnotatedString { append(htmlText) }
        }
    }

    /**
     * Applies formatting tags around selection or inserts tags at cursor position in TextFieldValue.
     */
    fun applyTagToSelection(
        value: TextFieldValue,
        openTag: String,
        closeTag: String
    ): TextFieldValue {
        val selectedText = value.getSelectedText().text
        val textBefore = value.getTextBeforeSelection(value.text.length).text
        val textAfter = value.getTextAfterSelection(value.text.length).text

        return if (selectedText.isNotEmpty()) {
            // If already wrapped in exact same tags, strip them
            if (selectedText.startsWith(openTag) && selectedText.endsWith(closeTag)) {
                val unwrapped = selectedText.removeSurrounding(openTag, closeTag)
                val newText = textBefore + unwrapped + textAfter
                val newSelectionStart = textBefore.length
                val newSelectionEnd = newSelectionStart + unwrapped.length
                TextFieldValue(newText, androidx.compose.ui.text.TextRange(newSelectionStart, newSelectionEnd))
            } else {
                val formatted = "$openTag$selectedText$closeTag"
                val newText = textBefore + formatted + textAfter
                val newSelectionStart = textBefore.length
                val newSelectionEnd = newSelectionStart + formatted.length
                TextFieldValue(newText, androidx.compose.ui.text.TextRange(newSelectionStart, newSelectionEnd))
            }
        } else {
            // Insert empty tags and place cursor inside
            val newText = "$textBefore$openTag$closeTag$textAfter"
            val cursorInside = textBefore.length + openTag.length
            TextFieldValue(newText, androidx.compose.ui.text.TextRange(cursorInside, cursorInside))
        }
    }

    /**
     * Inserts an Islamic bracket/phrase or wraps selection with brackets.
     */
    fun insertIslamicDecor(
        value: TextFieldValue,
        openBracket: String,
        closeBracket: String
    ): TextFieldValue {
        val selectedText = value.getSelectedText().text
        val textBefore = value.getTextBeforeSelection(value.text.length).text
        val textAfter = value.getTextAfterSelection(value.text.length).text

        return if (selectedText.isNotEmpty()) {
            val formatted = "$openBracket $selectedText $closeBracket"
            val newText = textBefore + formatted + textAfter
            val newSelectionStart = textBefore.length
            val newSelectionEnd = newSelectionStart + formatted.length
            TextFieldValue(newText, androidx.compose.ui.text.TextRange(newSelectionStart, newSelectionEnd))
        } else {
            val inserted = "$openBracket  $closeBracket"
            val newText = textBefore + inserted + textAfter
            val cursorInside = textBefore.length + openBracket.length + 1
            TextFieldValue(newText, androidx.compose.ui.text.TextRange(cursorInside, cursorInside))
        }
    }

    fun insertTextAtCursor(
        value: TextFieldValue,
        insertion: String
    ): TextFieldValue {
        val textBefore = value.getTextBeforeSelection(value.text.length).text
        val textAfter = value.getTextAfterSelection(value.text.length).text
        val newText = "$textBefore $insertion $textAfter"
        val newCursor = textBefore.length + insertion.length + 2
        return TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor, newCursor))
    }

    /**
     * Clears all HTML tags from the selected text or whole text if no selection.
     */
    fun stripHtmlTags(value: TextFieldValue): TextFieldValue {
        val selectedText = value.getSelectedText().text
        val textBefore = value.getTextBeforeSelection(value.text.length).text
        val textAfter = value.getTextAfterSelection(value.text.length).text

        val target = if (selectedText.isNotEmpty()) selectedText else value.text
        val clean = target.replace(Regex("<[^>]*>"), "")

        return if (selectedText.isNotEmpty()) {
            val newText = textBefore + clean + textAfter
            val start = textBefore.length
            TextFieldValue(newText, androidx.compose.ui.text.TextRange(start, start + clean.length))
        } else {
            TextFieldValue(clean, androidx.compose.ui.text.TextRange(clean.length, clean.length))
        }
    }
}
