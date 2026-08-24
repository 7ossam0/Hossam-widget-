package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.data.model.WidgetConfigEntity
import com.example.data.repository.WidgetRepository
import com.example.widgets.WidgetManagerHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WidgetRepository(application)

    val widgetConfigs: StateFlow<List<WidgetConfigEntity>> = repository.allWidgetConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentItems: StateFlow<List<ContentItemEntity>> = repository.allContentItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteItems: StateFlow<List<ContentItemEntity>> = repository.favoriteContentItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customFonts: StateFlow<List<com.example.data.model.CustomFontEntity>> = repository.allCustomFonts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasbeeh state
    val tasbeehItems: StateFlow<List<com.example.data.model.TasbeehEntity>> = repository.allTasbeehItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTasbeehId = MutableStateFlow<Long?>(null)
    val selectedTasbeehId: StateFlow<Long?> = _selectedTasbeehId.asStateFlow()

    val activeTasbeeh: StateFlow<com.example.data.model.TasbeehEntity?> = combine(tasbeehItems, _selectedTasbeehId) { items, selectedId ->
        if (items.isEmpty()) null
        else if (selectedId != null) items.find { it.id == selectedId } ?: items.first()
        else items.find { it.isFavorite } ?: items.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Prayer & Celestial Sky state
    val selectedCity = MutableStateFlow(com.example.data.prayer.PrayerTimeCalculator.PRESET_CITIES[0])
    val selectedCalculationMethod = MutableStateFlow(com.example.data.prayer.CalculationMethod.UMM_AL_QURA)
    val simulatedHour = MutableStateFlow<Float?>(null) // null for real-time

    private val _currentCalendar = MutableStateFlow(java.util.Calendar.getInstance())

    val prayerSchedule: StateFlow<com.example.data.prayer.DailyPrayerSchedule> = combine(
        selectedCity,
        selectedCalculationMethod,
        simulatedHour,
        _currentCalendar
    ) { city, method, simHour, cal ->
        val effectiveCal = if (simHour != null) {
            val c = java.util.Calendar.getInstance()
            val h = simHour.toInt()
            val m = ((simHour - h) * 60).toInt()
            c.set(java.util.Calendar.HOUR_OF_DAY, h)
            c.set(java.util.Calendar.MINUTE, m)
            c.set(java.util.Calendar.SECOND, 0)
            c
        } else {
            cal
        }
        com.example.data.prayer.PrayerTimeCalculator.calculateDailySchedule(
            calendar = effectiveCal,
            location = city,
            method = method
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.data.prayer.PrayerTimeCalculator.calculateDailySchedule()
    )

    private val _selectedCategoryIdFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryIdFilter: StateFlow<Long?> = _selectedCategoryIdFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.seedDatabaseIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Live ticker for second-by-second countdown and sky updates
        viewModelScope.launch {
            try {
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    _currentCalendar.value = java.util.Calendar.getInstance()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Tasbeeh Operations
    fun selectTasbeeh(id: Long) {
        _selectedTasbeehId.value = id
    }

    fun incrementTasbeeh(id: Long) {
        viewModelScope.launch {
            repository.incrementTasbeeh(id)
            // Update widget if needed
            com.example.widgets.TasbeehAppWidgetProvider.updateTasbeehWidgets(getApplication())
        }
    }

    fun resetTasbeeh(id: Long) {
        viewModelScope.launch {
            repository.resetTasbeeh(id)
            com.example.widgets.TasbeehAppWidgetProvider.updateTasbeehWidgets(getApplication())
            _statusMessage.value = "تم تصفير عداد المسبحة"
        }
    }

    fun addCustomTasbeeh(title: String, subtitle: String, targetCount: Int, colorHex: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val item = com.example.data.model.TasbeehEntity(
                    title = title.trim(),
                    subtitle = subtitle.trim(),
                    targetCount = targetCount,
                    colorHex = colorHex,
                    orderIndex = tasbeehItems.value.size + 1
                )
                val id = repository.insertTasbeeh(item)
                _selectedTasbeehId.value = id
                _statusMessage.value = "تمت إضافة الذكر الجديد للمسبحة 📿"
            }
        }
    }

    fun updateTasbeeh(item: com.example.data.model.TasbeehEntity) {
        viewModelScope.launch {
            repository.updateTasbeeh(item)
            _statusMessage.value = "تم تحديث الذكر"
        }
    }

    fun deleteTasbeeh(item: com.example.data.model.TasbeehEntity) {
        viewModelScope.launch {
            repository.deleteTasbeeh(item)
            _statusMessage.value = "تم حذف الذكر من المسبحة"
        }
    }

    // Prayer Operations
    fun selectCity(city: com.example.data.prayer.CityLocation) {
        selectedCity.value = city
        selectedCalculationMethod.value = city.defaultMethod
        _statusMessage.value = "تم تغيير المدينة إلى ${city.cityName}"
        com.example.widgets.PrayerTimesAppWidgetProvider.updatePrayerWidgets(getApplication())
    }

    fun setCalculationMethod(method: com.example.data.prayer.CalculationMethod) {
        selectedCalculationMethod.value = method
        _statusMessage.value = "تم تحديث طريقة الحساب: ${method.displayName}"
        com.example.widgets.PrayerTimesAppWidgetProvider.updatePrayerWidgets(getApplication())
    }

    fun setSimulatedHour(hour: Float?) {
        simulatedHour.value = hour
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryIdFilter.value = categoryId
    }

    // Category Operations
    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val newCat = CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    sortOrder = categories.value.size + 1
                )
                repository.insertCategory(newCat)
                _statusMessage.value = "تمت إضافة التصنيف بنجاح"
            }
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _statusMessage.value = "تم تحديث التصنيف"
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _statusMessage.value = "تم حذف التصنيف"
        }
    }

    // Content Item Operations
    fun addContentItem(title: String, body: String, categoryId: Long?, isFavorite: Boolean = false, repeatCount: Int = 1) {
        viewModelScope.launch {
            if (body.isNotBlank()) {
                val item = ContentItemEntity(
                    title = title.trim(),
                    body = body.trim(),
                    categoryId = categoryId,
                    isFavorite = isFavorite,
                    repeatCount = repeatCount.coerceAtLeast(1)
                )
                repository.insertContentItem(item)
                WidgetManagerHelper.updateAllWidgets(getApplication())
                _statusMessage.value = "تم إضافة النص بنجاح"
            }
        }
    }

    fun addBulkContentItems(itemsList: List<Pair<String, String>>, categoryId: Long?) {
        viewModelScope.launch {
            var addedCount = 0
            val currentOrder = contentItems.value.size
            for ((idx, pair) in itemsList.withIndex()) {
                val (t, b) = pair
                if (b.isNotBlank()) {
                    val item = ContentItemEntity(
                        title = t.trim(),
                        body = b.trim(),
                        categoryId = categoryId,
                        sortOrder = currentOrder + idx + 1
                    )
                    repository.insertContentItem(item)
                    addedCount++
                }
            }
            if (addedCount > 0) {
                WidgetManagerHelper.updateAllWidgets(getApplication())
                _statusMessage.value = "تمت إضافة $addedCount نص/دعاء بنجاح"
            }
        }
    }

    fun updateContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.updateContentItem(item.copy(updatedAt = System.currentTimeMillis()))
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تم تحديث النص"
        }
    }

    fun toggleFavorite(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.updateContentItem(item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis()))
            _statusMessage.value = if (!item.isFavorite) "تمت الإضافة للمفضلة" else "تمت الإزالة من المفضلة"
        }
    }

    fun toggleActive(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.updateContentItem(item.copy(isActive = !item.isActive, updatedAt = System.currentTimeMillis()))
            WidgetManagerHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.deleteContentItem(item)
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تم حذف النص"
        }
    }

    // Widget Config Operations
    fun saveWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch {
            repository.insertWidgetConfig(config)
            WidgetManagerHelper.updateSingleWidget(getApplication(), config.appWidgetId)
            _statusMessage.value = "تم حفظ إعدادات الودجت وتحديث الشاشة الرئيسية"
        }
    }

    fun advanceWidgetContent(config: WidgetConfigEntity) {
        viewModelScope.launch {
            val updated = config.copy(
                currentContentIndex = config.currentContentIndex + 1,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateWidgetConfig(updated)
            WidgetManagerHelper.updateSingleWidget(getApplication(), config.appWidgetId)
        }
    }

    fun copyWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch {
            val currentList = widgetConfigs.value
            val nextSortOrder = (currentList.maxOfOrNull { it.sortOrder } ?: 0) + 1
            val newAppWidgetId = (1000..9999).random()
            val copied = config.copy(
                appWidgetId = newAppWidgetId,
                name = "${config.name} (نسخة)",
                sortOrder = nextSortOrder,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertWidgetConfig(copied)
            _statusMessage.value = "تم إنشاء نسخة من الودجت"
        }
    }

    fun createNewStandaloneWidgetConfig(name: String, categoryId: Long?): WidgetConfigEntity {
        val currentList = widgetConfigs.value
        val nextSortOrder = (currentList.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val newAppWidgetId = (10000..99999).random()
        return WidgetConfigEntity(
            appWidgetId = newAppWidgetId,
            name = if (name.isNotBlank()) name else "ودجت جديد $newAppWidgetId",
            categoryId = categoryId,
            sortOrder = nextSortOrder
        )
    }

    fun moveWidgetUp(config: WidgetConfigEntity) {
        viewModelScope.launch {
            val list = widgetConfigs.value
            val currentIndex = list.indexOfFirst { it.appWidgetId == config.appWidgetId }
            if (currentIndex > 0) {
                val prevConfig = list[currentIndex - 1]
                val updatedPrev = prevConfig.copy(sortOrder = currentIndex)
                val updatedCurrent = config.copy(sortOrder = currentIndex - 1)
                repository.insertWidgetConfigs(listOf(updatedPrev, updatedCurrent))
                _statusMessage.value = "تم تحريك الودجت للأعلى ⬆️"
            }
        }
    }

    fun moveWidgetDown(config: WidgetConfigEntity) {
        viewModelScope.launch {
            val list = widgetConfigs.value
            val currentIndex = list.indexOfFirst { it.appWidgetId == config.appWidgetId }
            if (currentIndex >= 0 && currentIndex < list.size - 1) {
                val nextConfig = list[currentIndex + 1]
                val updatedNext = nextConfig.copy(sortOrder = currentIndex)
                val updatedCurrent = config.copy(sortOrder = currentIndex + 1)
                repository.insertWidgetConfigs(listOf(updatedNext, updatedCurrent))
                _statusMessage.value = "تم تحريك الودجت للأسفل ⬇️"
            }
        }
    }

    fun sortWidgetsNewestFirst() {
        viewModelScope.launch {
            val list = widgetConfigs.value.sortedByDescending { it.createdAt }
            val reordered = list.mapIndexed { index, item -> item.copy(sortOrder = index) }
            repository.insertWidgetConfigs(reordered)
            _statusMessage.value = "تم ترتيب الودجات: الأحدث أولاً"
        }
    }

    fun sortWidgetsOldestFirst() {
        viewModelScope.launch {
            val list = widgetConfigs.value.sortedBy { it.createdAt }
            val reordered = list.mapIndexed { index, item -> item.copy(sortOrder = index) }
            repository.insertWidgetConfigs(reordered)
            _statusMessage.value = "تم ترتيب الودجات: الأقدم أولاً"
        }
    }

    fun sortWidgetsAlphabetically() {
        viewModelScope.launch {
            val list = widgetConfigs.value.sortedBy { it.name }
            val reordered = list.mapIndexed { index, item -> item.copy(sortOrder = index) }
            repository.insertWidgetConfigs(reordered)
            _statusMessage.value = "تم ترتيب الودجات: أبجدياً"
        }
    }

    fun deleteWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch {
            repository.deleteWidgetConfig(config.appWidgetId)
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تم حذف إعدادات الودجت"
        }
    }

    // Custom Font Operations
    fun importCustomFont(uri: android.net.Uri, fileName: String, onImported: ((com.example.data.model.CustomFontEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.importCustomFont(uri, fileName)
            if (result != null) {
                _statusMessage.value = "تم استيراد الخط '${result.name}' بنجاح"
                onImported?.invoke(result)
            } else {
                _statusMessage.value = "فشل استيراد ملف الخط"
            }
        }
    }

    fun deleteCustomFont(font: com.example.data.model.CustomFontEntity) {
        viewModelScope.launch {
            repository.deleteCustomFont(font)
            _statusMessage.value = "تم حذف الخط '${font.name}'"
        }
    }

    fun triggerWidgetRefreshBroadcast(appWidgetId: Int? = null) {
        if (appWidgetId != null) {
            WidgetManagerHelper.updateSingleWidget(getApplication(), appWidgetId)
        } else {
            WidgetManagerHelper.updateAllWidgets(getApplication())
        }
        _statusMessage.value = "تم إرسال أمر التحديث للودجت"
    }

    // Backup & Import/Export
    suspend fun exportDataJson(): String {
        return repository.exportToJson()
    }

    suspend fun exportToFileUri(uri: android.net.Uri): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val json = repository.exportToJson()
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            _statusMessage.value = "تم حفظ ملف النسخة الاحتياطية بنجاح 💾"
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _statusMessage.value = "فشل حفظ الملف: ${e.message}"
            false
        }
    }

    suspend fun importFromFileUri(uri: android.net.Uri): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val jsonString = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return@withContext false

            val result = repository.importFromJson(jsonString)
            if (result) {
                WidgetManagerHelper.updateAllWidgets(getApplication())
                _statusMessage.value = "تم استيراد النسخة الاحتياطية من الملف بنجاح! 📂"
            } else {
                _statusMessage.value = "صيغة الملف غير صالحة أو تالفة"
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            _statusMessage.value = "فشل قراءة الملف: ${e.message}"
            false
        }
    }

    suspend fun importDataJson(jsonString: String): Boolean {
        val result = repository.importFromJson(jsonString)
        if (result) {
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تم استيراد البيانات والودجت بنجاح"
        } else {
            _statusMessage.value = "حدث خطأ أثناء استيراد الملف"
        }
        return result
    }

    fun resetDataToDefaults() {
        viewModelScope.launch {
            repository.resetToSampleData()
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تمت إعادة تعيين البيانات التجريبية"
        }
    }

    suspend fun getItemsForWidget(config: WidgetConfigEntity): List<ContentItemEntity> {
        return repository.getItemsForWidgetConfig(config)
    }

    // ==========================================
    // PRAYER-LINKED TASKS (المهام المربوطة بالصلوات)
    // ==========================================
    val prayerTasks: StateFlow<List<com.example.data.model.PrayerTaskEntity>> = repository.allPrayerTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPrayerTask(title: String, prayerKey: String, offsetMinutes: Int, category: String = "عام") {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val task = com.example.data.model.PrayerTaskEntity(
                    title = title.trim(),
                    prayerKey = prayerKey,
                    offsetMinutes = offsetMinutes,
                    category = category
                )
                repository.insertPrayerTask(task)
                _statusMessage.value = "تمت جدولة المهمة بنجاح مع صلاة $prayerKey ⏱️"
            }
        }
    }

    fun togglePrayerTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.togglePrayerTask(taskId, isCompleted)
        }
    }

    fun deletePrayerTask(taskId: Long) {
        viewModelScope.launch {
            repository.deletePrayerTask(taskId)
            _statusMessage.value = "تم حذف المهمة"
        }
    }

    // ==========================================
    // SPIRITUAL HABITS (مُتتبع العادات الروحية)
    // ==========================================
    private val todayDateString: String
        get() {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            return sdf.format(java.util.Date())
        }

    val todayHabits: StateFlow<com.example.data.model.SpiritualHabitEntity?> = repository.getHabitForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateTodayHabit(updater: (com.example.data.model.SpiritualHabitEntity) -> com.example.data.model.SpiritualHabitEntity) {
        viewModelScope.launch {
            val current = todayHabits.value ?: com.example.data.model.SpiritualHabitEntity(dateString = todayDateString)
            val updated = updater(current)
            repository.saveHabit(updated)
        }
    }

    // ==========================================
    // THE HOLY QURAN & TAFSIR (المصحف والتفسير)
    // ==========================================
    val quranSurahs: List<com.example.data.quran.Surah> = com.example.data.quran.QuranDataProvider.surahs

    private val _selectedSurahNumber = MutableStateFlow(1)
    val selectedSurahNumber: StateFlow<Int> = _selectedSurahNumber.asStateFlow()

    private val _selectedAyahForTafsir = MutableStateFlow<com.example.data.quran.Ayah?>(null)
    val selectedAyahForTafsir: StateFlow<com.example.data.quran.Ayah?> = _selectedAyahForTafsir.asStateFlow()

    val currentSurahAyahs: StateFlow<List<com.example.data.quran.Ayah>> = _selectedSurahNumber.map { num ->
        com.example.data.quran.QuranDataProvider.getAyahsForSurah(num)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.quran.QuranDataProvider.getAyahsForSurah(1))

    val quranBookmarks: StateFlow<List<com.example.data.model.QuranBookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSurah(surahNumber: Int) {
        _selectedSurahNumber.value = surahNumber
    }

    fun showTafsirForAyah(ayah: com.example.data.quran.Ayah?) {
        _selectedAyahForTafsir.value = ayah
    }

    fun addBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, note: String = "") {
        viewModelScope.launch {
            val bookmark = com.example.data.model.QuranBookmarkEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                note = note
            )
            repository.insertBookmark(bookmark)
            _statusMessage.value = "تم حفظ الفاصلة المرجعية في سورة $surahName (آية $ayahNumber) 🔖"
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
            _statusMessage.value = "تمت إزالة الفاصلة المرجعية"
        }
    }

    // ==========================================
    // ISLAMIC WISDOM & AI ASSISTANT (مساعد بيان الذكي)
    // ==========================================
    private val _wisdomSearchQuery = MutableStateFlow("")
    val wisdomSearchQuery: StateFlow<String> = _wisdomSearchQuery.asStateFlow()

    val wisdomResults: StateFlow<List<com.example.data.ai.WisdomTopic>> = _wisdomSearchQuery.map { query ->
        com.example.data.ai.IslamicWisdomAssistant.searchWisdom(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.ai.IslamicWisdomAssistant.topics)

    fun setWisdomQuery(query: String) {
        _wisdomSearchQuery.value = query
    }
}
