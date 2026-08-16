package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val colorHex: String
)

@JsonClass(generateAdapter = true)
data class ContentItemDto(
    val id: Long,
    val title: String,
    val body: String,
    val categoryId: Long?,
    val sortOrder: Int,
    val isActive: Boolean,
    val isFavorite: Boolean,
    val repeatCount: Int = 1,
    val createdAt: Long,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class WidgetConfigDto(
    val appWidgetId: Int,
    val name: String,
    val categoryId: Long?,
    val contentMode: String,
    val singleContentId: Long?,
    val selectedContentIds: String,
    val backgroundColorHex: String,
    val gradientStartColorHex: String?,
    val gradientEndColorHex: String?,
    val gradientDirection: String,
    val backgroundOpacity: Float,
    val textColorHex: String,
    val titleColorHex: String,
    val secondaryTextColorHex: String,
    val fontSize: Int,
    val fontWeight: String,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val fontFamily: String,
    val textAlignment: String,
    val directionRtl: Boolean,
    val lineSpacing: Float,
    val letterSpacing: Float,
    val cornerRadius: Int,
    val padding: Int,
    val borderWidth: Int,
    val borderColorHex: String,
    val showTitle: Boolean,
    val showCategory: Boolean,
    val showDate: Boolean,
    val rotationMode: String,
    val rotationIntervalMinutes: Int,
    val currentContentIndex: Int,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class ExportData(
    val appName: String = "Widget Studio",
    val exportTimestamp: Long = System.currentTimeMillis(),
    val categories: List<CategoryDto>,
    val contentItems: List<ContentItemDto>,
    val widgetConfigs: List<WidgetConfigDto>
)
