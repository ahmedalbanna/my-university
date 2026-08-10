package com.example.data.model

data class AcademicLevel(
    val levelNumber: Int, // 1 to 5
    val titleAr: String,
    val subtitleAr: String
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val faculty: String,
    val level: Int,
    val semester: Int, // 1 or 2
    val pages: Int,
    val category: String, // "كتاب مقرر", "مرجع أساسي", "ملخص دراسي"
    val rating: Float,
    val description: String,
    val fileSize: String,
    val isAvailable: Boolean = true
)

data class Project(
    val id: String,
    val title: String,
    val studentName: String,
    val supervisor: String,
    val faculty: String,
    val year: String,
    val level: Int,
    val summary: String,
    val tags: List<String>,
    val fileFormat: String = "PDF",
    val fileSize: String = "4.2 MB",
    val downloadsCount: Int = 142
)

data class QuestionVersion(
    val version: String,
    val date: String,
    val modifiedBy: String,
    val changeNote: String
)

data class Question(
    val id: String,
    val questionText: String,
    val subject: String,
    val faculty: String,
    val level: Int,
    val semester: Int,
    val difficulty: String, // "سهل", "متوسط", "صعب"
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val tags: List<String>,
    val versionHistory: List<QuestionVersion> = emptyList()
)

data class VocabularyCard(
    val id: String,
    val termAr: String,
    val termEn: String,
    val definitionAr: String,
    val definitionEn: String,
    val faculty: String,
    val example: String
)

data class WorldLibrary(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val category: String,
    val url: String,
    val description: String
)

data class ExamResult(
    val code: String,
    val name: String,
    val credits: Int,
    val grade: String,
    val score: Int
)

data class StudentProfile(
    val name: String = "أحمد محمد العباسي",
    val studentId: String = "2024-CSC-88412",
    val university: String = "جامعة الملك سعود",
    val faculty: String = "كلية الحاسبات والمعلومات",
    val major: String = "علوم الحاسب والذكاء الاصطناعي",
    val level: Int = 3,
    val gpa: Double = 3.84,
    val balance: Double = 2450.00
)
