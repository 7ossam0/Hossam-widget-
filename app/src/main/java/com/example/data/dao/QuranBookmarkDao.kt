package com.example.data.dao

import androidx.room.*
import com.example.data.model.QuranBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE id = :bookmarkId")
    suspend fun deleteById(bookmarkId: Long)
}
