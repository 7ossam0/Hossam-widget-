package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.ContentItemDao
import com.example.data.dao.CustomFontDao
import com.example.data.dao.PrayerTaskDao
import com.example.data.dao.QuranBookmarkDao
import com.example.data.dao.SpiritualHabitDao
import com.example.data.dao.TasbeehDao
import com.example.data.dao.WidgetConfigDao
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.data.model.CustomFontEntity
import com.example.data.model.PrayerTaskEntity
import com.example.data.model.QuranBookmarkEntity
import com.example.data.model.SpiritualHabitEntity
import com.example.data.model.TasbeehEntity
import com.example.data.model.WidgetConfigEntity

@Database(
    entities = [
        CategoryEntity::class,
        ContentItemEntity::class,
        WidgetConfigEntity::class,
        CustomFontEntity::class,
        TasbeehEntity::class,
        PrayerTaskEntity::class,
        SpiritualHabitEntity::class,
        QuranBookmarkEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun contentItemDao(): ContentItemDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun customFontDao(): CustomFontDao
    abstract fun tasbeehDao(): TasbeehDao
    abstract fun prayerTaskDao(): PrayerTaskDao
    abstract fun spiritualHabitDao(): SpiritualHabitDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bayan_quran_app_v8.db"
                )
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


