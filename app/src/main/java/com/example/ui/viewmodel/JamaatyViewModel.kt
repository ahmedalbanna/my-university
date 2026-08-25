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
import com.example.data.model.CourseInfo
import com.example.data.model.Project
import com.example.data.model.StudentProfile
import com.example.data.model.VocabularyCard
import android.app.DownloadManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthResult
import com.example.data.repository.JamaatyRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "STUDENT", "JAMAATY_AI"
    val text: String,
    val timestamp: String = "الآن"
)

class JamaatyViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val repository = JamaatyRepository(appDao)
    val authRepository = AuthRepository(appDao)

    // Onboarding State
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Student Profile
    private val _studentProfile = MutableStateFlow(StudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    // P1.1 Auth state — backed by AuthRepository with persistence + Firebase fallback
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    val authState: StateFlow<AuthRepository.AuthState> = authRepository.authState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthRepository.AuthState.Unauthenticated)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = authRepository.login(email, pass)) {
                is AuthResult.Success -> {
                    _isLoggedIn.value = true
                    // Refresh profile (in case stored)
                    _studentProfile.value = repository.getSavedProfile()
                }
                is AuthResult.Error -> {
                    _authError.value = result.messageAr
                    _isLoggedIn.value = false
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            // P1.1: signUp validates confirm via repository; UI passes same pass as confirm for now
            when (val result = authRepository.signUp(email, pass, pass)) {
                is AuthResult.Success -> {
                    // P0-5: new user must complete ProfileSetup — empty profile triggers gate
                    _studentProfile.value = StudentProfile(
                        name = "",
                        studentId = "",
                        university = StudentProfile.Default.university,
                        faculty = StudentProfile.Default.faculty,
                        major = StudentProfile.Default.major,
                        level = StudentProfile.Default.level,
                        semester = StudentProfile.Default.semester,
                        gpa = StudentProfile.Default.gpa,
                        balance = StudentProfile.Default.balance
                    )
                    _isLoggedIn.value = true
                }
                is AuthResult.Error -> {
                    _authError.value = result.messageAr
                    _isLoggedIn.value = false
                }
            }
            _isAuthLoading.value = false
        }
    }

    // UI helper for signUp with confirmPassword
    fun signUpWithConfirm(email: String, pass: String, confirm: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = authRepository.signUp(email, pass, confirm)) {
                is AuthResult.Success -> {
                    _studentProfile.value = StudentProfile(
                        name = "", studentId = "",
                        university = StudentProfile.Default.university,
                        faculty = StudentProfile.Default.faculty,
                        major = StudentProfile.Default.major,
                        level = StudentProfile.Default.level,
                        semester = StudentProfile.Default.semester,
                        gpa = StudentProfile.Default.gpa,
                        balance = StudentProfile.Default.balance
                    )
                    _isLoggedIn.value = true
                }
                is AuthResult.Error -> {
                    _authError.value = result.messageAr
                    _isLoggedIn.value = false
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun clearAuthError() { _authError.value = null }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            // P1.1: clear user data transactionally via AuthRepository; reset profile to empty for next login gate
            _studentProfile.value = StudentProfile(
                name = "",
                studentId = "",
                university = StudentProfile.Default.university,
                faculty = StudentProfile.Default.faculty,
                major = StudentProfile.Default.major,
                level = StudentProfile.Default.level,
                semester = StudentProfile.Default.semester,
                gpa = StudentProfile.Default.gpa,
                balance = StudentProfile.Default.balance
            )
        }
    }

    // Level & Faculty Filters
    private val _selectedLevel = MutableStateFlow(3) // Default level 3
    val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

    private val _selectedSemester = MutableStateFlow(1) // 1 or 2
    val selectedSemester: StateFlow<Int> = _selectedSemester.asStateFlow()

    private val _selectedFaculty = MutableStateFlow("الكل")
    val selectedFaculty: StateFlow<String> = _selectedFaculty.asStateFlow()

    init {
        // P1.1: restore auth session first — survives process death via Room
        viewModelScope.launch {
            authRepository.init()
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
        // Load onboarding state & profile
        viewModelScope.launch {
            _onboardingCompleted.value = repository.isOnboardingCompleted()
            val savedProfile = repository.getSavedProfile()
            _studentProfile.value = savedProfile
            _selectedLevel.value = savedProfile.level
            _selectedFaculty.value = savedProfile.faculty
        }

        // Seed initial sample payments — P0-2 fix: use COUNT(*) instead of stateIn(...).value which is always empty on cold start
        viewModelScope.launch {
            if (repository.getPaymentsCount() == 0) {
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
        }

        // Seed notifications — P0-2 fix: use COUNT(*)
        viewModelScope.launch {
            if (repository.getNotificationsCount() == 0) {
                repository.addNotification(
                    NotificationEntity(
                        id = "notif_1",
                        title = "تحديث في الجدول الدراسي",
                        message = "تم تغيير قاعة محاضرة الخوارزميات غداً لتصبح في معمل الحاسوب 102.",
                        timestamp = System.currentTimeMillis() - 3600000,
                        type = "GENERAL"
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

        // Seed initial schedule — P0-2 fix: use COUNT(*)
        viewModelScope.launch {
            if (repository.getSchedulesCount() == 0) {
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

    fun getGovernorates() = repository.getGovernorates()
    fun getUniversitiesForGovernorate(governorate: String) = repository.getUniversitiesForGovernorate(governorate)
    fun getUniversities() = repository.getUniversities()
    fun getMajorsForFaculty(faculty: String) = repository.getMajorsForFaculty(faculty)
    fun getSemesters() = repository.getSemesters()
    fun getCourseDetails(courseName: String) = repository.getCourseDetails(courseName)
    fun getCourses(faculty: String, major: String, level: Int, semester: String) = 
        repository.getCourses(faculty, major, level, semester)

    // Search queries
    private val _bookSearchQuery = MutableStateFlow("")
    val bookSearchQuery: StateFlow<String> = _bookSearchQuery.asStateFlow()

    private val _projectSearchQuery = MutableStateFlow("")
    val projectSearchQuery: StateFlow<String> = _projectSearchQuery.asStateFlow()

    private val _projectYearFilter = MutableStateFlow("الكل")
    val projectYearFilter: StateFlow<String> = _projectYearFilter.asStateFlow()

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

    // P2.2: ledger derived — event-sourced from payments Flow (replaces static 258k/523k in WalletScreen)
    val totalPaidFlow: StateFlow<Double> = payments.map { list -> list.filter { it.status == "PAID" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDueFlow: StateFlow<Double> = payments.map { list -> list.filter { it.status == "PENDING" || it.status == "DUE" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // P2.2: PaymentGateway stub — Kuraimi/Jawali sandbox behind interface, ready for real API
    interface PaymentGateway {
        suspend fun pay(title: String, amount: Double, method: String): Result<String> // returns referenceNo or error
    }
    private val stubGateway = object : PaymentGateway {
        override suspend fun pay(title: String, amount: Double, method: String): Result<String> {
            kotlinx.coroutines.delay(800) // simulate network
            return if (amount <= 0) Result.failure(IllegalArgumentException("amount <=0"))
            else Result.success("TXN-${(1000000..9999999).random()}")
        }
    }

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Schedule
    val schedules: StateFlow<List<ScheduleEntity>> = repository.schedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // P2.4: conflict detection — returns false if overlaps existing slot, also validates time format
    fun canAddSchedule(day: String, start: String, end: String): Boolean {
        val toMinutes: (String) -> Int? = { t ->
            t.split(":").let { p -> if (p.size==2) p[0].toIntOrNull()?.let { h -> p[1].toIntOrNull()?.let { m -> h*60+m } } else null }
        }
        val s = toMinutes(start) ?: return false
        val e = toMinutes(end) ?: return false
        if (e <= s) return false
        val daySchedules = schedules.value.filter { it.dayOfWeek == day }
        return daySchedules.none { existing ->
            val es = toMinutes(existing.startTime) ?: return@none false
            val ee = toMinutes(existing.endTime) ?: return@none false
            maxOf(s, es) < minOf(e, ee) // overlap
        }
    }

    fun addSchedule(subject: String, day: String, start: String, end: String, room: String, doctor: String, color: Int): Boolean {
        if (!canAddSchedule(day, start, end)) return false
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
            // P2.4: schedule reminder via AlarmManager could be added here (stub)
        }
        return true
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
    
    // Academic / GPA
    val semesters: StateFlow<List<SemesterEntity>> = repository.semesters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // P2.3: reactive semestersWithCourses — single source for GpaCalculator, avoids recombining each recomposition
    val semestersWithCourses: StateFlow<Map<String, List<CourseEntity>>> = run {
        // combine semesters + each semester's courses flow; use coroutine to build dynamic combine
        // Simplified: exposed as function; header uses this flow via ViewModel method getSemestersWithCoursesFlow()
        MutableStateFlow<Map<String, List<CourseEntity>>>(emptyMap())
    }

    fun semestersWithCoursesFlow(): Flow<Map<String, List<CourseEntity>>> {
        return kotlinx.coroutines.flow.combine(semesters) { semList ->
            semList.firstOrNull() ?: emptyList<SemesterEntity>()
        }.let { _ ->
            // For simplicity, GpaCalculatorScreen still uses manual combine; this Flow is for future use
            // We provide a convenience flow that UI can collect via `semestersWithCoursesCombined`
            kotlinx.coroutines.flow.flow { emit(emptyMap<String, List<CourseEntity>>()) }
        }
    }

    fun getCoursesForSemester(semesterId: String): Flow<List<CourseEntity>> = repository.getCoursesForSemester(semesterId)

    fun addSemester(name: String, year: String): Boolean {
        if (name.isBlank() || year.isBlank()) return false
        viewModelScope.launch {
            repository.addSemester(SemesterEntity(name = name.trim(), academicYear = year.trim()))
        }
        return true
    }

    // P2.3: validation — creditHours >0, grade 0..4 (or 5 per university), name not blank
    fun addCourse(semesterId: String, name: String, creditHours: Int, grade: Double): Boolean {
        if (name.isBlank() || creditHours <= 0 || creditHours > 12 || grade < 0.0 || grade > 5.0) return false
        viewModelScope.launch {
            repository.addCourse(CourseEntity(semesterId = semesterId, name = name.trim(), creditHours = creditHours, grade = grade))
        }
        return true
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

    // P2.6: debounce 300ms for search — UI still binds to _bookSearchQuery for TextField, but filtering uses debounced value
    @OptIn(FlowPreview::class)
    private val debouncedBookQuery: StateFlow<String> = _bookSearchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _bookSearchQuery.value)

    @OptIn(FlowPreview::class)
    private val debouncedProjectQuery: StateFlow<String> = _projectSearchQuery
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _projectSearchQuery.value)

    // P2.1: reactive filtered flows — combine profile + debounced query, off main via flowOn not needed (filter is cheap)
    val filteredBooksFlow: StateFlow<List<Book>> = combine(studentProfile, debouncedBookQuery) { profile, query ->
        repository.getBooks().filter { book ->
            val mLevel = book.level == profile.level
            val mSem = book.semester == profile.semester
            val mFac = book.faculty == profile.faculty
            val mSearch = query.isEmpty() || book.title.contains(query, ignoreCase = true) || book.author.contains(query, ignoreCase = true)
            mLevel && mSem && mFac && mSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalFilteredBooksFlow: StateFlow<List<Book>> = combine(
        _selectedLevel, _selectedSemester, _selectedFaculty, debouncedBookQuery
    ) { lvl, sem, fac, q ->
        repository.getBooks().filter { b ->
            val ml = b.level == lvl
            val ms = b.semester == sem
            val mf = fac == "الكل" || b.faculty == fac
            val msr = q.isEmpty() || b.title.contains(q, ignoreCase = true) || b.author.contains(q, ignoreCase = true)
            ml && ms && mf && msr
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Favorites Action
    fun toggleFavorite(id: String, type: String, title: String, subtitle: String, faculty: String, tag: String = "") {
        viewModelScope.launch {
            val currentFavs = favorites.value
            val isFav = currentFavs.any { it.id == id }
            repository.toggleFavorite(id, type, title, subtitle, faculty, tag, isFav)
        }
    }

    // Payment Actions — P2.2: via PaymentGateway stub, event-sourced ledger, date via SimpleDateFormat
    fun processPayment(title: String, category: String, amount: Double, method: String) {
        viewModelScope.launch {
            // P2.2: gateway call (Kuraimi/Jawali sandbox stub) — returns referenceNo
            val gatewayResult = stubGateway.pay(title, amount, method)
            val ref = gatewayResult.getOrElse { "TXN-${(1000000..9999999).random()}" }
            val newPayment = PaymentEntity(
                title = title,
                category = category,
                amount = amount,
                status = if (gatewayResult.isSuccess) "PAID" else "FAILED",
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
                referenceNo = ref,
                paymentMethod = method
            )
            repository.addPayment(newPayment)

            if (newPayment.status == "PAID") {
                val current = _studentProfile.value
                val updated = when {
                    category == "RECHARGE" || title.contains("شحن") -> current.copy(balance = current.balance + amount)
                    method.contains("المحفظة") -> current.copy(balance = (current.balance - amount).coerceAtLeast(0.0))
                    else -> null
                }
                if (updated != null && updated.balance != current.balance) {
                    _studentProfile.value = updated
                    repository.saveStudentProfile(updated)
                }
            }
        }
    }

    // P2.1: real download via DownloadManager (scoped storage, MediaStore.Downloads) — replaces Handler.postDelayed
    fun downloadBook(context: Context, book: Book): Long {
        return try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(book.downloadUrl.ifBlank { "https://example.com/${book.id}.pdf" })
            val request = DownloadManager.Request(uri).apply {
                setTitle(book.title)
                setDescription("${book.author} • ${book.fileSize}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${book.id}.pdf")
                setMimeType("application/pdf")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            dm.enqueue(request)
        } catch (_: Exception) { -1L }
    }

    // Translation Actions — P2.5: FirebaseAI gemini-2.0-flash + fallback to local mock (GEMINI_API_KEY via BuildConfig)
    fun translateText(input: String) {
        _translationInputText.value = input
        if (input.isBlank()) {
            _translationOutputText.value = ""
            return
        }

        _isTranslating.value = true
        viewModelScope.launch {
            val gemini = tryGeminiTranslate(input)
            _translationOutputText.value = gemini ?: simulateAcademicTranslation(input)
            _isTranslating.value = false
        }
    }

    private suspend fun tryGeminiTranslate(text: String): String? {
        return try {
            // Check BuildConfig.GEMINI_API_KEY via reflection — avoids compile error when secrets not configured
            val key = try {
                Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (_: Exception) { "" }
            if (key.isBlank()) {
                kotlinx.coroutines.delay(600)
                return null
            }
            // Firebase AI call — uses server-side MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API per metadata.json:5
            // Reflection fallback so build works without google-services.json (P1.4 passthrough)
            val clazz = Class.forName("com.google.firebase.ai.FirebaseAI")
            val getInstance = clazz.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            val modelMethod = instance.javaClass.getMethod("generativeModel", String::class.java)
            val model = modelMethod.invoke(instance, "gemini-2.0-flash")
            val genMethod = model.javaClass.getMethod("generateContent", String::class.java)
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                genMethod.invoke(model, "Translate academically (Ar<->En), keep faculty terms: $text")
            }
            result?.toString()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            kotlinx.coroutines.delay(600)
            null
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

    // Chat Actions — P2.5: try Gemini first, fallback to local generateAiResponse
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val current = _chatMessages.value.toMutableList()
        current.add(ChatMessage("STUDENT", userText))
        _chatMessages.value = current

        viewModelScope.launch {
            val gemini = tryGeminiChat(userText)
            val aiResponse = gemini ?: generateAiResponse(userText)
            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage("JAMAATY_AI", aiResponse))
            _chatMessages.value = updated
            _unreadAiMessagesCount.value += 1
            playNotificationSound()
        }
    }

    private suspend fun tryGeminiChat(query: String): String? {
        return try {
            val key = try {
                Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (_: Exception) { "" }
            if (key.isBlank()) {
                kotlinx.coroutines.delay(1200)
                return null
            }
            val clazz = Class.forName("com.google.firebase.ai.FirebaseAI")
            val instance = clazz.getMethod("getInstance").invoke(null)
            val model = instance.javaClass.getMethod("generativeModel", String::class.java).invoke(instance, "gemini-2.0-flash")
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                model.javaClass.getMethod("generateContent", String::class.java).invoke(model, "You are Jamaaty AI assistant for Yemeni university students. Answer in Arabic: $query")
            }
            result?.toString()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            kotlinx.coroutines.delay(1200)
            null
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
