package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.ContentItemDao
import com.example.data.dao.CustomFontDao
import com.example.data.dao.TasbeehDao
import com.example.data.dao.WidgetConfigDao
import com.example.data.model.CategoryEntity
import com.example.data.model.ContentItemEntity
import com.example.data.model.CustomFontEntity
import com.example.data.model.TasbeehEntity
import com.example.data.model.WidgetConfigEntity

@Database(
    entities = [
        CategoryEntity::class,
        ContentItemEntity::class,
        WidgetConfigEntity::class,
        CustomFontEntity::class,
        TasbeehEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun contentItemDao(): ContentItemDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun customFontDao(): CustomFontDao
    abstract fun tasbeehDao(): TasbeehDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `tasbeeh_items`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tasbeeh_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `currentCount` INTEGER NOT NULL,
                        `targetCount` INTEGER NOT NULL,
                        `totalLifetimeCount` INTEGER NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `soundEnabled` INTEGER NOT NULL,
                        `hapticEnabled` INTEGER NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "widget_studio_v8.db"
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

