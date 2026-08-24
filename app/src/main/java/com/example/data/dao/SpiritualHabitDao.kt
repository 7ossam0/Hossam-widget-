package com.example.data.dao

import androidx.room.*
import com.example.data.model.SpiritualHabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpiritualHabitDao {
    @Query("SELECT * FROM spiritual_habits WHERE dateString = :dateStr LIMIT 1")
    fun getHabitForDate(dateStr: String): Flow<SpiritualHabitEntity?>

    @Query("SELECT * FROM spiritual_habits ORDER BY dateString DESC LIMIT 30")
    fun getRecentHabits(): Flow<List<SpiritualHabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHabit(habit: SpiritualHabitEntity)
}
