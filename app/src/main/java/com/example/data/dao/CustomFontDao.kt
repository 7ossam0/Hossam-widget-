package com.example.data.dao

import androidx.room.*
import com.example.data.model.CustomFontEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFontDao {
    @Query("SELECT * FROM custom_fonts ORDER BY createdAt DESC")
    fun getAllFonts(): Flow<List<CustomFontEntity>>

    @Query("SELECT * FROM custom_fonts ORDER BY createdAt DESC")
    suspend fun getAllFontsList(): List<CustomFontEntity>

    @Query("SELECT * FROM custom_fonts WHERE id = :id")
    suspend fun getFontById(id: Long): CustomFontEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFont(font: CustomFontEntity): Long

    @Delete
    suspend fun deleteFont(font: CustomFontEntity)

    @Query("DELETE FROM custom_fonts WHERE id = :id")
    suspend fun deleteFontById(id: Long)
}
