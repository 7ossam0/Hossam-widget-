package com.example.data.dao

import androidx.room.*
import com.example.data.model.PrayerTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTaskDao {
    @Query("SELECT * FROM prayer_tasks ORDER BY isCompleted ASC, prayerKey ASC, offsetMinutes ASC")
    fun getAllTasks(): Flow<List<PrayerTaskEntity>>

    @Query("SELECT * FROM prayer_tasks WHERE isCompleted = 0 ORDER BY prayerKey ASC, offsetMinutes ASC")
    fun getPendingTasks(): Flow<List<PrayerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PrayerTaskEntity): Long

    @Update
    suspend fun updateTask(task: PrayerTaskEntity)

    @Delete
    suspend fun deleteTask(task: PrayerTaskEntity)

    @Query("DELETE FROM prayer_tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("UPDATE prayer_tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
}
