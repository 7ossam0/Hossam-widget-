package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.TasbeehEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_items ORDER BY orderIndex ASC, id ASC")
    fun getAllTasbeeh(): Flow<List<TasbeehEntity>>

    @Query("SELECT * FROM tasbeeh_items ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllTasbeehList(): List<TasbeehEntity>

    @Query("SELECT * FROM tasbeeh_items WHERE id = :id LIMIT 1")
    suspend fun getTasbeehById(id: Long): TasbeehEntity?

    @Query("SELECT * FROM tasbeeh_items WHERE isFavorite = 1 ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getActiveTasbeeh(): TasbeehEntity?

    @Query("SELECT * FROM tasbeeh_items ORDER BY orderIndex ASC, id ASC LIMIT 1")
    suspend fun getFirstTasbeeh(): TasbeehEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TasbeehEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TasbeehEntity>)

    @Update
    suspend fun update(item: TasbeehEntity)

    @Delete
    suspend fun delete(item: TasbeehEntity)

    @Query("UPDATE tasbeeh_items SET currentCount = currentCount + 1, totalLifetimeCount = totalLifetimeCount + 1 WHERE id = :id")
    suspend fun incrementCount(id: Long)

    @Query("UPDATE tasbeeh_items SET currentCount = 0 WHERE id = :id")
    suspend fun resetCount(id: Long)

    @Query("UPDATE tasbeeh_items SET isFavorite = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE tasbeeh_items SET isFavorite = 1 WHERE id = :id")
    suspend fun setActiveFlag(id: Long)

    @Transaction
    suspend fun selectActiveTasbeeh(id: Long) {
        clearActiveFlag()
        setActiveFlag(id)
    }

    @Query("SELECT COUNT(*) FROM tasbeeh_items")
    suspend fun getCount(): Int
}
