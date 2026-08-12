package com.example.data.model

/**
 * Information about a specific academic course and its available resources.
 * 
 * @property name Name of the course.
 * @property hasProjects Whether the course has associated graduation projects.
 * @property hasResearch Whether the course has research papers or studies.
 * @property hasReferences Whether the course has external textbooks and references.
 */
data class CourseInfo(
    val name: String,
    val hasProjects: Boolean = false,
    val hasResearch: Boolean = false,
    val hasReferences: Boolean = true
)
