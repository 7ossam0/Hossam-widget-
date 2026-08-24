package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spiritual_habits")
data class SpiritualHabitEntity(
    @PrimaryKey
    val dateString: String, // YYYY-MM-DD
    val fajrDone: Boolean = false,
    val dhuhrDone: Boolean = false,
    val asrDone: Boolean = false,
    val maghribDone: Boolean = false,
    val ishaDone: Boolean = false,
    val quranDone: Boolean = false,
    val quranPages: Int = 0,
    val morningAthkarDone: Boolean = false,
    val eveningAthkarDone: Boolean = false,
    val sunnahDone: Boolean = false
)
