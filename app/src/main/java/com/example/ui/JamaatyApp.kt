package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AiChatDialog
import com.example.ui.components.BottomTab
import com.example.ui.components.DeepLinkDialog
import com.example.ui.components.JamaatyBottomBar
import com.example.ui.components.JamaatyTopBar
import com.example.ui.navigation.DeepLinkParser
import com.example.ui.navigation.DeepLinkResult
import com.example.ui.screens.exams.ExamsScreen
import com.example.ui.screens.gpa.GpaCalculatorScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.more.TranslateScreen
import com.example.ui.screens.more.WorldLibrariesScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.profile.StudentProfileScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.onboarding.ProfileSetupScreen
import com.example.ui.screens.onboarding.AuthScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.results.ResultsScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.viewmodel.JamaatyViewModel

@Composable
fun JamaatyApp(
    initialDeepLinkUri: Uri? = null,
    onDeepLinkHandled: () -> Unit = {},
    viewModel: JamaatyViewModel = viewModel()
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    var currentTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }
    var showAiChatSheet by remember { mutableStateOf(false) }
    var showDeepLinkDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showGpaCalculator by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var showCourses by remember { mutableStateOf(false) }
    var showTranslate by remember { mutableStateOf(false) }
    var showWorldLibraries by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }

    // Observe ViewModel state
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val selectedFaculty by viewModel.selectedFaculty.collectAsState()

    val bookQuery by viewModel.bookSearchQuery.collectAsState()
    val projectQuery by viewModel.projectSearchQuery.collectAsState()
    val projectYear by viewModel.projectYearFilter.collectAsState()

    val questionQuery by viewModel.questionSearchQuery.collectAsState()
    val difficulty by viewModel.selectedDifficulty.collectAsState()
    val questionTag by viewModel.selectedQuestionTag.collectAsState()

    val selectedQuizAnswers by viewModel.selectedQuizAnswers.collectAsState()
    val quizSubmitted by viewModel.quizSubmitted.collectAsState()

    val translationInput by viewModel.translationInputText.collectAsState()
    val translationOutput by viewModel.translationOutputText.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()

    val favorites by viewModel.favorites.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val unreadAiMessagesCount by viewModel.unreadAiMessagesCount.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Deep Link Application Function
    val applyDeepLink: (Uri) -> Unit = remember(viewModel) {
        { uri ->
            when (val result = DeepLinkParser.parse(uri)) {
                is DeepLinkResult.NavigateTab -> {
                    BottomTab.list.find { it.route == result.tabRoute }?.let { targetTab ->
                        currentTab = targetTab
                    }

                    result.level?.let { viewModel.setLevel(it) }
                    result.semester?.let { viewModel.setSemester(it) }
                    result.faculty?.let { viewModel.setFaculty(it) }

                    if (result.tabRoute == "library") {
                        result.searchQuery?.let { viewModel.setBookSearchQuery(it) }
                    } else if (result.tabRoute == "exams") {
                        result.searchQuery?.let { viewModel.setQuestionSearchQuery(it) }
                    }

                    result.difficulty?.let { viewModel.setDifficulty(it) }
                    result.tag?.let { viewModel.setQuestionTag(it) }

                    if (result.openChat) {
                        showAiChatSheet = true
                    }
                }
                DeepLinkResult.Invalid -> { /* ignore invalid link */ }
            }
        }
    }

    // Clear AI unread count when chat is opened
    LaunchedEffect(showAiChatSheet) {
        if (showAiChatSheet) {
            viewModel.markAiChatAsRead()
        }
    }

    // Process initial or incoming deep link URI
    LaunchedEffect(initialDeepLinkUri) {
        initialDeepLinkUri?.let { uri ->
            applyDeepLink(uri)
            onDeepLinkHandled()
        }
    }

    // System Back Handler Refinement
    BackHandler(enabled = showAiChatSheet || showDeepLinkDialog || showNotifications || showGpaCalculator || showSchedule || showCourses || showTranslate || showWorldLibraries || showProfile || currentTab != BottomTab.Home) {
        when {
            showAiChatSheet -> showAiChatSheet = false
            showDeepLinkDialog -> showDeepLinkDialog = false
            showNotifications -> showNotifications = false
            showGpaCalculator -> showGpaCalculator = false
            showSchedule -> showSchedule = false
            showCourses -> showCourses = false
            showTranslate -> showTranslate = false
            showWorldLibraries -> showWorldLibraries = false
            showProfile -> showProfile = false
            currentTab != BottomTab.Home -> currentTab = BottomTab.Home
        }
    }

    // Build current active screen's deep link for sharing/copying
    val currentDeepLink = remember(
        currentTab, selectedLevel, selectedSemester, selectedFaculty,
        bookQuery, questionQuery, difficulty, questionTag
    ) {
        DeepLinkParser.buildLink(
            route = currentTab.route,
            level = if (currentTab == BottomTab.Library) selectedLevel else null,
            semester = if (currentTab == BottomTab.Library) selectedSemester else null,
            faculty = if (currentTab == BottomTab.Library) selectedFaculty else null,
            searchQuery = if (currentTab == BottomTab.Library) bookQuery else if (currentTab == BottomTab.Exams) questionQuery else null,
            difficulty = if (currentTab == BottomTab.Exams) difficulty else null,
            tag = if (currentTab == BottomTab.Exams) questionTag else null
        )
    }

    // Force RTL for Arabic layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (!onboardingCompleted) {
            OnboardingScreen(
                onFinish = { viewModel.completeOnboarding() }
            )
        } else if (!isLoggedIn) {
            AuthScreen(
                onLoginSuccess = { email, pass -> viewModel.login(email, pass) },
                onSignUpSuccess = { email, pass -> viewModel.signUp(email, pass) }
            )
        } else if (profile.name.isEmpty()) {
            ProfileSetupScreen(
                initialProfile = profile,
                universities = viewModel.getUniversities(),
                faculties = viewModel.getFaculties().filter { it != "الكل" },
                getMajorsForFaculty = { fac -> viewModel.getMajorsForFaculty(fac) },
                onSaveProfileAndFinish = { name, id, uni, fac, maj, lvl ->
                    viewModel.updateStudentProfile(name, id, uni, fac, maj, lvl)
                }
            )
        } else {
            Scaffold(
                topBar = {
                JamaatyTopBar(
                    selectedLevel = selectedLevel,
                    studentName = profile.name,
                    favoritesCount = favorites.size,
                    unreadNotificationsCount = unreadNotificationsCount,
                    unreadAiMessagesCount = unreadAiMessagesCount,
                    onOpenAiChat = { showAiChatSheet = true },
                    onOpenFavorites = { currentTab = BottomTab.Favorites },
                    onOpenNotifications = { showNotifications = true },
                    onOpenProfile = { showProfile = true },
                    onOpenDeepLinkTester = { showDeepLinkDialog = true }
                )
            },
            bottomBar = {
                JamaatyBottomBar(
                    currentRoute = currentTab.route,
                    onTabSelected = { tab -> currentTab = tab }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("jamaaty_main_scaffold")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val direction = if (BottomTab.list.indexOf(targetState) > BottomTab.list.indexOf(initialState)) 1 else -1
                        (fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { it * direction }
                        )) togetherWith (fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { -it * direction }
                        ))
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        BottomTab.Home -> {
                            HomeScreen(
                                profile = profile,
                                recommendedBooks = viewModel.getFilteredBooks(),
                                onNavigateToTab = { route ->
                                    BottomTab.list.find { it.route == route }?.let { currentTab = it }
                                },
                                onNavigateToSchedule = { showSchedule = true },
                                onNavigateToCourses = { showCourses = true },
                                onOpenResults = { currentTab = BottomTab.Results },
                                onToggleFavorite = { id, type, title, sub, fac ->
                                    viewModel.toggleFavorite(id, type, title, sub, fac)
                                },
                                isFavorite = { id -> favorites.any { it.id == id } }
                            )
                        }
                        BottomTab.Library -> {
                            LibraryScreen(
                                profile = profile,
                                books = viewModel.getFilteredBooks(),
                                projects = viewModel.getFilteredProjects(),
                                bookSearchQuery = bookQuery,
                                projectSearchQuery = projectQuery,
                                onBookSearchChange = { q -> viewModel.setBookSearchQuery(q) },
                                onProjectSearchChange = { q -> viewModel.setProjectSearchQuery(q) },
                                onToggleFavorite = { id, type, title, sub, fac ->
                                    viewModel.toggleFavorite(id, type, title, sub, fac)
                                },
                                isFavorite = { id -> favorites.any { it.id == id } }
                            )
                        }
                        BottomTab.Exams -> {
                            ExamsScreen(
                                questions = viewModel.getFilteredQuestions(),
                                searchQuery = questionQuery,
                                selectedDifficulty = difficulty,
                                selectedTag = questionTag,
                                selectedAnswers = selectedQuizAnswers,
                                quizSubmitted = quizSubmitted,
                                onSearchChange = { q -> viewModel.setQuestionSearchQuery(q) },
                                onDifficultyChange = { d -> viewModel.setDifficulty(d) },
                                onTagChange = { t -> viewModel.setQuestionTag(t) },
                                onAnswerSelected = { qIdx, optIdx -> viewModel.answerQuizQuestion(qIdx, optIdx) },
                                onSubmitQuiz = { viewModel.submitQuiz() },
                                onResetQuiz = { viewModel.resetQuiz() }
                            )
                        }
                        BottomTab.Wallet -> {
                            WalletScreen(
                                profile = profile,
                                payments = payments,
                                onProcessPayment = { title, cat, amt, method ->
                                    viewModel.processPayment(title, cat, amt, method)
                                }
                            )
                        }
                        BottomTab.Favorites -> {
                            FavoritesScreen(
                                favorites = favorites,
                                onToggleFavorite = { id, type, title, sub, fac ->
                                    viewModel.toggleFavorite(id, type, title, sub, fac)
                                }
                            )
                        }
                        BottomTab.Results -> {
                            ResultsScreen(
                                examResults = viewModel.getExamResults(),
                                onOpenGpaCalculator = { showGpaCalculator = true }
                            )
                        }
                        BottomTab.More -> {
                            MoreScreen(
                                onNavigateToTranslate = { showTranslate = true },
                                onOpenGpaCalculator = { showGpaCalculator = true },
                                onOpenLibraries = { showWorldLibraries = true },
                                onOpenResults = { currentTab = BottomTab.Results },
                                onOpenProfile = { showProfile = true },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }

        // AI Chat Helper Dialog
        if (showAiChatSheet) {
            AiChatDialog(
                messages = chatMessages,
                onSendMessage = { text -> viewModel.sendChatMessage(text) },
                onDismiss = { showAiChatSheet = false },
                onClearChat = { viewModel.clearChat() }
            )
        }

        // Deep Link Dialog & Tester
        if (showDeepLinkDialog) {
            DeepLinkDialog(
                currentRoute = currentTab.route,
                currentLink = currentDeepLink,
                onExecuteDeepLink = applyDeepLink,
                onDismiss = { showDeepLinkDialog = false }
            )
        }

        AnimatedVisibility(
            visible = showNotifications,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                NotificationScreen(
                    viewModel = viewModel,
                    onBack = { showNotifications = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showGpaCalculator,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                GpaCalculatorScreen(
                    viewModel = viewModel,
                    onBack = { showGpaCalculator = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showSchedule,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                ScheduleScreen(
                    viewModel = viewModel,
                    onBack = { showSchedule = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showCourses,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                CoursesScreen(
                    viewModel = viewModel,
                    onBack = { showCourses = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showTranslate,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                TranslateScreen(
                    translationInput = translationInput,
                    translationOutput = translationOutput,
                    isTranslating = isTranslating,
                    vocabularyCards = viewModel.getVocabularyCards(),
                    onTranslateInput = { text -> viewModel.translateText(text) },
                    onBack = { showTranslate = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showWorldLibraries,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                WorldLibrariesScreen(
                    worldLibraries = viewModel.getWorldLibraries(),
                    books = viewModel.getGlobalFilteredBooks(),
                    projects = viewModel.getGlobalFilteredProjects(),
                    faculties = viewModel.getFaculties(),
                    selectedFaculty = selectedFaculty,
                    selectedLevel = selectedLevel,
                    selectedSemester = selectedSemester,
                    projectYearFilter = projectYear,
                    bookQuery = bookQuery,
                    projectQuery = projectQuery,
                    onSelectFaculty = { viewModel.setFaculty(it) },
                    onSelectLevel = { viewModel.setLevel(it) },
                    onSelectSemester = { viewModel.setSemester(it) },
                    onProjectYearChange = { yr -> viewModel.setProjectYearFilter(yr) },
                    onBookSearchChange = { q -> viewModel.setBookSearchQuery(q) },
                    onProjectSearchChange = { q -> viewModel.setProjectSearchQuery(q) },
                    onAddProject = { title, student, supervisor, faculty, year, level, summary, tags ->
                        viewModel.addNewProject(title, student, supervisor, faculty, year, level, summary, tags)
                    },
                    onBack = { showWorldLibraries = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showProfile,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
                StudentProfileScreen(
                    profile = profile,
                    onBack = { showProfile = false },
                    onEditProfile = { /* Future: Implement editing */ }
                )
            }
        }
    }
}
}
