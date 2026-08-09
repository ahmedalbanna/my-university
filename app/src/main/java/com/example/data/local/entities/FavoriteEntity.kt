package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "BOOK", "PROJECT", "QUESTION", "LIBRARY"
    val title: String,
    val subtitle: String,
    val faculty: String,
    val tag: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
