package com.example.ui.screens.more.worldlibrary

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Project
import com.example.data.repository.JamaatyRepository

@Composable
fun LibraryContentView(
    books: List<Book>,
    projects: List<Project>,
    projectYearFilter: String,
    activeGlobalTab: Int,
    onActiveTabChange: (Int) -> Unit,
    onProjectYearChange: (String) -> Unit,
    onAddProjectClick: () -> Unit,
    selectedCourse: String,
    getCourseDetails: (String) -> JamaatyRepository.CourseInfo
) {
    val context = LocalContext.current
    val courseInfo = if (selectedCourse.isNotEmpty()) getCourseDetails(selectedCourse) else null
    
    val courseBooks = if (selectedCourse.isNotEmpty()) {
        books.filter { it.title.contains(selectedCourse, ignoreCase = true) }
    } else books

    val availableTabs = mutableListOf<Triple<Int, String, String>>()
    availableTabs.add(Triple(1, "📚 المقررات", "Textbooks"))
    
    if (courseInfo == null || courseInfo.hasReferences) {
        availableTabs.add(Triple(2, "📖 المراجع", "References"))
    }
    if (courseInfo == null || courseInfo.hasExams) {
        availableTabs.add(Triple(3, "📝 النماذج", "Exams"))
    }
    if (courseInfo != null && courseInfo.hasProjects) {
        availableTabs.add(Triple(4, "💡 المشاريع", "Projects"))
    }
    if (courseInfo != null && courseInfo.hasResearch) {
        availableTabs.add(Triple(5, "🔬 الأبحاث", "Research"))
    }

    var safeActiveTab = activeGlobalTab
    if (availableTabs.none { it.first == safeActiveTab }) {
        safeActiveTab = 1
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedCourse.isNotEmpty()) {
                    SelectedCourseHeader(selectedCourse, courseInfo)
                }

                TabSelector(
                    availableTabs = availableTabs,
                    activeTab = safeActiveTab,
                    onTabSelected = onActiveTabChange
                )

                if (safeActiveTab == 4 || safeActiveTab == 5) {
                    YearFilterRow(projectYearFilter, onProjectYearChange)
                }

                if (courseInfo?.hasProjects == true) {
                    GraduationProjectsBanner(onAddProjectClick)
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionTitle(safeActiveTab, selectedCourse, books.size, courseBooks.size)
            }
        }

        items(
            items = when (safeActiveTab) {
                1, 2, 3 -> courseBooks
                else -> projects
            },
            key = { item -> 
                when(item) {
                    is Book -> "book_${item.title}_${item.author}"
                    is Project -> "proj_${item.title}_${item.studentName}"
                    else -> item.hashCode()
                }
            }
        ) { item ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .animateItem()
            ) {
                when (item) {
                    is Book -> ResourceBookCard(
                        book = item,
                        tabType = safeActiveTab,
                        onDownload = { Toast.makeText(context, "جاري تحميل: ${item.title}", Toast.LENGTH_SHORT).show() }
                    )
                    is Project -> ResourceProjectCard(
                        project = item,
                        tabType = safeActiveTab,
                        onDownload = { Toast.makeText(context, "جاري تحميل: ${item.title}", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedCourseHeader(selectedCourse: String, courseInfo: JamaatyRepository.CourseInfo?) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("المقرر المختار:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                Text(selectedCourse, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (courseInfo != null) {
                    Text(
                        text = "المحتوى المتاح: الكتب ${if(courseInfo.hasExams) "+ النماذج" else ""} ${if(courseInfo.hasProjects) "+ المشاريع" else ""}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GraduationProjectsBanner(onAddProjectClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("🎓 أبحاث ومشاريع التخرج", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("تصفح أبحاث الدفعات السابقة ووثق مشروع تخرجك.", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onAddProjectClick, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TabSelector(
    availableTabs: List<Triple<Int, String, String>>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        LazyRow(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(availableTabs) { (id, title, _) ->
                val isSelected = activeTab == id
                Surface(
                    onClick = { onTabSelected(id) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 80.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearFilterRow(selectedYear: String, onYearChange: (String) -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("السنة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(listOf("الكل", "2026", "2025", "2024")) { yr ->
                val isSelected = selectedYear == yr
                Surface(
                    onClick = { onYearChange(yr) },
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

@Composable
private fun SectionTitle(tab: Int, course: String, count: Int, courseBookCount: Int) {
    Text(
        text = when(tab) {
            1 -> if (course.isNotEmpty()) "الكتب المقررة لمقرر $course ($courseBookCount):" else "الكتب والمراجع المطابقة ($count):"
            2 -> "المراجع المساعدة المتاحة:"
            3 -> "نماذج الاختبارات والتقييمات:"
            4 -> "أبحاث التخرج والمشاريع المتاحة:"
            else -> "الأوراق البحثية والندوات:"
        },
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}
