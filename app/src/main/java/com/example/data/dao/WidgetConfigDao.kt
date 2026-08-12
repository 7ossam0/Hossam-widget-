package com.example.data.dao

import androidx.room.*
import com.example.data.model.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs ORDER BY updatedAt DESC")
    fun getAllWidgetConfigs(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs ORDER BY updatedAt DESC")
    suspend fun getAllWidgetConfigsList(): List<WidgetConfigEntity>

    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun getWidgetConfigById(appWidgetId: Int): WidgetConfigEntity?

    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId")
    fun getWidgetConfigByIdFlow(appWidgetId: Int): Flow<WidgetConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfig(config: WidgetConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfigs(configs: List<WidgetConfigEntity>)

    @Update
    suspend fun updateWidgetConfig(config: WidgetConfigEntity)

    @Delete
    suspend fun deleteWidgetConfig(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun deleteWidgetConfigById(appWidgetId: Int)

    @Query("DELETE FROM widget_configs")
    suspend fun deleteAllWidgetConfigs()
}
