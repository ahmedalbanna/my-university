package com.example.ui.screens.more

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Book
import com.example.data.model.Project
import com.example.data.repository.JamaatyRepository
import com.example.ui.screens.more.worldlibrary.*

@Composable
fun WorldLibrariesScreen(
    books: List<Book>,
    projects: List<Project>,
    governorates: List<String>,
    universities: List<String>,
    faculties: List<String>,
    selectedFaculty: String,
    selectedLevel: Int,
    selectedSemester: Int,
    projectYearFilter: String,
    onSelectGovernorate: (String) -> Unit,
    onSelectFaculty: (String) -> Unit,
    onSelectLevel: (Int) -> Unit,
    onSelectSemester: (Int) -> Unit,
    onProjectYearChange: (String) -> Unit,
    onAddProject: (title: String, studentName: String, supervisor: String, faculty: String, year: String, level: Int, summary: String, tags: List<String>) -> Unit,
    getMajorsForFaculty: (String) -> List<String>,
    getSemesters: () -> List<String>,
    getCourses: (faculty: String, major: String, level: Int, semester: String) -> List<String>,
    getCourseDetails: (String) -> JamaatyRepository.CourseInfo,
    onBack: () -> Unit
) {
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var activeGlobalTab by remember { mutableStateOf(1) }
    val context = LocalContext.current

    // Selection Flow State
    var selectionStep by remember { mutableStateOf(0) } // 0-6: Steps, 7: Content
    var currentGov by remember { mutableStateOf("") }
    var currentUni by remember { mutableStateOf("") }
    var currentFac by remember { mutableStateOf("") }
    var currentMajor by remember { mutableStateOf("") }
    var currentLevel by remember { mutableStateOf(1) }
    var currentSem by remember { mutableStateOf("") }
    var currentCourse by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectionStep > 0) selectionStep--
                        else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White)
                    }
                    Text(
                        text = when(selectionStep) {
                            0 -> "المحافظة 📍"
                            1 -> "الجامعة 🏛️"
                            2 -> "الكلية 🎓"
                            3 -> "التخصص 📝"
                            4 -> "المستوى 🔢"
                            5 -> "الفصل 🗓️"
                            6 -> "المقرر 📖"
                    else -> "المحتوى الأكاديمي 📚"
                },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = selectionStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "SelectionFlow"
            ) { step ->
                when (step) {
                    0 -> SelectionList(
                        title = "حدد المحافظة اليمنية",
                        items = governorates,
                        icon = Icons.Default.Map,
                        onSelect = {
                            currentGov = it
                            onSelectGovernorate(it)
                            selectionStep = 1
                        }
                    )
                    1 -> SelectionList(
                        title = "حدد الجامعة في محافظة $currentGov",
                        items = universities,
                        icon = Icons.Default.LocationCity,
                        onSelect = {
                            currentUni = it
                            selectionStep = 2
                        }
                    )
                    2 -> SelectionList(
                        title = "حدد الكلية التابعة لجامعة $currentUni",
                        items = faculties.filter { it != "الكل" },
                        icon = Icons.Default.School,
                        onSelect = {
                            currentFac = it
                            onSelectFaculty(it)
                            selectionStep = 3
                        }
                    )
                    3 -> SelectionList(
                        title = "حدد تخصصك في $currentFac",
                        items = getMajorsForFaculty(currentFac),
                        icon = Icons.Default.ListAlt,
                        onSelect = {
                            currentMajor = it
                            selectionStep = 4
                        }
                    )
                    4 -> SelectionList(
                        title = "حدد السنة الدراسية",
                        items = listOf("السنة الأولى", "السنة الثانية", "السنة الثالثة", "السنة الرابعة", "السنة الخامسة"),
                        icon = Icons.Default.Filter1,
                        onSelect = {
                            currentLevel = when(it) {
                                "السنة الأولى" -> 1
                                "السنة الثانية" -> 2
                                "السنة الثالثة" -> 3
                                "السنة الرابعة" -> 4
                                else -> 5
                            }
                            selectionStep = 5
                        }
                    )
                    5 -> SelectionList(
                        title = "حدد الفصل الدراسي",
                        items = getSemesters(),
                        icon = Icons.Default.CalendarToday,
                        onSelect = {
                            currentSem = it
                            selectionStep = 6
                        }
                    )
                    6 -> SelectionList(
                        title = "حدد المقرر الدراسي",
                        items = getCourses(currentFac, currentMajor, currentLevel, currentSem),
                        icon = Icons.Default.MenuBook,
                        onSelect = {
                            currentCourse = it
                            selectionStep = 7
                        }
                    )
                    else -> LibraryContentView(
                        books = books,
                        projects = projects,
                        projectYearFilter = projectYearFilter,
                        activeGlobalTab = activeGlobalTab,
                        onActiveTabChange = { activeGlobalTab = it },
                        onProjectYearChange = onProjectYearChange,
                        onAddProjectClick = { showAddProjectDialog = true },
                        selectedCourse = currentCourse,
                        getCourseDetails = getCourseDetails
                    )
                }
            }
        }
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AddProjectDialog(
            faculties = faculties,
            onDismiss = { showAddProjectDialog = false },
            onSubmit = onAddProject
        )
    }
}
