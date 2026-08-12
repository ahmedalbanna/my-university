package com.example.data.model

/**
 * Represents an academic level in the university system.
 * 
 * @property levelNumber The numeric representation of the level (e.g., 1 to 5).
 * @property titleAr The title of the level in Arabic (e.g., "المستوى الثالث").
 * @property subtitleAr A secondary description or subtitle for the level.
 */
data class AcademicLevel(
    val levelNumber: Int,
    val titleAr: String,
    val subtitleAr: String
)
