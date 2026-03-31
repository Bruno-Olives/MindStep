package com.example.mindstep.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mood: Int,
    val anxiety: Int,
    val sleep: Int,
    val steps: Int,
    val waterGlasses: Int,
    val notes: String
)

