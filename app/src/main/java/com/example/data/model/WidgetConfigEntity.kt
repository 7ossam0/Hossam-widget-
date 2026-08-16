package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey
    val appWidgetId: Int,
    val name: String = "ودجت جديد",
    val categoryId: Long? = null,
    val contentMode: String = "CATEGORY_ALL", // "SINGLE", "MULTIPLE", "CATEGORY_ALL"
    val singleContentId: Long? = null,
    val selectedContentIds: String = "", // Comma-separated IDs if MULTIPLE
    val backgroundColorHex: String = "#1E293B",
    val gradientStartColorHex: String? = "#1E293B",
    val gradientEndColorHex: String? = "#0F172A",
    val gradientDirection: String = "TOP_BOTTOM", // "NONE", "TOP_BOTTOM", "LEFT_RIGHT", "DIAGONAL"
    val backgroundOpacity: Float = 0.95f,
    val textColorHex: String = "#FFFFFF",
    val titleColorHex: String = "#F59E0B",
    val secondaryTextColorHex: String = "#94A3B8",
    val titleFontSize: Int = 15, // sp for title
    val titleAlignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val titleFontWeight: String = "BOLD", // "NORMAL", "BOLD"
    val fontSize: Int = 14, // sp for body content
    val fontWeight: String = "NORMAL", // "NORMAL", "MEDIUM", "BOLD"
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val fontFamily: String = "TAJAWAL", // "DEFAULT", "CAIRO", "TAJAWAL", "AMIRI", "NOTO_KUFI", "NOTO_NASKH", "CUSTOM"
    val customFontPath: String? = null,
    val isLocked: Boolean = false, // When true, clicking on home screen widget will not open config/app
    val textAlignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val directionRtl: Boolean = true,
    val lineSpacing: Float = 1.2f,
    val letterSpacing: Float = 0.0f,
    val cornerRadius: Int = 16, // dp
    val padding: Int = 10, // dp
    val borderWidth: Int = 1, // dp
    val borderColorHex: String = "#33FFFFFF",
    val showTitle: Boolean = true,
    val showCategory: Boolean = true,
    val showDate: Boolean = true,
    val rotationMode: String = "MANUAL", // "AUTO", "MANUAL", "RANDOM", "SEQUENTIAL"
    val rotationIntervalMinutes: Int = 30, // 5, 15, 30, 60, 180, 1440
    val currentContentIndex: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
