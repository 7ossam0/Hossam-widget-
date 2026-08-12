package com.example.data.dao

import androidx.room.*
import com.example.data.model.ContentItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentItemDao {
    @Query("SELECT * FROM content_items ORDER BY sortOrder ASC, id DESC")
    fun getAllContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllContentItemsList(): List<ContentItemEntity>

    @Query("SELECT * FROM content_items WHERE isFavorite = 1 ORDER BY sortOrder ASC, id DESC")
    fun getFavoriteContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE categoryId = :categoryId AND isActive = 1 ORDER BY sortOrder ASC, id DESC")
    fun getContentItemsByCategory(categoryId: Long): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE categoryId = :categoryId AND isActive = 1 ORDER BY sortOrder ASC, id DESC")
    suspend fun getContentItemsByCategoryList(categoryId: Long): List<ContentItemEntity>

    @Query("SELECT * FROM content_items WHERE isActive = 1 ORDER BY sortOrder ASC, id DESC")
    suspend fun getActiveContentItemsList(): List<ContentItemEntity>

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getContentItemById(id: Long): ContentItemEntity?

    @Query("SELECT * FROM content_items WHERE id IN (:ids) AND isActive = 1")
    suspend fun getContentItemsByIds(ids: List<Long>): List<ContentItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItems(items: List<ContentItemEntity>)

    @Update
    suspend fun updateContentItem(item: ContentItemEntity)

    @Delete
    suspend fun deleteContentItem(item: ContentItemEntity)

    @Query("DELETE FROM content_items WHERE id = :id")
    suspend fun deleteContentItemById(id: Long)

    @Query("DELETE FROM content_items")
    suspend fun deleteAllContentItems()
}
