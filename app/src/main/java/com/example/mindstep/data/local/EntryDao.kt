package com.example.mindstep.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries ORDER BY createdAt DESC, id DESC")
    fun getAllEntries(): Flow<List<EntryEntity>>

    @Query("DELETE FROM entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}
