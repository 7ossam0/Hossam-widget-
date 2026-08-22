package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.sample.SampleData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WidgetRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val categoryDao = db.categoryDao()
    private val contentItemDao = db.contentItemDao()
    private val widgetConfigDao = db.widgetConfigDao()
    private val customFontDao = db.customFontDao()
    private val tasbeehDao = db.tasbeehDao()

    // Tasbeeh Operations
    val allTasbeehItems: Flow<List<TasbeehEntity>> = tasbeehDao.getAllTasbeeh()

    suspend fun getActiveTasbeeh(): TasbeehEntity? = withContext(Dispatchers.IO) {
        tasbeehDao.getActiveTasbeeh()
    }

    suspend fun getTasbeehById(id: Long): TasbeehEntity? = withContext(Dispatchers.IO) {
        tasbeehDao.getTasbeehById(id)
    }

    suspend fun insertTasbeeh(item: TasbeehEntity): Long = withContext(Dispatchers.IO) {
        tasbeehDao.insert(item)
    }

    suspend fun updateTasbeeh(item: TasbeehEntity) = withContext(Dispatchers.IO) {
        tasbeehDao.update(item)
    }

    suspend fun deleteTasbeeh(item: TasbeehEntity) = withContext(Dispatchers.IO) {
        tasbeehDao.delete(item)
    }

    suspend fun incrementTasbeeh(id: Long) = withContext(Dispatchers.IO) {
        tasbeehDao.incrementCount(id)
    }

    suspend fun resetTasbeeh(id: Long) = withContext(Dispatchers.IO) {
        tasbeehDao.resetCount(id)
    }

    suspend fun setActiveTasbeeh(item: TasbeehEntity) = withContext(Dispatchers.IO) {
        // Unmark all and set this one as favorite
        val all = tasbeehDao.getAllTasbeeh()
        // Simple update
        tasbeehDao.update(item.copy(isFavorite = true))
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Custom Fonts
    val allCustomFonts: Flow<List<CustomFontEntity>> = customFontDao.getAllFonts()
    suspend fun getCustomFontById(id: Long) = customFontDao.getFontById(id)
    suspend fun importCustomFont(uri: android.net.Uri, displayFileName: String): CustomFontEntity? = withContext(Dispatchers.IO) {
        try {
            val fontsDir = java.io.File(context.filesDir, "custom_fonts")
            if (!fontsDir.exists()) fontsDir.mkdirs()

            val cleanName = displayFileName.substringBeforeLast(".")
            val extension = displayFileName.substringAfterLast(".", "ttf")
            val destFile = java.io.File(fontsDir, "font_${System.currentTimeMillis()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                val entity = CustomFontEntity(
                    name = cleanName,
                    filePath = destFile.absolutePath,
                    fileName = displayFileName
                )
                val id = customFontDao.insertFont(entity)
                entity.copy(id = id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteCustomFont(font: CustomFontEntity) = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(font.filePath)
            if (file.exists()) file.delete()
            customFontDao.deleteFont(font)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun getCategoryById(id: Long) = categoryDao.getCategoryById(id)
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Content Items
    val allContentItems: Flow<List<ContentItemEntity>> = contentItemDao.getAllContentItems()
    val favoriteContentItems: Flow<List<ContentItemEntity>> = contentItemDao.getFavoriteContentItems()
    fun getContentItemsByCategory(categoryId: Long) = contentItemDao.getContentItemsByCategory(categoryId)
    
    suspend fun getContentItemById(id: Long) = contentItemDao.getContentItemById(id)
    suspend fun insertContentItem(item: ContentItemEntity) = contentItemDao.insertContentItem(item)
    suspend fun updateContentItem(item: ContentItemEntity) = contentItemDao.updateContentItem(item)
    suspend fun deleteContentItem(item: ContentItemEntity) = contentItemDao.deleteContentItem(item)

    // Widget Configs
    val allWidgetConfigs: Flow<List<WidgetConfigEntity>> = widgetConfigDao.getAllWidgetConfigs()
    suspend fun getWidgetConfigById(appWidgetId: Int) = widgetConfigDao.getWidgetConfigById(appWidgetId)
    fun getWidgetConfigByIdFlow(appWidgetId: Int) = widgetConfigDao.getWidgetConfigByIdFlow(appWidgetId)
    suspend fun insertWidgetConfig(config: WidgetConfigEntity) = widgetConfigDao.insertWidgetConfig(config)
    suspend fun insertWidgetConfigs(configs: List<WidgetConfigEntity>) = widgetConfigDao.insertWidgetConfigs(configs)
    suspend fun updateWidgetConfig(config: WidgetConfigEntity) = widgetConfigDao.updateWidgetConfig(config)
    suspend fun deleteWidgetConfig(appWidgetId: Int) = widgetConfigDao.deleteWidgetConfigById(appWidgetId)

    // Initial Seeding
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val categories = categoryDao.getAllCategoriesList()
        if (categories.isEmpty()) {
            categoryDao.insertCategories(SampleData.initialCategories)
            contentItemDao.insertContentItems(SampleData.initialContentItems)
        }
        val tasbeehCount = tasbeehDao.getCount()
        if (tasbeehCount == 0) {
            tasbeehDao.insertAll(SampleData.initialTasbeehItems)
        }
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        contentItemDao.deleteAllContentItems()
        categoryDao.deleteAllCategories()
        tasbeehDao.insertAll(SampleData.initialTasbeehItems)
        categoryDao.insertCategories(SampleData.initialCategories)
        contentItemDao.insertContentItems(SampleData.initialContentItems)
    }

    // Resolves content items for a specific widget config
    suspend fun getItemsForWidgetConfig(config: WidgetConfigEntity): List<ContentItemEntity> = withContext(Dispatchers.IO) {
        seedDatabaseIfEmpty()
        val items = when (config.contentMode) {
            "SINGLE" -> {
                val id = config.singleContentId
                if (id != null) {
                    val item = contentItemDao.getContentItemById(id)
                    if (item != null && item.isActive) listOf(item) else emptyList()
                } else emptyList()
            }
            "MULTIPLE" -> {
                val ids = config.selectedContentIds
                    .split(",")
                    .mapNotNull { it.trim().toLongOrNull() }
                if (ids.isEmpty()) emptyList() else contentItemDao.getContentItemsByIds(ids)
            }
            "CATEGORY_ALL" -> {
                val catId = config.categoryId
                if (catId != null) {
                    contentItemDao.getContentItemsByCategoryList(catId)
                } else {
                    contentItemDao.getActiveContentItemsList()
                }
            }
            else -> contentItemDao.getActiveContentItemsList()
        }

        if (items.isNotEmpty()) {
            items
        } else {
            val allActive = contentItemDao.getActiveContentItemsList()
            if (allActive.isNotEmpty()) allActive else SampleData.initialContentItems
        }
    }

    // Export to JSON string
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val categories = categoryDao.getAllCategoriesList().map {
            CategoryDto(it.id, it.name, it.sortOrder, it.colorHex)
        }
        val items = contentItemDao.getAllContentItemsList().map {
            ContentItemDto(it.id, it.title, it.body, it.categoryId, it.sortOrder, it.isActive, it.isFavorite, it.repeatCount, it.createdAt, it.updatedAt)
        }
        val configs = widgetConfigDao.getAllWidgetConfigsList().map {
            WidgetConfigDto(
                it.appWidgetId, it.name, it.categoryId, it.contentMode, it.singleContentId,
                it.selectedContentIds, it.backgroundColorHex, it.gradientStartColorHex,
                it.gradientEndColorHex, it.gradientDirection, it.backgroundOpacity,
                it.textColorHex, it.titleColorHex, it.secondaryTextColorHex, it.fontSize,
                it.fontWeight, it.isItalic, it.isUnderline, it.fontFamily, it.textAlignment,
                it.directionRtl, it.lineSpacing, it.letterSpacing, it.cornerRadius,
                it.padding, it.borderWidth, it.borderColorHex, it.showTitle, it.showCategory,
                it.showDate, it.rotationMode, it.rotationIntervalMinutes, it.currentContentIndex,
                it.sortOrder, it.createdAt, it.updatedAt
            )
        }
        val exportData = ExportData(
            categories = categories,
            contentItems = items,
            widgetConfigs = configs
        )
        val adapter = moshi.adapter(ExportData::class.java).indent("  ")
        adapter.toJson(exportData)
    }

    // Import from JSON string
    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(ExportData::class.java)
            val exportData = adapter.fromJson(jsonString) ?: return@withContext false

            categoryDao.deleteAllCategories()
            contentItemDao.deleteAllContentItems()
            widgetConfigDao.deleteAllWidgetConfigs()

            val newCategories = exportData.categories.map {
                CategoryEntity(it.id, it.name, it.sortOrder, it.colorHex)
            }
            val newItems = exportData.contentItems.map {
                ContentItemEntity(it.id, it.title, it.body, it.categoryId, it.sortOrder, it.isActive, it.isFavorite, it.repeatCount, it.createdAt, it.updatedAt)
            }
            val newConfigs = exportData.widgetConfigs.map {
                WidgetConfigEntity(
                    appWidgetId = it.appWidgetId,
                    name = it.name,
                    categoryId = it.categoryId,
                    contentMode = it.contentMode,
                    singleContentId = it.singleContentId,
                    selectedContentIds = it.selectedContentIds,
                    backgroundColorHex = it.backgroundColorHex,
                    gradientStartColorHex = it.gradientStartColorHex,
                    gradientEndColorHex = it.gradientEndColorHex,
                    gradientDirection = it.gradientDirection,
                    backgroundOpacity = it.backgroundOpacity,
                    textColorHex = it.textColorHex,
                    titleColorHex = it.titleColorHex,
                    secondaryTextColorHex = it.secondaryTextColorHex,
                    fontSize = it.fontSize,
                    fontWeight = it.fontWeight,
                    isItalic = it.isItalic,
                    isUnderline = it.isUnderline,
                    fontFamily = it.fontFamily,
                    customFontPath = null,
                    isLocked = false,
                    textAlignment = it.textAlignment,
                    directionRtl = it.directionRtl,
                    lineSpacing = it.lineSpacing,
                    letterSpacing = it.letterSpacing,
                    cornerRadius = it.cornerRadius,
                    padding = it.padding,
                    borderWidth = it.borderWidth,
                    borderColorHex = it.borderColorHex,
                    showTitle = it.showTitle,
                    showCategory = it.showCategory,
                    showDate = it.showDate,
                    rotationMode = it.rotationMode,
                    rotationIntervalMinutes = it.rotationIntervalMinutes,
                    currentContentIndex = it.currentContentIndex,
                    sortOrder = it.sortOrder,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }

            categoryDao.insertCategories(newCategories)
            contentItemDao.insertContentItems(newItems)
            widgetConfigDao.insertWidgetConfigs(newConfigs)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
