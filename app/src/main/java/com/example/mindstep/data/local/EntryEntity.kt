package com.example.mindstep.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val mood: Int,
    val anxiety: Int,
    val sleep: Int,
    val steps: Int,
    val waterGlasses: Int,
    val notes: String
)

@Entity(tableName = "settings")
data class EntrySettings(
    @PrimaryKey
    val id: Int = 1,
    val reminderWater: Boolean = false,
    val waterInterval: Int = 120,
    val reminderMeditation: Boolean = false,
    val meditationTime: String = "08:00",
    val reduceAnimations: Boolean = false,
    val hapticFeedback: Boolean = true,
    val voiceInput: Boolean = true,
    val darkMode: Boolean? = null
)
