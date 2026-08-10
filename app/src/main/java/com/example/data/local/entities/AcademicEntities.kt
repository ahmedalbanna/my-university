package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val academicYear: String
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val semesterId: String,
    val name: String,
    val creditHours: Int,
    val grade: Double // 0.0 to 4.0 or 5.0 depending on system, let's assume 4.0 scale
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: String, // "Saturday", "Sunday", etc.
    val subject: String,
    val doctor: String,
    val startTime: String, // e.g. "08:00"
    val endTime: String,   // e.g. "10:00"
    val room: String,
    val color: Int // Color as Int for UI
)
