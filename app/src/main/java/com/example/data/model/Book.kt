package com.example.data.model

/**
 * Categories for academic books and resources.
 */
enum class BookCategory(val label: String) {
    COURSE_BOOK("كتاب مقرر"),
    MAIN_REFERENCE("مرجع أساسي"),
    SUMMARY("ملخص دراسي"),
    RESEARCH("بحث علمي")
}

/**
 * Represents a textbook or academic reference.
 * 
 * @property id Unique identifier for the book.
 * @property title The title of the book.
 * @property author The author or professor of the book.
 * @property faculty The faculty this book belongs to.
 * @property level The academic level (1-5).
 * @property semester The semester (1 or 2).
 * @property pages Number of pages.
 * @property category The category of the resource (using [BookCategory]).
 * @property rating User rating for the book.
 * @property description A brief summary of the book content.
 * @property fileSize Size of the file (e.g., "14.2 MB").
 * @property downloadUrl URL to download the resource.
 * @property isAvailable Availability status.
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val faculty: String,
    val level: Int,
    val semester: Int,
    val pages: Int,
    val category: String, // Kept as String for compatibility with existing data but can use BookCategory
    val rating: Float,
    val description: String,
    val fileSize: String,
    val downloadUrl: String = "https://example.com/file.pdf",
    val isAvailable: Boolean = true
)
