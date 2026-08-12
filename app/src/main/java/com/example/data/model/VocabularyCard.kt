package com.example.data.model

/**
 * Represents a flashcard for academic terms and vocabulary.
 * 
 * @property id Unique identifier.
 * @property termAr The term in Arabic.
 * @property termEn The term in English.
 * @property definitionAr Definition in Arabic.
 * @property definitionEn Definition in English.
 * @property faculty The faculty where this term is commonly used.
 * @property example An example usage of the term.
 */
data class VocabularyCard(
    val id: String,
    val termAr: String,
    val termEn: String,
    val definitionAr: String,
    val definitionEn: String,
    val faculty: String,
    val example: String
)
