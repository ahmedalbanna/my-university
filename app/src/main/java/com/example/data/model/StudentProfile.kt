package com.example.data.model

/**
 * Represents the profile data for a student.
 * 
 * @property name Full name of the student.
 * @property studentId Unique student identification number.
 * @property university Name of the enrolled university.
 * @property faculty Name of the enrolled faculty.
 * @property major Specialization or major field of study.
 * @property level Current academic level (1-5).
 * @property semester Current academic semester (1 or 2).
 * @property gpa Current Grade Point Average.
 * @property balance Financial balance in the student's wallet.
 */
data class StudentProfile(
    val name: String = "أحمد محمد العباسي",
    val studentId: String = "2024-CSC-88412",
    val university: String = "جامعة الملك سعود",
    val faculty: String = "كلية الحاسبات والمعلومات",
    val major: String = "علوم الحاسب والذكاء الاصطناعي",
    val level: Int = 3,
    val semester: Int = 1,
    val gpa: Double = 3.84,
    val balance: Double = 2450.00
)
