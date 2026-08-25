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
/**
 * Single source of truth for defaults — P0-5 fix.
 * Previously defaults differed between this file (Saudi university, 2,450 balance)
 * and JamaatyRepository.getSavedProfile() (Sana'a, 250,000). Now unified to Yemeni context.
 */
data class StudentProfile(
    val name: String = Default.name,
    val studentId: String = Default.studentId,
    val university: String = Default.university,
    val faculty: String = Default.faculty,
    val major: String = Default.major,
    val level: Int = Default.level,
    val semester: Int = Default.semester,
    val gpa: Double = Default.gpa,
    val balance: Double = Default.balance
) {
    companion object Default {
        const val name = "أحمد عبدالله البنّاء"
        const val studentId = "2024-YEM-88412"
        const val university = "جامعة صنعاء"
        const val faculty = "كلية الحاسوب وتكنولوجيا المعلومات"
        const val major = "علوم الحاسوب والذكاء الاصطناعي"
        const val level = 3
        const val semester = 1
        const val gpa = 3.84
        const val balance = 250000.00
    }
}
