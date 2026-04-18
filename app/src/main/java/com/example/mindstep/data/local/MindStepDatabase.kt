package com.example.mindstep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EntryEntity::class, EntrySettings::class], version = 3, exportSchema = false)
abstract class MindStepDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: MindStepDatabase? = null

        fun getDatabase(context: Context): MindStepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindStepDatabase::class.java,
                    "mindstep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
