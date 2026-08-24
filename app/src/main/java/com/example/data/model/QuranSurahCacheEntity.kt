package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_surah_cache")
data class QuranSurahCacheEntity(
    @PrimaryKey
    val surahNumber: Int,
    val ayahsJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)
