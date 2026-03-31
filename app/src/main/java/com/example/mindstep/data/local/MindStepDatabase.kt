package com.example.mindstep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EntryEntity::class], version = 1, exportSchema = false)
abstract class MindStepDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: MindStepDatabase? = null

        fun getDatabase(context: Context): MindStepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindStepDatabase::class.java,
                    "mindstep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

