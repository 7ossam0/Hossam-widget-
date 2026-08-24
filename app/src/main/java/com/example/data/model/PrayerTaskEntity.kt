package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_tasks")
data class PrayerTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prayerKey: String, // FAJR, DHUHR, ASR, MAGHRIB, ISHA
    val offsetMinutes: Int, // e.g. +20 (after), -15 (before)
    val isCompleted: Boolean = false,
    val category: String = "عام",
    val dateString: String = "", // YYYY-MM-DD or empty for daily
    val createdAt: Long = System.currentTimeMillis()
)
