package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.PreferenceEntity
import com.example.data.local.entities.SemesterEntity
import com.example.data.local.entities.CourseEntity
import com.example.data.local.entities.ScheduleEntity
import com.example.data.model.Book
import com.example.data.model.ExamResult
import com.example.data.model.Project
import com.example.data.model.Question
import com.example.data.model.QuestionVersion
import com.example.data.model.StudentProfile
import com.example.data.model.VocabularyCard
import com.example.data.model.WorldLibrary
import kotlinx.coroutines.flow.Flow

class JamaatyRepository(private val appDao: AppDao) {

    val favorites: Flow<List<FavoriteEntity>> = appDao.getAllFavorites()
    val payments: Flow<List<PaymentEntity>> = appDao.getAllPayments()
    val notifications: Flow<List<NotificationEntity>> = appDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = appDao.getUnreadNotificationsCount()

    suspend fun addNotification(notification: NotificationEntity) {
        appDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: String) {
        appDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        appDao.markAllAsRead()
    }

    suspend fun deleteNotification(notification: NotificationEntity) {
        appDao.deleteNotification(notification)
    }

    suspend fun deleteAllNotifications() {
        appDao.deleteAllNotifications()
    }

    // Academic / GPA
    val semesters: Flow<List<SemesterEntity>> = appDao.getAllSemesters()

    fun getCoursesForSemester(semesterId: String): Flow<List<CourseEntity>> = appDao.getCoursesForSemester(semesterId)

    suspend fun addSemester(semester: SemesterEntity) {
        appDao.insertSemester(semester)
    }

    suspend fun addCourse(course: CourseEntity) {
        appDao.insertCourse(course)
    }

    suspend fun deleteSemester(semester: SemesterEntity) {
        appDao.deleteCoursesForSemester(semester.id)
        appDao.deleteSemester(semester)
    }

    suspend fun deleteCourse(course: CourseEntity) {
        appDao.deleteCourse(course)
    }

    // Schedule
    val schedules: Flow<List<ScheduleEntity>> = appDao.getAllSchedules()

    suspend fun addSchedule(schedule: ScheduleEntity) {
        appDao.insertSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        appDao.deleteSchedule(schedule)
    }

    suspend fun deleteAllSchedules() {
        appDao.deleteAllSchedules()
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return appDao.getPreference("onboarding_completed") == "true"
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        appDao.insertPreference(PreferenceEntity("onboarding_completed", completed.toString()))
    }

    fun isFavorite(id: String): Flow<Boolean> = appDao.isFavorite(id)

    suspend fun toggleFavorite(
        id: String,
        type: String,
        title: String,
        subtitle: String,
        faculty: String,
        tag: String = "",
        isFav: Boolean
    ) {
        if (isFav) {
            appDao.deleteFavoriteById(id)
        } else {
            appDao.insertFavorite(
                FavoriteEntity(
                    id = id,
                    type = type,
                    title = title,
                    subtitle = subtitle,
                    faculty = faculty,
                    tag = tag
                )
            )
        }
    }

    suspend fun addPayment(payment: PaymentEntity) {
        appDao.insertPayment(payment)
    }

    suspend fun getSavedProfile(): StudentProfile {
        val name = appDao.getPreference("profile_name") ?: "أحمد عبدالله البنّاء"
        val studentId = appDao.getPreference("profile_id") ?: "2024-YEM-88412"
        val university = appDao.getPreference("profile_university") ?: "جامعة صنعاء"
        val faculty = appDao.getPreference("profile_faculty") ?: "كلية الحاسوب وتكنولوجيا المعلومات"
        val major = appDao.getPreference("profile_major") ?: "علوم الحاسوب والذكاء الاصطناعي"
        val level = appDao.getPreference("profile_level")?.toIntOrNull() ?: 3
        val gpa = appDao.getPreference("profile_gpa")?.toDoubleOrNull() ?: 3.84
        val balance = appDao.getPreference("profile_balance")?.toDoubleOrNull() ?: 250000.00
        return StudentProfile(
            name = name,
            studentId = studentId,
            university = university,
            faculty = faculty,
            major = major,
            level = level,
            gpa = gpa,
            balance = balance
        )
    }

    suspend fun saveStudentProfile(profile: StudentProfile) {
        appDao.insertPreference(PreferenceEntity("profile_name", profile.name))
        appDao.insertPreference(PreferenceEntity("profile_id", profile.studentId))
        appDao.insertPreference(PreferenceEntity("profile_university", profile.university))
        appDao.insertPreference(PreferenceEntity("profile_faculty", profile.faculty))
        appDao.insertPreference(PreferenceEntity("profile_major", profile.major))
        appDao.insertPreference(PreferenceEntity("profile_level", profile.level.toString()))
        appDao.insertPreference(PreferenceEntity("profile_gpa", profile.gpa.toString()))
        appDao.insertPreference(PreferenceEntity("profile_balance", profile.balance.toString()))
    }

    fun getGovernorates(): List<String> = listOf(
        "صنعاء",
        "عدن",
        "تعز",
        "إب",
        "ذمار",
        "حضرموت",
        "الحديدة",
        "عمران"
    )

    fun getUniversitiesForGovernorate(governorate: String): List<String> {
        return when (governorate) {
            "صنعاء" -> listOf("جامعة صنعاء", "الجامعة الإماراتية الدولية - صنعاء", "جامعة العلوم والتكنولوجيا - اليمن")
            "عدن" -> listOf("جامعة عدن")
            "تعز" -> listOf("جامعة تعز")
            "إب" -> listOf("جامعة إب")
            "ذمار" -> listOf("جامعة ذمار")
            "حضرموت" -> listOf("جامعة حضرموت", "جامعة سيئون")
            "الحديدة" -> listOf("جامعة الحديدة")
            "عمران" -> listOf("جامعة عمران")
            else -> getUniversities()
        }
    }

    fun getSemesters(): List<String> = listOf("الفصل الأول", "الفصل الثاني", "الفصل الصيفي")

    data class CourseInfo(
        val name: String,
        val hasProjects: Boolean = false,
        val hasResearch: Boolean = false,
        val hasExams: Boolean = true,
        val hasReferences: Boolean = true
    )

    fun getCourseDetails(courseName: String): CourseInfo {
        return when {
            courseName.contains("مشروع") || courseName.contains("تخرج") || courseName.contains("عملي") -> 
                CourseInfo(courseName, hasProjects = true, hasResearch = true)
            courseName.contains("ندوة") || courseName.contains("بحث") -> 
                CourseInfo(courseName, hasResearch = true)
            else -> CourseInfo(courseName)
        }
    }

    fun getCourses(faculty: String, major: String, level: Int, semester: String): List<String> {
        return when (faculty) {
            "كلية الحاسوب وتكنولوجيا المعلومات" -> when (semester) {
                "الفصل الأول" -> when (level) {
                    1 -> listOf("تفاضل وتكامل 1", "مقدمة حاسوب", "لغة عربية 1", "ثقافة إسلامية 1")
                    2 -> listOf("هياكل بيانات", "برمجة كائنية 1", "رياضيات متقطعة")
                    else -> listOf("هندسة برمجيات", "قواعد بيانات 2", "أمن شبكات")
                }
                "الفصل الثاني" -> when (level) {
                    1 -> listOf("تفاضل وتكامل 2", "برمجة 1", "لغة إنجليزية 1")
                    2 -> listOf("خوارزميات", "نظم تشغيل", "تصميم منطقي")
                    else -> listOf("ذكاء اصطناعي", "معالجة صور", "تنقيب بيانات")
                }
                else -> listOf("دورة تدريبية صيفية", "مشروع عملي")
            }
            "كلية الهندسة" -> listOf("فيزياء هندسية", "رسم هندسي", "ميكانيكا", "دوائر كهربائية")
            else -> listOf("مقرر عام 1", "مقرر عام 2", "بحث تخرج")
        }
    }

    fun getUniversities(): List<String> = listOf(
        "جامعة صنعاء",
        "جامعة عدن",
        "جامعة تعز",
        "جامعة إب",
        "جامعة ذمار",
        "جامعة حضرموت",
        "جامعة العلوم والتكنولوجيا - اليمن",
        "جامعة الحديدة",
        "الجامعة الإماراتية الدولية - صنعاء",
        "جامعة سيئون",
        "جامعة عمران"
    )

    fun getFaculties(): List<String> = listOf(
        "الكل",
        "كلية الحاسوب وتكنولوجيا المعلومات",
        "كلية الهندسة والبترول",
        "كلية الطب والعلوم الصحية",
        "كلية التجارة والاقتصاد",
        "كلية العلوم",
        "كلية الشريعة والقانون",
        "كلية الآداب والعلوم الإنسانية"
    )

    fun getMajorsForFaculty(faculty: String): List<String> {
        return when (faculty) {
            "كلية الحاسوب وتكنولوجيا المعلومات" -> listOf("علوم الحاسوب والذكاء الاصطناعي", "هندسة البرمجيات", "نظم المعلومات", "الأمن السيبراني والشبكات")
            "كلية الهندسة والبترول" -> listOf("الهندسة المدنية", "الهندسة المعمارية", "الهندسة الكهربائية", "هندسة النفط والغاز")
            "كلية الطب والعلوم الصحية" -> listOf("الطب والجراحة العامة", "طب وجراحة الأسنان", "الصيدلة الدوائية", "المختبرات الطبية")
            "كلية التجارة والاقتصاد" -> listOf("إدارة الأعمال", "المحاسبة والمراجعة", "العلوم المالية والمصرفية", "التسويق الرقمي")
            "كلية العلوم" -> listOf("الرياضيات والحاسوب", "الفيزياء التطبيقية", "الكيمياء الحيوية", "الأحياء والجيولوجيا")
            "كلية الشريعة والقانون" -> listOf("الشريعة والقانون", "القانون العام", "القانون الخاص")
            "كلية الآداب والعلوم الإنسانية" -> listOf("اللغة الإنجليزية والترجمة", "اللغة العربية وآدابها", "الإعلام والاتصال")
            else -> listOf("علوم الحاسوب والذكاء الاصطناعي", "هندسة البرمجيات", "إدارة الأعمال", "تخصص عام")
        }
    }

    fun getBooks(): List<Book> = listOf(
        // Level 1
        Book(
            id = "b101",
            title = "مقدمة في برمجة الحواسب وتراكيب البيانات",
            author = "د. صادق الشراجي",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 1,
            semester = 1,
            pages = 320,
            category = "كتاب مقرر",
            rating = 4.8f,
            description = "مرجع شامل لأساسيات الخوارزميات وتراكيب البيانات مع أمثلة تطبيقية للمناهج الجامعية اليمنية.",
            fileSize = "14.2 MB"
        ),
        Book(
            id = "b102",
            title = "الرياضيات الهندسية والتفاضل والتكامل I",
            author = "أ.د. عبدالكريم الكبسي",
            faculty = "كلية الهندسة والبترول",
            level = 1,
            semester = 1,
            pages = 410,
            category = "مرجع أساسي",
            rating = 4.6f,
            description = "شرح مبسط لقوانين التفاضل والتكامل والتطبيقات الفيزيائية والهندسية المتقدمة.",
            fileSize = "18.5 MB"
        ),
        Book(
            id = "b_ref_01",
            title = "المرجع الشامل في الشبكات اللاسلكية",
            author = "أندرو تانينباوم",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 3,
            semester = 1,
            pages = 850,
            category = "مرجع أساسي",
            rating = 5.0f,
            description = "أهم المراجع العالمية في علم الشبكات والبروتوكولات، مترجم للعربية.",
            fileSize = "45.1 MB"
        ),

        // Level 2
        Book(
            id = "b201",
            title = "قواعد البيانات ونظم إدارة المعلومات SQL",
            author = "د. محمد العمراني",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 2,
            semester = 1,
            pages = 350,
            category = "كتاب مقرر",
            rating = 4.7f,
            description = "تصميم قواعد البيانات العلاقاتية وتحسين الاستعلامات والربط المعقد في الأنظمة الجامعية.",
            fileSize = "11.4 MB"
        ),

        // Level 3
        Book(
            id = "b301",
            title = "هندسة البرمجيات وإدارة المشروعات التقنية",
            author = "د. وليد البعداني",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 3,
            semester = 1,
            pages = 310,
            category = "مرجع أساسي",
            rating = 4.9f,
            description = "منهجيات Agile و Scrum، دورة حياة تطوير البرمجيات، واختبار الأنظمة البرمجية.",
            fileSize = "9.8 MB"
        ),
        Book(
            id = "b_res_01",
            title = "أثر التحول الرقمي على التعليم الجامعي في اليمن",
            author = "د. أحمد البناء",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 3,
            semester = 1,
            pages = 45,
            category = "بحث علمي",
            rating = 4.9f,
            description = "بحث محكم يتناول التحديات والفرص للتعليم الإلكتروني في الجامعات الحكومية والخاصة.",
            fileSize = "2.4 MB"
        ),
        Book(
            id = "b302",
            title = "المحاسبة المتوسطة والتقارير المالية",
            author = "د. فؤاد راشد",
            faculty = "كلية التجارة والاقتصاد",
            level = 3,
            semester = 1,
            pages = 380,
            category = "كتاب مقرر",
            rating = 4.5f,
            description = "شرح مفصل لمعايير المحاسبة الدولية وتطبيقاتها في الشركات اليمنية.",
            fileSize = "15.2 MB"
        ),
        Book(
            id = "b401",
            title = "القانون الجنائي اليمني - القسم العام",
            author = "أ.د. محمد الشامي",
            faculty = "كلية الشريعة والقانون",
            level = 2,
            semester = 1,
            pages = 450,
            category = "كتاب مقرر",
            rating = 4.7f,
            description = "دراسة تحليلية لقانون الجرائم والعقوبات اليمني والنظريات العامة للجريمة.",
            fileSize = "22.1 MB"
        ),
        Book(
            id = "b501",
            title = "تكنولوجيا الاتصالات والشبكات الحديثة",
            author = "د. خليل المعمري",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 4,
            semester = 1,
            pages = 340,
            category = "مرجع أساسي",
            rating = 4.6f,
            description = "بروتوكولات الشبكات، الأمن السيبراني، وتقنيات الجيل الخامس والاتصالات اللاسلكية.",
            fileSize = "19.4 MB"
        )
    )

    fun getProjects(): List<Project> = listOf(
        Project(
            id = "p101",
            title = "نظام ذكي للتشخيص المبكر لأمراض العيون - جامعة صنعاء",
            studentName = "عبدالرحمن المطري وزملاؤه",
            supervisor = "د. عادل الصنعاني",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            year = "2025",
            level = 4,
            summary = "مشروع تخرج بكلية الحاسوب جامعة صنعاء يستخدم شبكات CNN المتقدمة لتحليل صور شبكية العين.",
            tags = listOf("ذكاء اصطناعي", "جامعة صنعاء", "طب رقمي")
        ),
        Project(
            id = "p102",
            title = "تصميم شبكة مياه ذكية ومستدامة - جامعة عدن",
            studentName = "فاطمة الزهراء اليافعي",
            supervisor = "أ.د. عبدالكريم الكبسي",
            faculty = "كلية الهندسة والبترول",
            year = "2024",
            level = 5,
            summary = "تحليل هيدروليكي واستخدام مستشعرات IoT للكشف الذكي عن التسربات.",
            tags = listOf("هندسة مدنية", "جامعة عدن", "إنترنت الأشياء")
        ),
        Project(
            id = "p103",
            title = "دراسة أثر الشمول المالي على التنمية الاقتصادية في اليمن",
            studentName = "محمد نبيل العبسي",
            supervisor = "د. محمد العمراني",
            faculty = "كلية التجارة والاقتصاد",
            year = "2025",
            level = 4,
            summary = "بحث علمي يتناول دور الخدمات المصرفية الرقمية في تعزيز الاقتصاد الوطني.",
            tags = listOf("اقتصاد", "جامعة تعز", "شمول مالي")
        ),
        Project(
            id = "p104",
            title = "تطوير تطبيق لإدارة المحاكم والتقاضي الإلكتروني",
            studentName = "هيثم القاضي",
            supervisor = "د. أحمد الشامي",
            faculty = "كلية الشريعة والقانون",
            year = "2024",
            level = 4,
            summary = "مشروع تقني قانوني يهدف لأتمتة الإجراءات القضائية في المحاكم اليمنية.",
            tags = listOf("قانون", "تقنية", "جامعة إب")
        )
    )

    fun getQuestions(): List<Question> = listOf(
        // Computer Science & IT
        Question(
            id = "q101",
            questionText = "ما هي التعقدية الزمانية لخوارزمية البحث الثنائي (Binary Search) في مصفوفة مرتبة؟",
            subject = "خوارزميات وتراكيب البيانات",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 2,
            semester = 1,
            difficulty = "متوسط",
            options = listOf("O(n)", "O(log n)", "O(n^2)", "O(1)"),
            correctIndex = 1,
            explanation = "البحث الثنائي يقوم بتقسيم مصفوفة البحث إلى النصف في كل خطوة، مما يجعله ذو كفاءة لوغاريتمية.",
            tags = listOf("#خوارزميات", "#ترم_أول", "#جامعة_صنعاء"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-09-10", "د. صادق الشراجي", "إضافة السؤال لبنك نماذج الاختبارات.")
            )
        ),
        Question(
            id = "q103",
            questionText = "ما هي وظيفة بروتوكول DHCP في شبكات الحاسوب؟",
            subject = "شبكات الحاسوب",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            level = 3,
            semester = 2,
            difficulty = "سهل",
            options = listOf("تشفير البيانات", "توزيع عناوين IP تلقائياً", "توجيه الرسائل البريدية", "ترجمة أسماء النطاقات"),
            correctIndex = 1,
            explanation = "يقوم بروتوكول DHCP بتخصيص عناوين IP وإعدادات الشبكة للأجهزة المنضمة للشبكة تلقائياً.",
            tags = listOf("#شبكات", "#جامعة_تعز", "#IT"),
            versionHistory = listOf(
                QuestionVersion("v1.1", "2026-02-15", "م. وليد البعداني", "تحديث الخيارات.")
            )
        ),

        // Engineering
        Question(
            id = "q102",
            questionText = "في قانون أوم، ما هي العلاقة بين التيار والجهد عند ثبوت المقاومة؟",
            subject = "الفيزياء الهندسية",
            faculty = "كلية الهندسة والبترول",
            level = 1,
            semester = 1,
            difficulty = "سهل",
            options = listOf("طردية", "عكسية", "لا توجد", "لوغاريتمية"),
            correctIndex = 0,
            explanation = "التيار يتناسب طردياً مع الجهد الكهربائي عند ثبوت قيمة المقاومة (V = I * R).",
            tags = listOf("#فيزياء", "#هندسة", "#قانون_أوم"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-10-01", "أ.د. الكبسي", "إدخال السؤال في بنك الأسئلة.")
            )
        ),
        Question(
            id = "q201",
            questionText = "ما هي المادة الأساسية المستخدمة في صناعة الخرسانة المسلحة لمقاومة قوى الشد؟",
            subject = "مواد البناء",
            faculty = "كلية الهندسة والبترول",
            level = 2,
            semester = 1,
            difficulty = "سهل",
            options = listOf("الرمل", "الأسمنت", "حديد التسليح", "الحصى"),
            correctIndex = 2,
            explanation = "الخرسانة تقاوم الضغط بشكل ممتاز ولكنها ضعيفة في الشد، لذا يضاف حديد التسليح لتحمل قوى الشد.",
            tags = listOf("#هندسة_مدنية", "#مواد_بناء", "#جامعة_عدن"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2024-11-20", "د. نبيل غانم", "إضافة سؤال لمقرر الخرسانة.")
            )
        ),

        // Medicine & Health Sciences
        Question(
            id = "q301",
            questionText = "ما هو العضو المسؤول عن تصفية الدم من الفضلات وإنتاج البول؟",
            subject = "علم وظائف الأعضاء (Physiology)",
            faculty = "كلية الطب والعلوم الصحية",
            level = 2,
            semester = 1,
            difficulty = "سهل",
            options = listOf("الكبد", "الطحال", "الكلية", "البنكرياس"),
            correctIndex = 2,
            explanation = "تقوم الكليتان بتصفية الدم وتنظيم توازن السوائل والأملاح في الجسم.",
            tags = listOf("#طب", "#Physiology", "#جامعة_إب"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-12-05", "د. أمة اللطيف الحيمي", "سؤال أساسي لطلاب الطب.")
            )
        ),
        Question(
            id = "q302",
            questionText = "أي من الفيتامينات التالية يساهم في عملية تخثر الدم؟",
            subject = "الكيمياء الحيوية",
            faculty = "كلية الطب والعلوم الصحية",
            level = 1,
            semester = 2,
            difficulty = "متوسط",
            options = listOf("Vitamin C", "Vitamin D", "Vitamin K", "Vitamin A"),
            correctIndex = 2,
            explanation = "فيتامين K ضروري لتصنيع بروتينات التخثر في الكبد.",
            tags = listOf("#Biochemistry", "#فيتامينات", "#طب_جامعة_ذمار")
        ),

        // Commerce & Economics
        Question(
            id = "q401",
            questionText = "ما هو المبدأ المحاسبي الذي ينص على تسجيل الإيرادات عند تحققها وليس عند استلام النقد؟",
            subject = "مبادئ المحاسبة",
            faculty = "كلية التجارة والاقتصاد",
            level = 1,
            semester = 1,
            difficulty = "متوسط",
            options = listOf("الأساس النقدي", "أساس الاستحقاق", "مبدأ التكلفة التاريخية", "مبدأ الحيطة والحذر"),
            correctIndex = 1,
            explanation = "أساس الاستحقاق يقضي بتسجيل العمليات المالية في الفترة التي حدثت فيها بغض النظر عن الحركة النقدية.",
            tags = listOf("#محاسبة", "#جامعة_صنعاء", "#تجارة"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-08-15", "د. محمد العمراني", "إضافة مقرر محاسبة 1.")
            )
        )
    )

    fun getVocabulary(): List<VocabularyCard> = listOf(
        VocabularyCard(
            id = "v1",
            termAr = "تراكيب البيانات",
            termEn = "Data Structures",
            definitionAr = "طريقة تنظيم وتخزين البيانات في الحاسوب لتسهيل الوصول إليها وتعديلها بكفاءة.",
            definitionEn = "A specialized format for organizing and storing data.",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            example = "Arrays and Linked Lists."
        ),
        VocabularyCard(
            id = "v2",
            termAr = "الذكاء الاصطناعي",
            termEn = "Artificial Intelligence",
            definitionAr = "محاكاة العمليات الذهنية البشرية بواسطة أنظمة الكمبيوتر.",
            definitionEn = "The simulation of human intelligence processes by machines.",
            faculty = "كلية الحاسوب وتكنولوجيا المعلومات",
            example = "Machine Learning, Neural Networks."
        ),
        VocabularyCard(
            id = "v3",
            termAr = "الهندسة المدنية",
            termEn = "Civil Engineering",
            definitionAr = "فرع هندسي يهتم بتصميم وبناء وصيانة المنشآت مثل الجسور والطرق.",
            definitionEn = "Engineering discipline that deals with the design and construction of the built environment.",
            faculty = "كلية الهندسة والبترول",
            example = "Skyscrapers and dams."
        ),
        VocabularyCard(
            id = "v4",
            termAr = "المحاسبة المالية",
            termEn = "Financial Accounting",
            definitionAr = "عملية تسجيل وتلخيص وإعداد التقارير عن العمليات المالية للمنشأة.",
            definitionEn = "The process of recording, summarizing and reporting the myriad of transactions resulting from business operations.",
            faculty = "كلية التجارة والاقتصاد",
            example = "Balance sheets and income statements."
        )
    )

    fun getWorldLibraries(): List<WorldLibrary> = listOf(
        WorldLibrary(
            id = "wl1",
            nameAr = "المكتبة الرقمية العالمية (WDL)",
            nameEn = "World Digital Library",
            category = "مكتبة تاريخية",
            url = "https://www.wdl.org",
            description = "تتيح الوصول المجاني للمراجع التاريخية والمخطوطات النادرة من مختلف دول العالم."
        ),
        WorldLibrary(
            id = "wl2",
            nameAr = "منصة جوجل للأبحاث (Google Scholar)",
            nameEn = "Google Scholar",
            category = "محرك بحث أكاديمي",
            url = "https://scholar.google.com",
            description = "محرك بحث متخصص في المؤلفات الأكاديمية والرسائل العلمية والمقالات الموثقة."
        ),
        WorldLibrary(
            id = "wl3",
            nameAr = "أرشيف الإنترنت (Internet Archive)",
            nameEn = "Internet Archive",
            category = "مكتبة رقمية شاملة",
            url = "https://archive.org",
            description = "مكتبة ضخمة تضم ملايين الكتب المجانية، والأفلام، والبرمجيات، والموسيقى."
        ),
        WorldLibrary(
            id = "wl4",
            nameAr = "بنك المعرفة العربي (AskZad)",
            nameEn = "AskZad",
            category = "قاعدة بيانات عربية",
            url = "https://www.askzad.com",
            description = "أكبر قاعدة بيانات عربية تضم كتباً ودوريات وأبحاثاً علمية باللغة العربية."
        )
    )

    fun getExamResults(): List<ExamResult> = listOf(
        ExamResult("CSC301", "هندسة البرمجيات", 3, "A+", 98),
        ExamResult("CSC302", "الذكاء الاصطناعي", 3, "A", 93)
    )
}
