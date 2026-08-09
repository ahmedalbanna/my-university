package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.ui.components.JamaatyBottomBar
import com.example.ui.components.JamaatyTopBar
import com.example.ui.screens.exams.ExamsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.more.TranslateMoreScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.viewmodel.JamaatyViewModel

@Composable
fun JamaatyApp(
    viewModel: JamaatyViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }
    var showAiChatSheet by remember { mutableStateOf(false) }

    // Observe ViewModel state
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
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Force RTL for Arabic layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                JamaatyTopBar(
                    selectedLevel = selectedLevel,
                    studentName = profile.name,
                    favoritesCount = favorites.size,
                    onOpenAiChat = { showAiChatSheet = true },
                    onOpenFavorites = { currentTab = BottomTab.TranslateMore }
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
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        BottomTab.Home -> {
                            HomeScreen(
                                profile = profile,
                                selectedLevel = selectedLevel,
                                recommendedBooks = viewModel.getFilteredBooks(),
                                onSelectLevel = { lvl -> viewModel.setLevel(lvl) },
                                onNavigateToTab = { route ->
                                    BottomTab.list.find { it.route == route }?.let { currentTab = it }
                                },
                                onToggleFavorite = { id, type, title, sub, fac ->
                                    viewModel.toggleFavorite(id, type, title, sub, fac)
                                },
                                isFavorite = { id -> favorites.any { it.id == id } }
                            )
                        }
                        BottomTab.Library -> {
                            LibraryScreen(
                                selectedLevel = selectedLevel,
                                selectedSemester = selectedSemester,
                                selectedFaculty = selectedFaculty,
                                faculties = viewModel.getFaculties(),
                                books = viewModel.getFilteredBooks(),
                                projects = viewModel.getFilteredProjects(),
                                bookSearchQuery = bookQuery,
                                projectSearchQuery = projectQuery,
                                projectYearFilter = projectYear,
                                onSelectLevel = { lvl -> viewModel.setLevel(lvl) },
                                onSelectSemester = { sem -> viewModel.setSemester(sem) },
                                onSelectFaculty = { fac -> viewModel.setFaculty(fac) },
                                onBookSearchChange = { q -> viewModel.setBookSearchQuery(q) },
                                onProjectSearchChange = { q -> viewModel.setProjectSearchQuery(q) },
                                onProjectYearChange = { yr -> viewModel.setProjectYearFilter(yr) },
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
                        BottomTab.TranslateMore -> {
                            TranslateMoreScreen(
                                translationInput = translationInput,
                                translationOutput = translationOutput,
                                isTranslating = isTranslating,
                                vocabularyCards = viewModel.getVocabularyCards(),
                                worldLibraries = viewModel.getWorldLibraries(),
                                examResults = viewModel.getExamResults(),
                                favorites = favorites,
                                onTranslateInput = { text -> viewModel.translateText(text) },
                                onToggleFavorite = { id, type, title, sub, fac ->
                                    viewModel.toggleFavorite(id, type, title, sub, fac)
                                }
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
                onDismiss = { showAiChatSheet = false }
            )
        }
    }
}
