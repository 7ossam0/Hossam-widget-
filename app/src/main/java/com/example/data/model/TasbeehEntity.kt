package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbeeh_items")
data class TasbeehEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val currentCount: Int = 0,
    val targetCount: Int = 33, // 33, 99, 100, or 0 for unlimited
    val totalLifetimeCount: Long = 0,
    val colorHex: String = "#00E5FF",
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0
)
