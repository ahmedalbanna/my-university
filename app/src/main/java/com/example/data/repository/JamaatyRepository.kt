package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.PreferenceEntity
import com.example.data.local.entities.SemesterEntity
import com.example.data.local.entities.CourseEntity
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
        tag: String = ""
    , isFav: Boolean) {
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

    fun getStudentProfile(): StudentProfile = StudentProfile()

    fun getFaculties(): List<String> = listOf(
        "الكل",
        "كلية الحاسبات والمعلومات",
        "كلية الهندسة",
        "كلية الطب والجراحة",
        "كلية إدارة الأعمال",
        "كلية العلوم"
    )

    fun getBooks(): List<Book> = listOf(
        // Level 1
        Book(
            id = "b101",
            title = "مقدمة في برمجة الحواسب وتراكيب البيانات",
            author = "د. خالد السعيد",
            faculty = "كلية الحاسبات والمعلومات",
            level = 1,
            semester = 1,
            pages = 320,
            category = "كتاب مقرر",
            rating = 4.8f,
            description = "مرجع شامل لأساسيات الخوارزميات وتراكيب البيانات بلغتي C++ و Java مع أداء التطبيقات الأساسية.",
            fileSize = "14.2 MB"
        ),
        Book(
            id = "b102",
            title = "الرياضيات الهندسية والتفاضل والتكامل I",
            author = "أ.د. مصطفى حسان",
            faculty = "كلية الهندسة",
            level = 1,
            semester = 1,
            pages = 410,
            category = "مرجع أساسي",
            rating = 4.6f,
            description = "شرح مبسط لقوانين التفاضل والتكامل والتطبيقات الفيزيائية والهندسية المتقدمة.",
            fileSize = "18.5 MB"
        ),
        Book(
            id = "b103",
            title = "علم التشريح البشري العام والوظائف",
            author = "د. نادية الفاضل",
            faculty = "كلية الطب والجراحة",
            level = 1,
            semester = 1,
            pages = 520,
            category = "كتاب مقرر",
            rating = 4.9f,
            description = "أطلس مصور وشامل لأجهزة الجسم البشري ومبادئ علم وظائف الأعضاء للسنوات الأولى.",
            fileSize = "32.0 MB"
        ),
        Book(
            id = "b104",
            title = "مبادئ إدارة الأعمال والقيادة الحديثة",
            author = "د. عمر الفاروق",
            faculty = "كلية إدارة الأعمال",
            level = 1,
            semester = 2,
            pages = 280,
            category = "ملخص دراسي",
            rating = 4.5f,
            description = "دليل مبادئ التخطيط والتنظيم والتوجيه الإداري والمقومات الاقتصادية للشركات.",
            fileSize = "8.7 MB"
        ),

        // Level 2
        Book(
            id = "b201",
            title = "قواعد البيانات ونظم إدارة المعلومات SQL",
            author = "د. ياسر الشمري",
            faculty = "كلية الحاسبات والمعلومات",
            level = 2,
            semester = 1,
            pages = 350,
            category = "كتاب مقرر",
            rating = 4.7f,
            description = "تصميم قواعد البيانات العلاقاتية وتحسين الاستعلامات والربط المعقد.",
            fileSize = "11.4 MB"
        ),
        Book(
            id = "b202",
            title = "الدوائر الكهربائية والإلكترونيات الرقمية",
            author = "د. طارق الحكيم",
            faculty = "كلية الهندسة",
            level = 2,
            semester = 2,
            pages = 390,
            category = "كتاب مقرر",
            rating = 4.6f,
            description = "دراسة المحولات والبوابات المنطقية وتحليل الدوائر الإلكترونية المتقدمة.",
            fileSize = "16.1 MB"
        ),

        // Level 3
        Book(
            id = "b301",
            title = "هندسة البرمجيات وإدارة المشروعات التقنية",
            author = "د. أسامة القحطاني",
            faculty = "كلية الحاسبات والمعلومات",
            level = 3,
            semester = 1,
            pages = 310,
            category = "مرجع أساسي",
            rating = 4.9f,
            description = "منهجيات Agile و Scrum، دورة حياة تطوير البرمجيات، واختبار الأنظمة البرمجية.",
            fileSize = "9.8 MB"
        ),
        Book(
            id = "b302",
            title = "ذكاء الآلة والتعلم العميق للمهندسين",
            author = "د. سامي الجابري",
            faculty = "كلية الحاسبات والمعلومات",
            level = 3,
            semester = 2,
            pages = 460,
            category = "كتاب مقرر",
            rating = 4.95f,
            description = "تطبيقات الشبكات العصيبة الاصطناعية ومعالجة اللغات الطبيعية باستخدام Python.",
            fileSize = "22.3 MB"
        ),

        // Level 4 & 5
        Book(
            id = "b401",
            title = "الأمن السيبراني وتشفير البيانات المتقدم",
            author = "د. فهد الماجد",
            faculty = "كلية الحاسبات والمعلومات",
            level = 4,
            semester = 1,
            pages = 380,
            category = "مرجع أساسي",
            rating = 4.88f,
            description = "آليات التشفير المتناظر وغير المتناظر وحماية الشبكات وتحليل الثغرات الأمنية.",
            fileSize = "15.0 MB"
        ),
        Book(
            id = "b501",
            title = "استراتيجيات التحول الرقمي وإدارة الابتكار",
            author = "أ.د. ماجد الدوسري",
            faculty = "كلية إدارة الأعمال",
            level = 5,
            semester = 2,
            pages = 340,
            category = "كتاب مقرر",
            rating = 4.75f,
            description = "إدارة التغيير في المؤسسات الكبرى وبناء نماذج الأعمال الرقمية الحديثة.",
            fileSize = "12.6 MB"
        )
    )

    fun getProjects(): List<Project> = listOf(
        Project(
            id = "p101",
            title = "نظام ذكي للتشخيص المبكر لأمراض العيون باستخدام الذكاء الاصطناعي",
            studentName = "عبدالرحمن الملا وزملاؤه",
            supervisor = "د. سامي الجابري",
            faculty = "كلية الحاسبات والمعلومات",
            year = "2025",
            level = 4,
            summary = "مشروع تخرج يستخدم شبكات CNN المتقدمة لتحليل صور شبكية العين واكتشاف المياه البيضاء بنسبة دقة 97.4%.",
            tags = listOf("ذكاء اصطناعي", "رؤية حاسوبية", "طب رقمي")
        ),
        Project(
            id = "p102",
            title = "تصميم شبكة مياه ذكية ومستدامة للمدن الحديثة",
            studentName = "فاطمة الزهراء الشريف",
            supervisor = "أ.د. مصطفى حسان",
            faculty = "كلية الهندسة",
            year = "2024",
            level = 5,
            summary = "تحليل هيدروليكي واستخدام مستشعرات IoT للكشف الذكي عن التسربات وتقليل الهدر في الشبكات المائية.",
            tags = listOf("هندسة مدنية", "إنترنت الأشياء", "استدامة")
        ),
        Project(
            id = "p103",
            title = "دراسة مقارنة لتأثير التسويق الرقمي على المنشآت الناشئة",
            studentName = "سارة بن خالد",
            supervisor = "د. عمر الفاروق",
            faculty = "كلية إدارة الأعمال",
            year = "2025",
            level = 3,
            summary = "بحث ميداني يحلل سلوك المستهلك الرقمي وعوائد الاستثمار في حملات التواصل الاجتماعي بالمنطقة.",
            tags = listOf("تسويق", "تحليل بيانات", "ريادة أعمال")
        ),
        Project(
            id = "p104",
            title = "منصة جامعتي الذكية لإدارة المناهج والخدمات الطلابية",
            studentName = "فريق مبتكري المستقبل",
            supervisor = "د. أسعد الهاشمي",
            faculty = "كلية الحاسبات والمعلومات",
            year = "2026",
            level = 4,
            summary = "تطبيق أندرويد متكامل يجمع المكتبة، المحفظة المالية، وبنك الأسئلة التفاعلي لطلاب الجامعة.",
            tags = listOf("أندرويد", "تطبيق جامعي", "Jetpack Compose")
        )
    )

    fun getQuestions(): List<Question> = listOf(
        Question(
            id = "q101",
            questionText = "ما هي التعقدية الزمانية (Time Complexity) الخوارزمية لخوارزمية البحث الثنائي (Binary Search) في مصفوفة مرتبة؟",
            subject = "خوارزميات وتراكيب البيانات",
            faculty = "كلية الحاسبات والمعلومات",
            level = 2,
            semester = 1,
            difficulty = "متوسط",
            options = listOf("O(n)", "O(log n)", "O(n^2)", "O(1)"),
            correctIndex = 1,
            explanation = "البحث الثنائي يقوم بتقسيم مصفوفة البحث إلى النصف في كل خطوة، مما يجعل التعقيد الزمني لوغاريتمي O(log n).",
            tags = listOf("#خوارزميات", "#ترم_أول", "#اختبارات_سابقة"),
            versionHistory = listOf(
                QuestionVersion("v1.2", "2026-01-15", "اللجنة الأكاديمية", "تحديث شرح الإجابة وإضافة تفاصيل تقليل الفضاء."),
                QuestionVersion("v1.0", "2025-09-10", "د. خالد السعيد", "إضافة السؤال لبنك نماذج الاختبارات النهائي.")
            )
        ),
        Question(
            id = "q102",
            questionText = "أي من العبارات التالية تصف مبدأ التجريد (Abstraction) في البرمجة كائنية التوجه (OOP) بشكل صحيح؟",
            subject = "برمجة كائنية التوجه",
            faculty = "كلية الحاسبات والمعلومات",
            level = 1,
            semester = 2,
            difficulty = "سهل",
            options = listOf(
                "إخفاء تفاصيل التنفيذ الداخلي وإظهار الواجهة الضرورية فقط للمستخدم",
                "وراثة الخصائص من فئة رئيسية إلى فئات فرعية",
                "تعدد الأشكال واستدعاء الدالة بحسب الكائن",
                "تجميع البيانات والدوال داخل كبسولة واحدة"
            ),
            correctIndex = 0,
            explanation = "التجريد يركز على إظهار الميزات الأساسية للكائن وإخفاء تعقيد آليات العمل الداخلية.",
            tags = listOf("#OOP", "#أساسيات", "#مستوى1"),
            versionHistory = listOf(
                QuestionVersion("v1.1", "2025-11-20", "د. أسامة", "مراجعة الخيارات لضبط الصياغة الأكاديمية.")
            )
        ),
        Question(
            id = "q103",
            questionText = "في قانون أوم الكهربائي، ما هي العلاقة بين حث التيار (I) والفرق في الجهد (V) عند ثبوت المقاومة (R)؟",
            subject = "الفيزياء الهندسية والكهرباء",
            faculty = "كلية الهندسة",
            level = 1,
            semester = 1,
            difficulty = "سهل",
            options = listOf(
                "علاقة طردية خطية (I = V / R)",
                "علاقة عكسية تربيعية",
                "لا توجد علاقة بينهما",
                "علاقة لوغاريتمية"
            ),
            correctIndex = 0,
            explanation = "ينص قانون أوم على أن التيار المتدفق عبر موصل بين نقطتين يتناسب طردياً مع الجهد عبر النقطتين.",
            tags = listOf("#فيزياء", "#هندسة", "#قانون_أوم"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-10-01", "أ.د. مصطفى", "إدخال السؤال في بنك الأسئلة الشامل.")
            )
        ),
        Question(
            id = "q104",
            questionText = "ما هو العضو الرئيسي المسؤول عن تصفية الدم وتنظيم التوازن المائي والسام في الجسم البشري؟",
            subject = "وظائف الأعضاء (Physiology)",
            faculty = "كلية الطب والجراحة",
            level = 2,
            semester = 1,
            difficulty = "متوسط",
            options = listOf("الكبِد", "الكلية (Kidney)", "الرئة", "البنكرياس"),
            correctIndex = 1,
            explanation = "تقوم الكليتان بتصفية السوائل والفضلات الأيضية من الدم الحركي وإخراجها وتنظيم الضغط المائي.",
            tags = listOf("#طب", "#وظائف_أعضاء", "#تشريح"),
            versionHistory = listOf(
                QuestionVersion("v1.0", "2025-08-12", "د. نادية", "سؤال دوري في نماذج الاختبارات النصفية.")
            )
        ),
        Question(
            id = "q105",
            questionText = "ما هي الفائدة الرئيسية لإستخدام الفهارس (Indexes) في قواعد البيانات العلاقاتية SQL؟",
            subject = "قواعد البيانات SQL",
            faculty = "كلية الحاسبات والمعلومات",
            level = 2,
            semester = 2,
            difficulty = "صعب",
            options = listOf(
                "توفير المساحة التخزينية في القرص الصلب",
                "تسريع عمليات استعلام وقراءة البيانات SELECT على حساب سرعة الكتابة",
                "تشفير البيانات الحساسة بشكل تلقائي",
                "النسخ الاحتياطي التلقائي للجداول"
            ),
            correctIndex = 1,
            explanation = "تنشئ الفهارس تراكيب بيانات متوازنة B-Tree لتسريع استرجاع السجلات بدلاً من التفتيش الكامل للجدول Full Table Scan.",
            tags = listOf("#قواعد_بيانات", "#SQL", "#صعب"),
            versionHistory = listOf(
                QuestionVersion("v2.0", "2026-02-01", "مراجعة الاختبارات", "تحديث الخيار الرابع وتدقيق تعقيد البحث.")
            )
        )
    )

    fun getVocabulary(): List<VocabularyCard> = listOf(
        VocabularyCard(
            id = "v1",
            termAr = "تراكيب البيانات",
            termEn = "Data Structures",
            definitionAr = "طريقة تنظيم وتخزين البيانات في الحاسوب لتسهيل الوصول إليها وتعديلها بكفاءة.",
            definitionEn = "A specialized format for organizing, processing, retrieving and storing data.",
            faculty = "كلية الحاسبات والمعلومات",
            example = "Arrays and Linked Lists are fundamental data structures."
        ),
        VocabularyCard(
            id = "v2",
            termAr = "معالجة اللغات الطبيعية",
            termEn = "Natural Language Processing (NLP)",
            definitionAr = "فرع من الذكاء الاصطناعي يهدف لفهم وتحليل وتوليد اللغة البشرية بواسطة الحاسوب.",
            definitionEn = "A field of AI focused on enabling computers to understand human language.",
            faculty = "كلية الحاسبات والمعلومات",
            example = "NLP enables automated translation and sentiment analysis."
        ),
        VocabularyCard(
            id = "v3",
            termAr = "التحليل الهيدروليكي",
            termEn = "Hydraulic Analysis",
            definitionAr = "دراسة تدفق السوائل والقوى المؤثرة عليها داخل أنابيب وقنوات الري والشبكات.",
            definitionEn = "The study of fluid flow behavior in pipe networks and open channels.",
            faculty = "كلية الهندسة",
            example = "Engineers use hydraulic analysis to design city water distribution."
        ),
        VocabularyCard(
            id = "v4",
            termAr = "علم الأدوية والفارماكولوجي",
            termEn = "Pharmacology",
            definitionAr = "الدراسة العلمية لاستجابة الأجسام الحية للمركبات الكيميائية والأدوية العلاجية.",
            definitionEn = "The branch of medicine concerned with the uses, effects, and modes of action of drugs.",
            faculty = "كلية الطب والجراحة",
            example = "Pharmacology helps clinicians understand drug interaction and toxicity."
        )
    )

    fun getWorldLibraries(): List<WorldLibrary> = listOf(
        WorldLibrary(
            id = "wl1",
            nameAr = "المكتبة الرقمية العالمية (WDL)",
            nameEn = "World Digital Library",
            category = "مكتبة تاريخية وأكاديمية",
            url = "https://www.wdl.org",
            description = "تتيح الوصول المجاني للمخطوطات والكتب النادرة والمراجع التاريخية من جميع دول العالم."
        ),
        WorldLibrary(
            id = "wl2",
            nameAr = "مكتبة IEEE Xplore التقنية",
            nameEn = "IEEE Xplore Digital Library",
            category = "هندسة وحاسبات",
            url = "https://ieeexplore.ieee.org",
            description = "أضخم قاعدة بيانات علمية محكمة للأبحاث الهندسية وعلوم الحاسب والاتصالات."
        ),
        WorldLibrary(
            id = "wl3",
            nameAr = "المكتبة الطبية الوطنية (PubMed)",
            nameEn = "PubMed National Library",
            category = "علوم طبية وحيوية",
            url = "https://pubmed.ncbi.nlm.nih.gov",
            description = "أكثر من 35 مليون مقال وبحث طبي محكم في الرعاية الصحية والأدوية."
        ),
        WorldLibrary(
            id = "wl4",
            nameAr = "الباحث العلمي من جوجل (Google Scholar)",
            nameEn = "Google Scholar",
            category = "أبحاث وأطروحات عامة",
            url = "https://scholar.google.com",
            description = "محرك بحث أكاديمي شامل للأوراق العلمية والرسائل الجامعية والاقتباسات."
        )
    )

    fun getExamResults(): List<ExamResult> = listOf(
        ExamResult("CSC301", "هندسة البرمجيات", 3, "A+", 98),
        ExamResult("CSC302", "الذكاء الاصطناعي", 3, "A", 93),
        ExamResult("CSC303", "قواعد البيانات 2", 3, "A-", 89),
        ExamResult("MTH201", "الاحتمالات والإحصاء", 2, "B+", 86),
        ExamResult("ENG102", "الكتابة الأكاديمية بالإنجليزية", 2, "A", 92)
    )
}
