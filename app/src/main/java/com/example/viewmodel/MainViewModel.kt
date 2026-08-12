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

    private val _selectedCategoryIdFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryIdFilter: StateFlow<Long?> = _selectedCategoryIdFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
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
    fun addContentItem(title: String, body: String, categoryId: Long?, isFavorite: Boolean = false) {
        viewModelScope.launch {
            if (body.isNotBlank()) {
                val item = ContentItemEntity(
                    title = title.trim(),
                    body = body.trim(),
                    categoryId = categoryId,
                    isFavorite = isFavorite
                )
                repository.insertContentItem(item)
                WidgetManagerHelper.updateAllWidgets(getApplication())
                _statusMessage.value = "تم إضافة النص بنجاح"
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

    fun copyWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch {
            val newAppWidgetId = (1000..9999).random()
            val copied = config.copy(
                appWidgetId = newAppWidgetId,
                name = "${config.name} (نسخة)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertWidgetConfig(copied)
            _statusMessage.value = "تم إنشاء نسخة من الودجت"
        }
    }

    fun createNewStandaloneWidgetConfig(name: String, categoryId: Long?): WidgetConfigEntity {
        val newAppWidgetId = (10000..99999).random()
        return WidgetConfigEntity(
            appWidgetId = newAppWidgetId,
            name = if (name.isNotBlank()) name else "ودجت جديد $newAppWidgetId",
            categoryId = categoryId
        )
    }

    fun deleteWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch {
            repository.deleteWidgetConfig(config.appWidgetId)
            WidgetManagerHelper.updateAllWidgets(getApplication())
            _statusMessage.value = "تم حذف إعدادات الودجت"
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
}
