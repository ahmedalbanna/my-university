package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.SemesterEntity
import com.example.data.local.entities.CourseEntity
import com.example.data.local.entities.ScheduleEntity
import com.example.data.model.Book
import com.example.data.model.ExamResult
import com.example.data.model.Project
import com.example.data.model.Question
import com.example.data.model.StudentProfile
import com.example.data.model.VocabularyCard
import com.example.data.model.WorldLibrary
import android.media.RingtoneManager
import android.net.Uri
import com.example.data.repository.JamaatyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "STUDENT", "JAMAATY_AI"
    val text: String,
    val timestamp: String = "الآن"
)

class JamaatyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JamaatyRepository(AppDatabase.getDatabase(application).appDao())

    // Onboarding State
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Student Profile
    private val _studentProfile = MutableStateFlow(StudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    // Level & Faculty Filters
    private val _selectedLevel = MutableStateFlow(3) // Default level 3
    val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

    private val _selectedSemester = MutableStateFlow(1) // 1 or 2
    val selectedSemester: StateFlow<Int> = _selectedSemester.asStateFlow()

    private val _selectedFaculty = MutableStateFlow("الكل")
    val selectedFaculty: StateFlow<String> = _selectedFaculty.asStateFlow()

    init {
        // Load onboarding state & profile
        viewModelScope.launch {
            _onboardingCompleted.value = repository.isOnboardingCompleted()
            val savedProfile = repository.getSavedProfile()
            _studentProfile.value = savedProfile
            _selectedLevel.value = savedProfile.level
            _selectedFaculty.value = savedProfile.faculty
        }

        // Seed initial sample payment if empty
        viewModelScope.launch {
            repository.addPayment(
                PaymentEntity(
                    title = "رسوم الفصل الدراسي الأول 2026",
                    category = "TUITION",
                    amount = 3500.0,
                    status = "PAID",
                    date = "2026-01-10",
                    referenceNo = "TXN-8841290",
                    paymentMethod = "محفظة الكريمي / كاش"
                )
            )
            repository.addPayment(
                PaymentEntity(
                    title = "اشتراك باقة الطلاب للاتصالات 50GB",
                    category = "TELECOM",
                    amount = 5000.0,
                    status = "PAID",
                    date = "2026-02-01",
                    referenceNo = "TXN-9931002",
                    paymentMethod = "محفظة جوالي"
                )
            )
        }

        // Seed notifications if empty
        viewModelScope.launch {
            val current = repository.notifications.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            if (current.isEmpty()) {
                repository.addNotification(
                    NotificationEntity(
                        id = "notif_1",
                        title = "موعد اختبار قادم",
                        message = "اختبار مادة القانون الإداري غداً الساعة 10:00 صباحاً في القاعة 402.",
                        timestamp = System.currentTimeMillis() - 3600000,
                        type = "EXAM"
                    )
                )
                repository.addNotification(
                    NotificationEntity(
                        id = "notif_2",
                        title = "تنبيه سداد رسوم",
                        message = "تبقي 3 أيام على الموعد النهائي لسداد رسوم الفصل الدراسي الثاني لتجنب الغرامة.",
                        timestamp = System.currentTimeMillis() - 86400000,
                        type = "FEE"
                    )
                )
                repository.addNotification(
                    NotificationEntity(
                        id = "notif_3",
                        title = "رفع كتب جديدة",
                        message = "تمت إضافة 5 مراجع جديدة في قسم الدراسات الإسلامية بالمكتبة.",
                        timestamp = System.currentTimeMillis() - 172800000,
                        type = "LIBRARY"
                    )
                )
            }
        }

        // Seed initial schedule if empty
        viewModelScope.launch {
            val current = repository.schedules.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            if (current.isEmpty()) {
                val sampleSchedules = listOf(
                    ScheduleEntity(
                        dayOfWeek = "الأحد",
                        subject = "خوارزميات وتراكيب البيانات",
                        doctor = "د. صادق الشراجي",
                        startTime = "08:00",
                        endTime = "10:00",
                        room = "قاعة 402",
                        color = 0xFF6200EE.toInt()
                    ),
                    ScheduleEntity(
                        dayOfWeek = "الأحد",
                        subject = "هندسة البرمجيات",
                        doctor = "د. وليد البعداني",
                        startTime = "10:30",
                        endTime = "12:30",
                        room = "معمل 5",
                        color = 0xFF03DAC6.toInt()
                    ),
                    ScheduleEntity(
                        dayOfWeek = "الأثنين",
                        subject = "قواعد البيانات",
                        doctor = "د. محمد العمراني",
                        startTime = "09:00",
                        endTime = "11:00",
                        room = "قاعة 201",
                        color = 0xFFFFAB00.toInt()
                    ),
                    ScheduleEntity(
                        dayOfWeek = "الأربعاء",
                        subject = "الأمن السيبراني",
                        doctor = "د. خليل المعمري",
                        startTime = "08:00",
                        endTime = "10:30",
                        room = "قاعة التشريفات",
                        color = 0xFFD81B60.toInt()
                    )
                )
                sampleSchedules.forEach { repository.addSchedule(it) }
            }
        }
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    fun updateStudentProfile(
        name: String,
        studentId: String,
        university: String,
        faculty: String,
        major: String,
        level: Int
    ) {
        val updated = _studentProfile.value.copy(
            name = name,
            studentId = studentId,
            university = university,
            faculty = faculty,
            major = major,
            level = level
        )
        _studentProfile.value = updated
        _selectedLevel.value = level
        _selectedFaculty.value = faculty

        viewModelScope.launch {
            repository.saveStudentProfile(updated)
            repository.setOnboardingCompleted(true)
            _onboardingCompleted.value = true
        }
    }

    fun getUniversities() = repository.getUniversities()
    fun getMajorsForFaculty(faculty: String) = repository.getMajorsForFaculty(faculty)

    // Search queries
    private val _bookSearchQuery = MutableStateFlow("")
    val bookSearchQuery: StateFlow<String> = _bookSearchQuery.asStateFlow()

    private val _projectSearchQuery = MutableStateFlow("")
    val projectSearchQuery: StateFlow<String> = _projectSearchQuery.asStateFlow()

    private val _projectYearFilter = MutableStateFlow("الكل")
    val projectYearFilter: StateFlow<String> = _projectYearFilter.asStateFlow()

    private val _questionSearchQuery = MutableStateFlow("")
    val questionSearchQuery: StateFlow<String> = _questionSearchQuery.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("الكل")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedQuestionTag = MutableStateFlow("الكل")
    val selectedQuestionTag: StateFlow<String> = _selectedQuestionTag.asStateFlow()

    // Interactive Quiz State
    private val _activeQuizQuestionIndex = MutableStateFlow(0)
    val activeQuizQuestionIndex: StateFlow<Int> = _activeQuizQuestionIndex.asStateFlow()

    private val _selectedQuizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedQuizAnswers: StateFlow<Map<Int, Int>> = _selectedQuizAnswers.asStateFlow()

    private val _quizSubmitted = MutableStateFlow(false)
    val quizSubmitted: StateFlow<Boolean> = _quizSubmitted.asStateFlow()

    // Translation State
    private val _translationInputText = MutableStateFlow("")
    val translationInputText: StateFlow<String> = _translationInputText.asStateFlow()

    private val _translationOutputText = MutableStateFlow("")
    val translationOutputText: StateFlow<String> = _translationOutputText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    // Chat Assistant State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("JAMAATY_AI", "مرحباً بك يا أحمد! أنا مساعد جامعتي الذكي. كيف يمكنني مساعدتك في المذاكرة أو استفسارات الكلية اليوم؟")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _unreadAiMessagesCount = MutableStateFlow(0)
    val unreadAiMessagesCount: StateFlow<Int> = _unreadAiMessagesCount.asStateFlow()

    // Favorites & Payments from Room DB
    val favorites: StateFlow<List<FavoriteEntity>> = repository.favorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val payments: StateFlow<List<PaymentEntity>> = repository.payments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Schedule
    val schedules: StateFlow<List<ScheduleEntity>> = repository.schedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(subject: String, day: String, start: String, end: String, room: String, doctor: String, color: Int) {
        viewModelScope.launch {
            repository.addSchedule(
                ScheduleEntity(
                    subject = subject,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    room = room,
                    doctor = doctor,
                    color = color
                )
            )
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
    
    // Academic / GPA
    val semesters: StateFlow<List<SemesterEntity>> = repository.semesters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCoursesForSemester(semesterId: String): Flow<List<CourseEntity>> = repository.getCoursesForSemester(semesterId)

    fun addSemester(name: String, year: String) {
        viewModelScope.launch {
            repository.addSemester(SemesterEntity(name = name, academicYear = year))
        }
    }

    fun addCourse(semesterId: String, name: String, creditHours: Int, grade: Double) {
        viewModelScope.launch {
            repository.addCourse(CourseEntity(semesterId = semesterId, name = name, creditHours = creditHours, grade = grade))
        }
    }

    fun deleteSemester(semester: SemesterEntity) {
        viewModelScope.launch {
            repository.deleteSemester(semester)
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    // GPA Calculation logic
    fun calculateGpa(courses: List<CourseEntity>): Double {
        if (courses.isEmpty()) return 0.0
        val totalPoints = courses.sumOf { it.grade * it.creditHours }
        val totalCredits = courses.sumOf { it.creditHours }
        if (totalCredits == 0) return 0.0
        return totalPoints / totalCredits
    }

    fun calculateCumulativeGpa(semestersWithCourses: Map<String, List<CourseEntity>>): Double {
        val allCourses = semestersWithCourses.values.flatten()
        return calculateGpa(allCourses)
    }

    // Data lists getters
    fun getFaculties() = repository.getFaculties()
    fun getWorldLibraries(): List<WorldLibrary> = repository.getWorldLibraries()
    fun getExamResults(): List<ExamResult> = repository.getExamResults()
    fun getVocabularyCards(): List<VocabularyCard> = repository.getVocabulary()

    // Filtering Functions
    fun setLevel(level: Int) {
        _selectedLevel.value = level
    }

    fun setSemester(semester: Int) {
        _selectedSemester.value = semester
    }

    fun setFaculty(faculty: String) {
        _selectedFaculty.value = faculty
    }

    fun setBookSearchQuery(query: String) {
        _bookSearchQuery.value = query
    }

    fun setProjectSearchQuery(query: String) {
        _projectSearchQuery.value = query
    }

    fun setProjectYearFilter(year: String) {
        _projectYearFilter.value = year
    }

    fun setQuestionSearchQuery(query: String) {
        _questionSearchQuery.value = query
    }

    fun setDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun setQuestionTag(tag: String) {
        _selectedQuestionTag.value = tag
    }

    // Books list filtered ONLY by student's current profile (Fixed Scope)
    fun getFilteredBooks(): List<Book> {
        val profile = _studentProfile.value
        return repository.getBooks().filter { book ->
            val matchLevel = (book.level == profile.level)
            val matchSemester = (book.semester == profile.semester)
            val matchFaculty = (book.faculty == profile.faculty)
            val matchSearch = _bookSearchQuery.value.isEmpty() ||
                    book.title.contains(_bookSearchQuery.value, ignoreCase = true) ||
                    book.author.contains(_bookSearchQuery.value, ignoreCase = true)

            matchLevel && matchSemester && matchFaculty && matchSearch
        }
    }

    // Global Books search for World Library (Variable Scope)
    fun getGlobalFilteredBooks(): List<Book> {
        return repository.getBooks().filter { book ->
            val matchLevel = (book.level == _selectedLevel.value)
            val matchSemester = (book.semester == _selectedSemester.value)
            val matchFaculty = (_selectedFaculty.value == "الكل" || book.faculty == _selectedFaculty.value)
            val matchSearch = _bookSearchQuery.value.isEmpty() ||
                    book.title.contains(_bookSearchQuery.value, ignoreCase = true) ||
                    book.author.contains(_bookSearchQuery.value, ignoreCase = true)

            matchLevel && matchSemester && matchFaculty && matchSearch
        }
    }

    private val _userProjects = MutableStateFlow<List<Project>>(emptyList())
    val userProjects: StateFlow<List<Project>> = _userProjects.asStateFlow()

    fun addNewProject(
        title: String,
        studentName: String,
        supervisor: String,
        faculty: String,
        year: String,
        level: Int,
        summary: String,
        tags: List<String>
    ) {
        val newProj = Project(
            id = "proj_${System.currentTimeMillis()}",
            title = title,
            studentName = studentName,
            supervisor = supervisor,
            faculty = faculty,
            year = year,
            level = level,
            summary = summary,
            tags = tags,
            fileFormat = "PDF",
            fileSize = "5.1 MB",
            downloadsCount = 1
        )
        _userProjects.value = _userProjects.value + newProj
    }

    // Researches & Projects filtered for Student's Library (Fixed Faculty/Level)
    fun getFilteredProjects(): List<Project> {
        val profile = _studentProfile.value
        val allProjects = repository.getProjects() + _userProjects.value
        return allProjects.filter { proj ->
            val matchFaculty = (proj.faculty == profile.faculty)
            val matchLevel = (proj.level == profile.level)
            val matchSearch = _projectSearchQuery.value.isEmpty() ||
                    proj.title.contains(_projectSearchQuery.value, ignoreCase = true) ||
                    proj.studentName.contains(_projectSearchQuery.value, ignoreCase = true)

            matchFaculty && matchLevel && matchSearch
        }
    }

    // Researches & Projects filtered for Global Search
    fun getGlobalFilteredProjects(): List<Project> {
        val allProjects = repository.getProjects() + _userProjects.value
        return allProjects.filter { proj ->
            val matchFaculty = (_selectedFaculty.value == "الكل" || proj.faculty == _selectedFaculty.value)
            val matchLevel = (proj.level == _selectedLevel.value)
            val matchYear = (_projectYearFilter.value == "الكل" || proj.year == _projectYearFilter.value)
            val matchSearch = _projectSearchQuery.value.isEmpty() ||
                    proj.title.contains(_projectSearchQuery.value, ignoreCase = true) ||
                    proj.studentName.contains(_projectSearchQuery.value, ignoreCase = true) ||
                    proj.tags.any { it.contains(_projectSearchQuery.value, ignoreCase = true) }

            matchFaculty && matchLevel && matchYear && matchSearch
        }
    }

    // Questions filtered for Exam Platform
    fun getFilteredQuestions(): List<Question> {
        return repository.getQuestions().filter { q ->
            val matchFaculty = (_selectedFaculty.value == "الكل" || q.faculty == _selectedFaculty.value)
            val matchDifficulty = (_selectedDifficulty.value == "الكل" || q.difficulty == _selectedDifficulty.value)
            val matchTag = (_selectedQuestionTag.value == "الكل" || q.tags.contains(_selectedQuestionTag.value))
            val matchSearch = _questionSearchQuery.value.isEmpty() ||
                    q.questionText.contains(_questionSearchQuery.value, ignoreCase = true) ||
                    q.subject.contains(_questionSearchQuery.value, ignoreCase = true)

            matchFaculty && matchDifficulty && matchTag && matchSearch
        }
    }

    // Quiz Actions
    fun answerQuizQuestion(questionIndex: Int, optionIndex: Int) {
        val currentMap = _selectedQuizAnswers.value.toMutableMap()
        currentMap[questionIndex] = optionIndex
        _selectedQuizAnswers.value = currentMap
    }

    fun submitQuiz() {
        _quizSubmitted.value = true
    }

    fun resetQuiz() {
        _activeQuizQuestionIndex.value = 0
        _selectedQuizAnswers.value = emptyMap()
        _quizSubmitted.value = false
    }

    // Favorites Action
    fun toggleFavorite(id: String, type: String, title: String, subtitle: String, faculty: String, tag: String = "") {
        viewModelScope.launch {
            val currentFavs = favorites.value
            val isFav = currentFavs.any { it.id == id }
            repository.toggleFavorite(id, type, title, subtitle, faculty, tag, isFav)
        }
    }

    // Payment Actions
    fun processPayment(title: String, category: String, amount: Double, method: String) {
        viewModelScope.launch {
            val newPayment = PaymentEntity(
                title = title,
                category = category,
                amount = amount,
                status = "PAID",
                date = "2026-08-09",
                referenceNo = "TXN-${(1000000..9999999).random()}",
                paymentMethod = method
            )
            repository.addPayment(newPayment)

            // Update balance: deduct if using wallet balance, increase if recharging
            val current = _studentProfile.value
            if (category == "RECHARGE" || title.contains("شحن")) {
                _studentProfile.value = current.copy(balance = current.balance + amount)
            } else if (method.contains("المحفظة")) {
                _studentProfile.value = current.copy(balance = (current.balance - amount).coerceAtLeast(0.0))
            }
        }
    }

    // Translation Actions
    fun translateText(input: String) {
        _translationInputText.value = input
        if (input.isBlank()) {
            _translationOutputText.value = ""
            return
        }

        _isTranslating.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(600) // Simulate fast AI processing
            _translationOutputText.value = simulateAcademicTranslation(input)
            _isTranslating.value = false
        }
    }

    // Notification Actions
    fun sendNotification(title: String, message: String, type: String = "GENERAL") {
        viewModelScope.launch {
            val notif = NotificationEntity(
                id = "notif_${System.currentTimeMillis()}",
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type,
                isRead = false
            )
            repository.addNotification(notif)
            playNotificationSound()
        }
    }

    private fun playNotificationSound() {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(getApplication(), notification)
            r.play()
        } catch (e: Exception) {
            // Log or handle error if sound cannot be played
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }

    private fun simulateAcademicTranslation(text: String): String {
        return when {
            text.contains("data structure", ignoreCase = true) -> "تراكيب البيانات (Data Structures): التنظيم الأكاديمي للبيانات وتراكيب التخزين في الذاكرة."
            text.contains("algorithm", ignoreCase = true) -> "الخوارزميات (Algorithms): تسلسل من الخطوات المنطقية المحددة لحل مسألة حاسوبية."
            text.contains("artificial intelligence", ignoreCase = true) -> "الذكاء الاصطناعي (AI): محاكاة القدرات الذكية للبشر كالتفكير والتعلم والاستنتاج."
            text.contains("database", ignoreCase = true) -> "قواعد البيانات (Databases): مجموعة منظمة من البيانات المخزنة إلكترونياً."
            text.contains("تراكيب", ignoreCase = true) -> "Data Structures: Specialized format for organizing and storing data in computer memory."
            text.contains("خوارزمية", ignoreCase = true) -> "Algorithm: A step-by-step procedure or formula for solving a problem."
            else -> "الترجمة الأكاديمية: $text - (مترجم مع تحسين مصطلحات الكلية المعتمدة)."
        }
    }

    // Chat Actions
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage("STUDENT", userText))
        _chatMessages.value = current

        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // Simulate thinking
            val aiResponse = generateAiResponse(userText)
            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage("JAMAATY_AI", aiResponse))
            _chatMessages.value = updated
            
            // For demo: increment unread if chat was hidden (handled in UI usually)
            // But here we'll just increment to show the badge functionality
            _unreadAiMessagesCount.value += 1
            playNotificationSound()
        }
    }

    fun markAiChatAsRead() {
        _unreadAiMessagesCount.value = 0
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("JAMAATY_AI", "مرحباً بك مجدداً! كيف يمكنني مساعدتك الآن؟")
        )
    }

    private fun generateAiResponse(query: String): String {
        return when {
            query.contains("سعر", ignoreCase = true) || query.contains("رسوم", ignoreCase = true) ->
                "يمكنك تسديد الرسوم الدراسية أو رسوم السكن والساعات من خلال قسم 'محفظتي والخدمات المالية'. تتوفر طرق دفع سريعة عبر الكريمي، البطاقات المصرفية والمحافظ الرقمية اليمنيّة."
            query.contains("كتاب", ignoreCase = true) || query.contains("مرجع", ignoreCase = true) || query.contains("ترم", ignoreCase = true) ->
                "أهلاً بك! يمكنك الوصول لجميع المراجع والكتب المقررة لمستواك الدراسي من خلال قسم 'المكتبة الرقمية' في القائمة الجانبية. هل تبحث عن كتاب محدد؟"
            query.contains("اختبار", ignoreCase = true) || query.contains("امتحان", ignoreCase = true) ->
                "ستجد نماذج الاختبارات السابقة والتمارين في قسم 'بنك الاختبارات'. نوصي بمراجعة اختبارات العام الماضي للتحضير بشكل أفضل."
            query.contains("جدول", ignoreCase = true) ->
                "يمكنك عرض جدولك الدراسي الأسبوعي ومواعيد المحاضرات من خلال تبويب 'الجدول' في الشاشة الرئيسية."
            query.contains("معدل", ignoreCase = true) || query.contains("gpa", ignoreCase = true) ->
                "لحساب معدلك التراكمي وتوقع نتائجك، استخدم 'حاسبة المعدل' الموجودة في قسم المحفظة."
            query.contains("تخرج", ignoreCase = true) ->
                "شروط التخرج تشمل إكمال جميع الساعات المعتمدة بنجاح، وتقديم مشروع التخرج، وإخلاء الطرف من المكتبة والشؤون المالية. يمكنك مراجعة حالتك الأكاديمية في قسم 'ملفي الشخصي'."
            query.contains("سلام", ignoreCase = true) || query.contains("مرحبا", ignoreCase = true) || query.contains("هلا", ignoreCase = true) ->
                "وعليكم السلام ورحمة الله! أهلاً بك في تطبيق جامعتي. أنا هنا لمساعدتك في كل ما يخص حياتك الجامعية. ماذا تريد أن تعرف؟"
            query.contains("بحث", ignoreCase = true) || query.contains("مشروع", ignoreCase = true) ->
                "يمكنك استعراض أبحاث ومشاريع التخرج السابقة من قسم الأبحاث والمشاريع، وتصفيتها حسب السنة والكلية لقراءة التحليل وتنزيل الملحقات."
            else -> "عذراً، لم أفهم استفسارك تماماً. يمكنك سؤالي عن الرسوم، الجداول الدراسية، الكتب والمراجع، أو كيفية حساب المعدل التراكمي."
        }
    }
}
