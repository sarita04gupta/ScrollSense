package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyScrollSummaryEntity
import com.example.data.model.ReelScrollEntity

@Database(
    entities = [
        ReelScrollEntity::class,
        DailyScrollSummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ScrollSenseDatabase : RoomDatabase() {
    abstract fun scrollSenseDao(): ScrollSenseDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollSenseDatabase? = null

        fun getInstance(context: Context): ScrollSenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollSenseDatabase::class.java,
                    "scrollsense_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
