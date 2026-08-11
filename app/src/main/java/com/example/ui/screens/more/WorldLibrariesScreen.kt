package com.example.ui.screens.more

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Project
import com.example.data.model.WorldLibrary
import androidx.compose.animation.*

@Composable
fun WorldLibrariesScreen(
    worldLibraries: List<WorldLibrary>,
    books: List<Book>,
    projects: List<Project>,
    faculties: List<String>,
    selectedFaculty: String,
    selectedLevel: Int,
    selectedSemester: Int,
    projectYearFilter: String,
    bookQuery: String,
    projectQuery: String,
    onSelectFaculty: (String) -> Unit,
    onSelectLevel: (Int) -> Unit,
    onSelectSemester: (Int) -> Unit,
    onProjectYearChange: (String) -> Unit,
    onBookSearchChange: (String) -> Unit,
    onProjectSearchChange: (String) -> Unit,
    onAddProject: (title: String, studentName: String, supervisor: String, faculty: String, year: String, level: Int, summary: String, tags: List<String>) -> Unit,
    onBack: () -> Unit
) {
    var worldLibQuery by remember { mutableStateOf("") }
    var selectedWorldLibForView by remember { mutableStateOf<WorldLibrary?>(null) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var activeGlobalTab by remember { mutableStateOf(0) } // 0: Digital Libs, 1: Global Books, 2: Global Projects
    val context = LocalContext.current

    val filteredWorldLibs = worldLibraries.filter {
        it.nameAr.contains(worldLibQuery, ignoreCase = true) ||
                it.nameEn.contains(worldLibQuery, ignoreCase = true) ||
                it.description.contains(worldLibQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("world_libraries_screen_root")
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White)
                }
                Text(
                    text = "المكتبات العالمية 🌐",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Shared Level and Faculty Filter Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Faculty Selector Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الكلية:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(faculties) { faculty ->
                                        val isSelected = selectedFaculty == faculty
                                        Surface(
                                            onClick = { onSelectFaculty(faculty) },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null
                                        ) {
                                            Text(
                                                text = faculty,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Level 1 to 5 Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..5).forEach { level ->
                                    val isSelected = (level == selectedLevel)
                                    Surface(
                                        onClick = { onSelectLevel(level) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "مستوى $level",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Semester 1 vs Semester 2 Segmented Row
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = { onSelectSemester(1) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedSemester == 1) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                contentColor = if (selectedSemester == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "الفصل الأول", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Surface(
                                onClick = { onSelectSemester(2) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedSemester == 2) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                contentColor = if (selectedSemester == 2) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "الفصل الثاني", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Header Banner card for Add Project
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎓 أبحاث ومشاريع التخرج",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "تصفح أبحاث الدفعات السابقة ووثق مشروع تخرجك في الأرشيف الأكاديمي.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showAddProjectDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة مشروع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global Search Sub-Tabs
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tabs = listOf("🌍 المكتبات", "📚 الكتب", "🎓 الأبحاث")
                            tabs.forEachIndexed { index, title ->
                                val isSelected = activeGlobalTab == index
                                Surface(
                                    onClick = { activeGlobalTab = index },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Search Bar based on tab
                    OutlinedTextField(
                        value = when(activeGlobalTab) {
                            0 -> worldLibQuery
                            1 -> bookQuery
                            else -> projectQuery
                        },
                        onValueChange = { 
                            when(activeGlobalTab) {
                                0 -> worldLibQuery = it
                                1 -> onBookSearchChange(it)
                                else -> onProjectSearchChange(it)
                            }
                        },
                        placeholder = { 
                            Text(
                                when(activeGlobalTab) {
                                    0 -> "بحث في المكتبات الرقمية والقواعد العالمية..."
                                    1 -> "بحث في الكتب والمراجع الجامعية..."
                                    else -> "بحث في الأبحاث ومشاريع التخرج..."
                                }, 
                                fontSize = 12.sp
                            ) 
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (activeGlobalTab == 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Year Filter Chips (Only for projects)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السنة:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(listOf("الكل", "2026", "2025", "2024")) { yr ->
                                    val isSelected = (projectYearFilter == yr)
                                    Surface(
                                        onClick = { onProjectYearChange(yr) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(text = yr, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when(activeGlobalTab) {
                            0 -> "المكتبات الرقمية العالمية الشريكة (${filteredWorldLibs.size}):"
                            1 -> "الكتب والمراجع المطابقة للفلترة (${books.size}):"
                            else -> "أبحاث التخرج المطابقة للفلترة (${projects.size}):"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Results list based on active tab
            if (activeGlobalTab == 0) {
                items(filteredWorldLibs) { lib ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        WorldLibraryCard(
                            library = lib,
                            onClick = { selectedWorldLibForView = lib }
                        )
                    }
                }
            } else if (activeGlobalTab == 1) {
                items(books) { book ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        // Reusing BookCard if available or simple card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = book.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = book.author, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${book.faculty} • مستوى ${book.level} • الفصل ${book.semester}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                items(projects) { proj ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = proj.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = proj.studentName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${proj.faculty} • ${proj.year}", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
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
            onSubmit = onAddProject
        )
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
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            Spacer(modifier = Modifier.width(14.dp))

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
