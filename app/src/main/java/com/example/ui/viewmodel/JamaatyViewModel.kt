package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.model.Book
import com.example.data.model.ExamResult
import com.example.data.model.Project
import com.example.data.model.Question
import com.example.data.model.StudentProfile
import com.example.data.model.VocabularyCard
import com.example.data.model.WorldLibrary
import com.example.data.repository.JamaatyRepository
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

    private val repository: JamaatyRepository

    init {
        val dao = AppDatabase.getDatabase(application).appDao()
        repository = JamaatyRepository(dao)

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
                    paymentMethod = "بطاقة مدى / Mada"
                )
            )
            repository.addPayment(
                PaymentEntity(
                    title = "اشتراك باقة الطلاب للاتصالات 50GB",
                    category = "TELECOM",
                    amount = 120.0,
                    status = "PAID",
                    date = "2026-02-01",
                    referenceNo = "TXN-9931002",
                    paymentMethod = "المحفظة الرقمية"
                )
            )
        }
    }

    // Onboarding State
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        _onboardingCompleted.value = true
    }

    // Student Profile
    private val _studentProfile = MutableStateFlow(repository.getStudentProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    // Level & Faculty Filters
    private val _selectedLevel = MutableStateFlow(3) // Default level 3
    val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

    private val _selectedSemester = MutableStateFlow(1) // 1 or 2
    val selectedSemester: StateFlow<Int> = _selectedSemester.asStateFlow()

    private val _selectedFaculty = MutableStateFlow("الكل")
    val selectedFaculty: StateFlow<String> = _selectedFaculty.asStateFlow()

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

    // Books list filtered by level, semester, faculty, search
    fun getFilteredBooks(): List<Book> {
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

    // Researches & Projects filtered
    fun getFilteredProjects(): List<Project> {
        return repository.getProjects().filter { proj ->
            val matchFaculty = (_selectedFaculty.value == "الكل" || proj.faculty == _selectedFaculty.value)
            val matchYear = (_projectYearFilter.value == "الكل" || proj.year == _projectYearFilter.value)
            val matchSearch = _projectSearchQuery.value.isEmpty() ||
                    proj.title.contains(_projectSearchQuery.value, ignoreCase = true) ||
                    proj.studentName.contains(_projectSearchQuery.value, ignoreCase = true) ||
                    proj.tags.any { it.contains(_projectSearchQuery.value, ignoreCase = true) }

            matchFaculty && matchYear && matchSearch
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

            // Deduct balance if using balance
            if (method.contains("المحفظة")) {
                val current = _studentProfile.value
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
            kotlinx.coroutines.delay(800)
            val aiResponse = generateAiResponse(userText)
            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage("JAMAATY_AI", aiResponse))
            _chatMessages.value = updated
        }
    }

    private fun generateAiResponse(query: String): String {
        return when {
            query.contains("سعر", ignoreCase = true) || query.contains("رسوم", ignoreCase = true) ->
                "يمكنك تسديد الرسوم الدراسية أو رسوم السكن والساعات من خلال قسم 'محفظتي والخدمات المالية'. تتوفر طرق دفع سريعة عبر مدى، البطاقات الائتمانية والمحفظة الرقمية."
            query.contains("كتاب", ignoreCase = true) || query.contains("مرجع", ignoreCase = true) ->
                "تتوفر جميع كتب المناهج الدراسية للترم الأول والثاني في قسم 'المكتبة والمستوى الدراسي'. يمكنك اختيار مستواك (من 1 إلى 5) وتنزيل المراجع مباشرة!"
            query.contains("اختبار", ignoreCase = true) || query.contains("امتحان", ignoreCase = true) ->
                "منصة الاختبارات في قسم 'نماذج الاختبارات' تحتوي على بنك أسئلة تفاعلي مع حلول مفصلة وسجل المراجعة والتحديثات التاريخية للأسئلة."
            query.contains("بحث", ignoreCase = true) || query.contains("مشروع", ignoreCase = true) ->
                "يمكنك استعراض أبحاث ومشاريع التخرج السابقة من قسم الأبحاث والمشاريع، وتصفيتها حسب السنة والكلية لقراءة التحليل وتنزيل الملحقات."
            else ->
                "أنا هنا لمساعدتك! يمكنك الاستفسار عن المناهج، الرسوم، بنك الأسئلة، أو ترجمة المصطلحات الأكاديمية الصعبة."
        }
    }
}
