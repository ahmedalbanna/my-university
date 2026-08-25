package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AiChatDialog
import com.example.ui.components.BottomTab
import com.example.ui.components.DeepLinkDialog
import com.example.ui.components.JamaatyBottomBar
import com.example.ui.components.JamaatyTopBar
import com.example.ui.components.OfflineBanner
import com.example.ui.components.RequestNotificationPermissionIfNeeded
import com.example.ui.navigation.DeepLinkParser
import com.example.ui.navigation.DeepLinkResult
import com.example.ui.navigation.MainNavGraph
import com.example.ui.navigation.navigateToTab
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.gpa.GpaCalculatorScreen
import com.example.ui.screens.more.TranslateScreen
import com.example.ui.screens.more.WorldLibrariesScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.onboarding.AuthScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.onboarding.ProfileSetupScreen
import com.example.ui.screens.profile.StudentProfileScreen
import com.example.ui.viewmodel.JamaatyViewModel

@Composable
fun JamaatyApp(
    initialDeepLinkUri: Uri? = null,
    onDeepLinkHandled: () -> Unit = {},
    viewModel: JamaatyViewModel = viewModel()
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    // P1.2: Navigation — NavController for bottom tabs (system back = Nav back)
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: BottomTab.Home.route
    val currentTab = BottomTab.all.find { it.route == currentRoute } ?: BottomTab.Home

    var showAiChatSheet by remember { mutableStateOf(false) }
    var showDeepLinkDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showGpaCalculator by remember { mutableStateOf(false) }
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

    val translationInput by viewModel.translationInputText.collectAsState()
    val translationOutput by viewModel.translationOutputText.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()

    val favorites by viewModel.favorites.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val unreadAiMessagesCount by viewModel.unreadAiMessagesCount.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Deep Link Application Function — P1.2: uses NavController so back stack is correct
    val applyDeepLink: (Uri) -> Unit = remember(viewModel, navController) {
        { uri ->
            when (val result = DeepLinkParser.parse(uri)) {
                is DeepLinkResult.NavigateTab -> {
                    BottomTab.all.find { it.route == result.tabRoute }?.let { targetTab ->
                        navController.navigateToTab(targetTab.route)
                    }

                    result.level?.let { viewModel.setLevel(it) }
                    result.semester?.let { viewModel.setSemester(it) }
                    result.faculty?.let { viewModel.setFaculty(it) }

                    if (result.tabRoute == "library") {
                        result.searchQuery?.let { viewModel.setBookSearchQuery(it) }
                    }

                    if (result.openChat) showAiChatSheet = true
                    if (result.openTranslate) showTranslate = true
                    if (result.openWorldLibraries) showWorldLibraries = true
                    if (result.openGpa) showGpaCalculator = true
                    if (result.openProfile) showProfile = true
                    if (result.openFavorites) navController.navigateToTab(BottomTab.Favorites.route)
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

    // System Back Handler — P1.2: overlays first, then NavController back stack, then Home
    BackHandler(enabled = showAiChatSheet || showDeepLinkDialog || showNotifications || showGpaCalculator || showCourses || showTranslate || showWorldLibraries || showProfile || currentRoute != BottomTab.Home.route) {
        when {
            showAiChatSheet -> showAiChatSheet = false
            showDeepLinkDialog -> showDeepLinkDialog = false
            showNotifications -> showNotifications = false
            showGpaCalculator -> showGpaCalculator = false
            showCourses -> showCourses = false
            showTranslate -> showTranslate = false
            showWorldLibraries -> showWorldLibraries = false
            showProfile -> showProfile = false
            currentRoute != BottomTab.Home.route -> navController.navigateToTab(BottomTab.Home.route)
            else -> {}
        }
    }

    // Build current active screen's deep link for sharing/copying
    val currentDeepLink = remember(
        currentRoute, selectedLevel, selectedSemester, selectedFaculty,
        bookQuery
    ) {
        DeepLinkParser.buildLink(
            route = currentRoute,
            level = if (currentRoute == BottomTab.Library.route) selectedLevel else null,
            semester = if (currentRoute == BottomTab.Library.route) selectedSemester else null,
            faculty = if (currentRoute == BottomTab.Library.route) selectedFaculty else null,
            searchQuery = if (currentRoute == BottomTab.Library.route) bookQuery else null,
            difficulty = null,
            tag = null
        )
    }

    // Force RTL for Arabic layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // P1.4: request POST_NOTIFICATIONS once when app starts (Android 13+)
        RequestNotificationPermissionIfNeeded()
        if (!onboardingCompleted) {
            OnboardingScreen(
                onFinish = { viewModel.completeOnboarding() }
            )
        } else if (!isLoggedIn) {
            val authError by viewModel.authError.collectAsState()
            val isAuthLoading by viewModel.isAuthLoading.collectAsState()
            AuthScreen(
                onLoginSuccess = { email, pass -> viewModel.login(email, pass) },
                onSignUpSuccess = { email, pass -> viewModel.signUp(email, pass) },
                onSignUpWithConfirm = { email, pass, confirm -> viewModel.signUpWithConfirm(email, pass, confirm) },
                authError = authError,
                isLoading = isAuthLoading,
                onClearError = { viewModel.clearAuthError() }
            )
        } else if (profile.name.isBlank()) {
            // P0-5: was isEmpty() dead code because StudentProfile defaults are non-empty; signUp now emits blank so gate works
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
                    onOpenFavorites = { navController.navigateToTab(BottomTab.Favorites.route) },
                    onOpenNotifications = { showNotifications = true },
                    onOpenProfile = { showProfile = true },
                    onOpenDeepLinkTester = { showDeepLinkDialog = true }
                )
            },
            bottomBar = {
                JamaatyBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab -> navController.navigateToTab(tab.route) }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("jamaaty_main_scaffold")
        ) { innerPadding ->
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // P3-3: offline banner (visible when no internet via NetworkMonitor)
                OfflineBanner()
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // P1.2: NavHost handles bottom tabs + deep links; system back = Nav back
                    MainNavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        onOpenCourses = { showCourses = true },
                        onOpenTranslate = { showTranslate = true },
                        onOpenGpa = { showGpaCalculator = true },
                        onOpenWorldLibraries = { showWorldLibraries = true },
                        onOpenProfile = { showProfile = true },
                        onOpenSchedule = { navController.navigateToTab(BottomTab.Schedule.route) }
                    )
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
                currentRoute = currentRoute,
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
                var currentGovernorate by remember { mutableStateOf("") }
                var currentUniversities by remember { mutableStateOf(viewModel.getUniversities()) }

                WorldLibrariesScreen(
                    books = viewModel.getGlobalFilteredBooks(),
                    projects = viewModel.getGlobalFilteredProjects(),
                    governorates = viewModel.getGovernorates(),
                    universities = currentUniversities,
                    faculties = viewModel.getFaculties(),
                    selectedFaculty = selectedFaculty,
                    selectedLevel = selectedLevel,
                    selectedSemester = selectedSemester,
                    projectYearFilter = projectYear,
                    onSelectGovernorate = { gov ->
                        currentGovernorate = gov
                        currentUniversities = viewModel.getUniversitiesForGovernorate(gov)
                    },
                    onSelectFaculty = { viewModel.setFaculty(it) },
                    onSelectLevel = { viewModel.setLevel(it) },
                    onSelectSemester = { viewModel.setSemester(it) },
                    onProjectYearChange = { yr -> viewModel.setProjectYearFilter(yr) },
                    onAddProject = { title, student, supervisor, faculty, year, level, summary, tags ->
                        viewModel.addNewProject(title, student, supervisor, faculty, year, level, summary, tags)
                    },
                    getMajorsForFaculty = { viewModel.getMajorsForFaculty(it) },
                    getSemesters = { viewModel.getSemesters() },
                    getCourseDetails = { viewModel.getCourseDetails(it) },
                    getCourses = { f, m, l, s -> viewModel.getCourses(f, m, l, s) },
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
