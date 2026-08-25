package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.ui.components.BottomTab
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.gpa.GpaCalculatorScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.more.TranslateScreen
import com.example.ui.screens.more.WorldLibrariesScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.profile.StudentProfileScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.viewmodel.JamaatyViewModel

/**
 * P1.2 — NavHost for bottom tabs + deep links.
 * Bottom tabs use NavHost so system back = Nav back and deep links produce proper back stack.
 * Overlay screens (Translate, GPA, WorldLibraries, etc.) remain as overlay booleans in JamaatyApp for now
 * but are also reachable as nested NavHost destinations for future full migration.
 */
@Composable
fun MainNavGraph(
    navController: NavHostController,
    viewModel: JamaatyViewModel,
    onOpenCourses: () -> Unit,
    onOpenTranslate: () -> Unit,
    onOpenGpa: () -> Unit,
    onOpenWorldLibraries: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSchedule: () -> Unit
) {
    val profile by viewModel.studentProfile.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val bookQuery by viewModel.bookSearchQuery.collectAsState()
    val projectQuery by viewModel.projectSearchQuery.collectAsState()
    // P2.1: reactive flows (debounce 300ms, combine) — used for Home/Library to avoid recompute on every recomposition
    val filteredBooks by viewModel.filteredBooksFlow.collectAsState()
    val globalFilteredBooks by viewModel.globalFilteredBooksFlow.collectAsState()

    NavHost(
        navController = navController,
        startDestination = BottomTab.Home.route
    ) {
        composable(
            route = BottomTab.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "jamaaty://home" }, navDeepLink { uriPattern = "https://jamaaty.app/home" })
        ) {
            HomeScreen(
                profile = profile,
                recommendedBooks = filteredBooks,
                onNavigateToTab = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                },
                onNavigateToSchedule = { navController.navigate(BottomTab.Schedule.route) },
                onNavigateToCourses = onOpenCourses,
                onToggleFavorite = { id, type, title, sub, fac -> viewModel.toggleFavorite(id, type, title, sub, fac) },
                isFavorite = { id -> favorites.any { it.id == id } }
            )
        }
        composable(
            route = BottomTab.Library.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "jamaaty://library?level={level}&faculty={faculty}&q={q}" },
                navDeepLink { uriPattern = "https://jamaaty.app/library" }
            )
        ) {
            LibraryScreen(
                profile = profile,
                books = filteredBooks,
                projects = viewModel.getFilteredProjects(),
                bookSearchQuery = bookQuery,
                projectSearchQuery = projectQuery,
                onBookSearchChange = { viewModel.setBookSearchQuery(it) },
                onProjectSearchChange = { viewModel.setProjectSearchQuery(it) },
                onToggleFavorite = { id, type, title, sub, fac -> viewModel.toggleFavorite(id, type, title, sub, fac) },
                isFavorite = { id -> favorites.any { it.id == id } }
            )
        }
        composable(
            route = BottomTab.Schedule.route,
            deepLinks = listOf(navDeepLink { uriPattern = "jamaaty://schedule" })
        ) {
            ScheduleScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = BottomTab.Wallet.route,
            deepLinks = listOf(navDeepLink { uriPattern = "jamaaty://wallet" })
        ) {
            WalletScreen(
                profile = profile,
                payments = payments,
                onProcessPayment = { title, cat, amt, method -> viewModel.processPayment(title, cat, amt, method) }
            )
        }
        composable(
            route = BottomTab.Favorites.route,
            deepLinks = listOf(navDeepLink { uriPattern = "jamaaty://favorites" })
        ) {
            FavoritesScreen(
                favorites = favorites,
                onToggleFavorite = { id, type, title, sub, fac -> viewModel.toggleFavorite(id, type, title, sub, fac) }
            )
        }
        composable(
            route = BottomTab.More.route,
            deepLinks = listOf(navDeepLink { uriPattern = "jamaaty://more" })
        ) {
            MoreScreen(
                onNavigateToTranslate = onOpenTranslate,
                onOpenGpaCalculator = onOpenGpa,
                onOpenLibraries = onOpenWorldLibraries,
                onOpenProfile = onOpenProfile,
                onLogout = { viewModel.logout() }
            )
        }
        // Overlay destinations also exposed as nav routes for deep-link completeness (P1.2 future)
        composable("translate") {
            val translationInput by viewModel.translationInputText.collectAsState()
            val translationOutput by viewModel.translationOutputText.collectAsState()
            val isTranslating by viewModel.isTranslating.collectAsState()
            TranslateScreen(
                translationInput = translationInput,
                translationOutput = translationOutput,
                isTranslating = isTranslating,
                vocabularyCards = viewModel.getVocabularyCards(),
                onTranslateInput = { viewModel.translateText(it) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("gpa") {
            GpaCalculatorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("worldLibraries") {
            val selectedLevel by viewModel.selectedLevel.collectAsState()
            val selectedSemester by viewModel.selectedSemester.collectAsState()
            val selectedFaculty by viewModel.selectedFaculty.collectAsState()
            val projectYear by viewModel.projectYearFilter.collectAsState()
            WorldLibrariesScreen(
                books = globalFilteredBooks,
                projects = viewModel.getGlobalFilteredProjects(),
                governorates = viewModel.getGovernorates(),
                universities = viewModel.getUniversities(),
                faculties = viewModel.getFaculties(),
                selectedFaculty = selectedFaculty,
                selectedLevel = selectedLevel,
                selectedSemester = selectedSemester,
                projectYearFilter = projectYear,
                onSelectGovernorate = { _ -> },
                onSelectFaculty = { viewModel.setFaculty(it) },
                onSelectLevel = { viewModel.setLevel(it) },
                onSelectSemester = { viewModel.setSemester(it) },
                onProjectYearChange = { viewModel.setProjectYearFilter(it) },
                onAddProject = { t, s, sup, f, y, l, sum, tags -> viewModel.addNewProject(t, s, sup, f, y, l, sum, tags) },
                getMajorsForFaculty = { viewModel.getMajorsForFaculty(it) },
                getSemesters = { viewModel.getSemesters() },
                getCourseDetails = { viewModel.getCourseDetails(it) },
                getCourses = { f, m, l, s -> viewModel.getCourses(f, m, l, s) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("notifications") {
            NotificationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("profile") {
            StudentProfileScreen(profile = profile, onBack = { navController.popBackStack() }, onEditProfile = {})
        }
        composable("courses") {
            CoursesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}

fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
    }
}
