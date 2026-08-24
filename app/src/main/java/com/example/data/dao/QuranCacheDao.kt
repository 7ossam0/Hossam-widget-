package com.example.data.dao

import androidx.room.*
import com.example.data.model.QuranSurahCacheEntity

@Dao
interface QuranCacheDao {
    @Query("SELECT * FROM quran_surah_cache WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getSurahCache(surahNumber: Int): QuranSurahCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahCache(cache: QuranSurahCacheEntity)

    @Query("DELETE FROM quran_surah_cache WHERE surahNumber = :surahNumber")
    suspend fun deleteSurahCache(surahNumber: Int)
}
