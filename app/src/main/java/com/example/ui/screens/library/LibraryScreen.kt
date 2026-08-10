package com.example.ui.screens.library

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Book
import com.example.data.model.Project
import com.example.data.model.WorldLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    selectedLevel: Int,
    selectedSemester: Int,
    selectedFaculty: String,
    faculties: List<String>,
    books: List<Book>,
    projects: List<Project>,
    worldLibraries: List<WorldLibrary> = emptyList(),
    bookSearchQuery: String,
    projectSearchQuery: String,
    projectYearFilter: String,
    onSelectLevel: (Int) -> Unit,
    onSelectSemester: (Int) -> Unit,
    onSelectFaculty: (String) -> Unit,
    onBookSearchChange: (String) -> Unit,
    onProjectSearchChange: (String) -> Unit,
    onProjectYearChange: (String) -> Unit,
    onToggleFavorite: (String, String, String, String, String) -> Unit,
    isFavorite: (String) -> Boolean,
    onAddNewProject: ((title: String, studentName: String, supervisor: String, faculty: String, year: String, level: Int, summary: String, tags: List<String>) -> Unit)? = null
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: الكتب والإنتاج, 1: الأبحاث والمشاريع, 2: المكتبات العالمية
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var selectedBookForReader by remember { mutableStateOf<Book?>(null) }
    var selectedProjectForDetails by remember { mutableStateOf<Project?>(null) }
    var selectedWorldLibForView by remember { mutableStateOf<WorldLibrary?>(null) }
    var showAddProjectDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("library_screen_root")
    ) {
        // Top SubTab Bar
        SecondaryTabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("📚 المقررات والكتب", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_books")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("🔬 الأبحاث والمشاريع", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_projects")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("🌐 المكتبات العالمية", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_world_libs")
            )
        }

        // Shared Level Selector (For Books and Projects)
        if (activeSubTab != 2) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المستوى الدراسي:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Faculty Chips Selector
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(faculties) { faculty ->
                                FilterChip(
                                    selected = (selectedFaculty == faculty),
                                    onClick = { onSelectFaculty(faculty) },
                                    label = { Text(faculty, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Levels 1-5 Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { level ->
                            val isSelected = (level == selectedLevel)
                            Surface(
                                onClick = { onSelectLevel(level) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lib_level_chip_$level")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "المستوى $level",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // BOOKS & COURSE MATERIALS SECTION
                val categories = listOf("الكل", "كتاب مقرر", "مرجع أساسي", "ملخص دراسي")
                val filteredBooks = books.filter { book ->
                    selectedCategoryFilter == "الكل" || book.category.contains(selectedCategoryFilter, ignoreCase = true)
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Semester 1 vs Semester 2 Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { onSelectSemester(1) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedSemester == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedSemester == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "الترم الأول (Semester 1)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            onClick = { onSelectSemester(2) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedSemester == 2) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedSemester == 2) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "الترم الثاني (Semester 2)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = (selectedCategoryFilter == cat),
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                leadingIcon = if (selectedCategoryFilter == cat) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Book Search Field
                    OutlinedTextField(
                        value = bookSearchQuery,
                        onValueChange = onBookSearchChange,
                        placeholder = { Text("بحث باسم الكتاب، اسم الدكتور، أو المرجع...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (bookSearchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onBookSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_search_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "كتب المستوى $selectedLevel • الترم $selectedSemester (${filteredBooks.size} كتاب):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredBooks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.library_empty_state_1786312083136),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "لا توجد كتب مطابقة للفلاتر الحالية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "حاول تغيير المستوى أو التصنيف أو كلمة البحث",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredBooks) { book ->
                                val isFav = isFavorite(book.id)
                                BookDetailCard(
                                    book = book,
                                    isFavorite = isFav,
                                    onToggleFav = {
                                        onToggleFavorite(
                                            book.id,
                                            "BOOK",
                                            book.title,
                                            book.author,
                                            book.faculty
                                        )
                                    },
                                    onClick = { selectedBookForReader = book }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // RESEARCHES & GRADUATION PROJECTS SECTION
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Banner card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎓 منصة أبحاث ومشاريع التخرج",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تصفح أبحاث الدفعات وقم برفع مشروع تخرجك للتوثيق الأكاديمي.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showAddProjectDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة مشروع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = projectSearchQuery,
                            onValueChange = onProjectSearchChange,
                            placeholder = { Text("بحث عن أبحاث ومشاريع بالاسم أو الكلية...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = if (projectSearchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { onProjectSearchChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح")
                                    }
                                }
                            } else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("project_search_field"),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Year Filter Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(listOf("الكل", "2026", "2025", "2024")) { yr ->
                                FilterChip(
                                    selected = (projectYearFilter == yr),
                                    onClick = { onProjectYearChange(yr) },
                                    label = { Text(yr, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "أبحاث ومشاريع التخرج المميزة (${projects.size} مشروع):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(projects) { proj ->
                            val isFav = isFavorite(proj.id)
                            ProjectCard(
                                project = proj,
                                isFavorite = isFav,
                                onToggleFav = {
                                    onToggleFavorite(
                                        proj.id,
                                        "PROJECT",
                                        proj.title,
                                        proj.studentName,
                                        proj.faculty
                                    )
                                },
                                onClickDetails = { selectedProjectForDetails = proj }
                            )
                        }
                    }
                }
            }
            2 -> {
                // WORLD LIBRARIES & DIGITAL PORTALS SECTION
                var worldLibQuery by remember { mutableStateOf("") }
                val filteredWorldLibs = worldLibraries.filter {
                    it.nameAr.contains(worldLibQuery, ignoreCase = true) ||
                            it.nameEn.contains(worldLibQuery, ignoreCase = true) ||
                            it.description.contains(worldLibQuery, ignoreCase = true)
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = worldLibQuery,
                        onValueChange = { worldLibQuery = it },
                        placeholder = { Text("بحث في المكتبات والقواعد العالمية...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "المكتبات والمعارف الرقمية المتاحة (${filteredWorldLibs.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredWorldLibs) { lib ->
                            WorldLibraryCard(
                                library = lib,
                                onClick = { selectedWorldLibForView = lib }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Book Reader & E-Book Interactive Viewer
    selectedBookForReader?.let { book ->
        BookReaderModalSheet(
            book = book,
            onDismiss = { selectedBookForReader = null },
            onToggleFavorite = {
                onToggleFavorite(book.id, "BOOK", book.title, book.author, book.faculty)
            },
            isFavorite = isFavorite(book.id)
        )
    }

    // Modal Sheet for Project Details & Download
    selectedProjectForDetails?.let { proj ->
        ProjectDetailsBottomSheet(
            project = proj,
            onDismiss = { selectedProjectForDetails = null },
            context = context
        )
    }

    // Dialog for World Library URL preview
    selectedWorldLibForView?.let { lib ->
        AlertDialog(
            onDismissRequest = { selectedWorldLibForView = null },
            icon = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(lib.nameAr, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(lib.nameEn, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(lib.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = lib.url,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "جاري فتح ${lib.nameAr} في المتصفح...", Toast.LENGTH_SHORT).show()
                    selectedWorldLibForView = null
                }) {
                    Text("انتقال للموقع")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWorldLibForView = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showAddProjectDialog) {
        AddProjectDialog(
            faculties = faculties,
            onDismiss = { showAddProjectDialog = false },
            onSubmit = { title, studentName, supervisor, faculty, year, level, summary, tags ->
                onAddNewProject?.invoke(title, studentName, supervisor, faculty, year, level, summary, tags)
                Toast.makeText(context, "تم توثيق ونشر المشروع في المكتبة الجامعية بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun BookDetailCard(
    book: Book,
    isFavorite: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.author} • ${book.faculty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = book.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${book.pages} صفحة • ⭐ ${book.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "المفضلة",
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderModalSheet(
    book: Book,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSheetTab by remember { mutableStateOf(0) } // 0: التفاصيل, 1: القارئ التفاعلي
    var currentPage by remember { mutableStateOf(12) }
    var fontSizeSp by remember { mutableStateOf(16) }
    var readerThemeMode by remember { mutableStateOf(0) } // 0: Light, 1: Sepia, 2: Dark
    var isBookmarkedPage by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val chapters = listOf(
        "المقدمة والأهداف الرئيسية",
        "الفصل الأول: المفاهيم الأساسية",
        "الفصل الثاني: التطبيقات والحلول البرمجية",
        "الفصل الثالث: التحليل والمقارنة الميدانية",
        "الخاتمة والمراجع العلمية"
    )

    val readerBgColor = when (readerThemeMode) {
        1 -> Color(0xFFFBF0D9) // Sepia
        2 -> Color(0xFF1E1E1E) // Dark
        else -> MaterialTheme.colorScheme.surface // Light
    }

    val readerTextColor = when (readerThemeMode) {
        1 -> Color(0xFF3E2723)
        2 -> Color(0xFFE0E0E0)
        else -> MaterialTheme.colorScheme.onSurface
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${book.author} • ${book.faculty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "مفضلة",
                            tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }

            // Tabs inside Sheet: Details vs Reader
            SecondaryTabRow(selectedTabIndex = selectedSheetTab) {
                Tab(
                    selected = selectedSheetTab == 0,
                    onClick = { selectedSheetTab = 0 },
                    text = { Text("📋 تفاصيل المرجع", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSheetTab == 1,
                    onClick = { selectedSheetTab = 1 },
                    text = { Text("📖 القارئ الإلكتروني والمعاينة", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSheetTab == 0) {
                // DETAILS VIEW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("عدد الصفحات", style = MaterialTheme.typography.labelSmall)
                                Text("${book.pages}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Divider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("حجم الملف", style = MaterialTheme.typography.labelSmall)
                                Text(book.fileSize, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Divider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("التقييم", style = MaterialTheme.typography.labelSmall)
                                Text("⭐ ${book.rating}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("وصف المرجع والمحتوى العلمي:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isDownloading) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                progress = downloadProgress,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("جاري تنزيل الكتاب PDF... ${(downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!isDownloaded) {
                                    isDownloading = true
                                    downloadProgress = 0.2f
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        downloadProgress = 0.7f
                                    }, 400)
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        downloadProgress = 1.0f
                                        isDownloading = false
                                        isDownloaded = true
                                        Toast.makeText(context, "تم حفظ ${book.title} في التنزيلات المحفوظة", Toast.LENGTH_SHORT).show()
                                    }, 800)
                                } else {
                                    Toast.makeText(context, "الملف محمل مسبقاً جاهز للقراءة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDownloaded) "تم حفظ النسخة الرقمية (PDF جاهز)" else "تحميل المرجع الكامل (${book.fileSize})")
                        }
                    }
                }
            } else {
                // E-READER VIEW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Controls Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Font Size Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (fontSizeSp > 12) fontSizeSp -= 2 }) {
                                Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("$fontSizeSp sp", style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { if (fontSizeSp < 24) fontSizeSp += 2 }) {
                                Text("A+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        // Theme Mode Selector
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { readerThemeMode = 0 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (readerThemeMode == 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            ) {
                                Icon(Icons.Default.LightMode, contentDescription = "نهاري", modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { readerThemeMode = 1 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (readerThemeMode == 1) Color(0xFFFBF0D9) else Color.Transparent)
                            ) {
                                Text("📜", fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { readerThemeMode = 2 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (readerThemeMode == 2) Color(0xFF1E1E1E) else Color.Transparent)
                            ) {
                                Icon(Icons.Default.DarkMode, contentDescription = "ليلي", modifier = Modifier.size(18.dp))
                            }
                        }

                        // Bookmark Page
                        IconButton(onClick = {
                            isBookmarkedPage = !isBookmarkedPage
                            Toast.makeText(
                                context,
                                if (isBookmarkedPage) "تم حفظ العلامة عند الصفحة $currentPage" else "تم إزالة العلامة",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                imageVector = if (isBookmarkedPage) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Page",
                                tint = if (isBookmarkedPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Reader View Frame
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        color = readerBgColor
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الفصل الثاني: البناء والتطوير",
                                    fontSize = 11.sp,
                                    color = readerTextColor.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "صفحة $currentPage من ${book.pages}",
                                    fontSize = 11.sp,
                                    color = readerTextColor.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "تعتبر هذه الوحدة من أهم الوحدات العلمية في مقرر ${book.title}. حيث تتطرق المادة الشاملة إلى التفاصيل التقنية والنظرية المعتمدة من قبل المدرس المباشر ${book.author}.\n\n" +
                                        "1. مفاهيم الربط والتحليل المباشر للمنظومات.\n" +
                                        "2. طرق تحسين الأداء ورفع الكفاءة التشغيلية للأكاديميين والطلاب.\n" +
                                        "3. تطبيقات عملية وحلول معتمدة لخطة الفصل الدراسي الحالية.",
                                fontSize = fontSizeSp.sp,
                                color = readerTextColor,
                                lineHeight = (fontSizeSp + 8).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Page Navigation Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الصفحة السابقة", fontSize = 12.sp)
                        }

                        Text(
                            text = "صفحة $currentPage / ${book.pages}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { if (currentPage < book.pages) currentPage++ },
                            enabled = currentPage < book.pages,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("الصفحة التالية", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    isFavorite: Boolean,
    onToggleFav: () -> Unit,
    onClickDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClickDetails,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${project.faculty} • دفعة ${project.year}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onToggleFav, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "مفضلة",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "إعداد الطالب: ${project.studentName} | إشراف: ${project.supervisor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                project.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "#$tag", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsBottomSheet(
    project: Project,
    onDismiss: () -> Unit,
    context: Context
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var downloaded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفاصيل ملخص مشروع البحث",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "الكلية: ${project.faculty} | السنة الدراسية: ${project.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "التحليل الملخص للمشروع:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Citation Copier
            OutlinedButton(
                onClick = {
                    val apaCitation = "${project.studentName} (${project.year}). ${project.title}. مشروع تخرج، ${project.faculty}، بإشراف ${project.supervisor}."
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("APA Citation", apaCitation)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "تم نسخ التوثيق العلمي (APA) للحافظة", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("نسخ التوثيق العلمي للمشروع (APA Citation)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { downloaded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (downloaded) Icons.Default.PictureAsPdf else Icons.Default.Download,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (downloaded) "تم تحميل الملحق بنجاح (${project.fileSize})" else "تنزيل ملف البحث الكامل (${project.fileFormat} - ${project.fileSize})"
                )
            }
        }
    }
}

@Composable
fun WorldLibraryCard(
    library: WorldLibrary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = library.nameAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = library.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = library.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.OpenInNew,
                contentDescription = "فتح الرابط",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    faculties: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (title: String, studentName: String, supervisor: String, faculty: String, year: String, level: Int, summary: String, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var selectedFaculty by remember { mutableStateOf(if (faculties.isNotEmpty() && faculties.first() != "الكل") faculties.first() else "كلية الحاسوب وتكنولوجيا المعلومات") }
    var selectedYear by remember { mutableStateOf("2026") }
    var selectedLevel by remember { mutableIntStateOf(4) }
    var summary by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة مشروع تخرج / بحث جديد", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان البحث / المشروع") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("اسم الطالب / أعضاء الفريق") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = supervisor,
                    onValueChange = { supervisor = it },
                    label = { Text("اسم المشرف العلمي (الدكتور)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("الكلية المعنية:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(faculties.filter { it != "الكل" }) { fac ->
                        FilterChip(
                            selected = (selectedFaculty == fac),
                            onClick = { selectedFaculty = fac },
                            label = { Text(fac, fontSize = 10.sp) }
                        )
                    }
                }

                Text("السنة الدراسية:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(listOf("2026", "2025", "2024", "2023")) { yr ->
                        FilterChip(
                            selected = (selectedYear == yr),
                            onClick = { selectedYear = yr },
                            label = { Text(yr, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("ملخص ومخرجات المشروع") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("الكلمات المفتاحية (تفصل بفاصلة)") },
                    placeholder = { Text("مثال: ذكاء اصطناعي, طاقة, أمن") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || studentName.isBlank()) return@Button
                    val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSubmit(
                        title,
                        studentName,
                        supervisor.ifBlank { "د. مشرف القسم" },
                        selectedFaculty,
                        selectedYear,
                        selectedLevel,
                        summary.ifBlank { "مشروع تخرج موثق ومدرج بالجامعة." },
                        if (tags.isEmpty()) listOf("مشروع تخرج", selectedFaculty) else tags
                    )
                    onDismiss()
                },
                enabled = title.isNotBlank() && studentName.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("نشر المشروع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
