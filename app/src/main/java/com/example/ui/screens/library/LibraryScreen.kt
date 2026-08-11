package com.example.ui.screens.library

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.StudentProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    profile: StudentProfile,
    books: List<Book>,
    projects: List<Project>,
    bookSearchQuery: String,
    projectSearchQuery: String,
    onBookSearchChange: (String) -> Unit,
    onProjectSearchChange: (String) -> Unit,
    onToggleFavorite: (String, String, String, String, String) -> Unit,
    isFavorite: (String) -> Boolean
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: الكتب والإنتاج, 1: الأبحاث والمشاريع
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var selectedBookForReader by remember { mutableStateOf<Book?>(null) }
    var selectedProjectForDetails by remember { mutableStateOf<Project?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("library_screen_root")
    ) {
        // Library Hero Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "المكتبة والإنتاج الأكاديمي 📖",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${books.size} كتاب مقرر • ${projects.size} بحث تخرج",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // SubTab Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    0 to "📚 الكتب والمراجع",
                    1 to "🔬 الأبحاث والمشاريع"
                )

                tabs.forEach { (index, title) ->
                    val isSelected = activeSubTab == index
                    Surface(
                        onClick = { activeSubTab = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = activeSubTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it * direction }
                )) togetherWith (fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it * direction }
                ))
            },
            label = "SubTabTransition"
        ) { subTab ->
            when (subTab) {
                0 -> {
                // BOOKS & COURSE MATERIALS SECTION
                val categories = listOf("الكل", "كتاب مقرر", "مرجع أساسي", "ملخص دراسي")
                val filteredBooks = books.filter { book ->
                    (selectedCategoryFilter == "الكل" || book.category.contains(selectedCategoryFilter, ignoreCase = true))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    // Category Chips & Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                Surface(
                                    onClick = { selectedCategoryFilter = cat },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Book Search Field
                    OutlinedTextField(
                        value = bookSearchQuery,
                        onValueChange = onBookSearchChange,
                        placeholder = { Text("بحث باسم الكتاب، اسم الدكتور، أو المرجع...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = if (bookSearchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onBookSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_search_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "كتب المستوى ${profile.level} • الفصل ${profile.semester} (${filteredBooks.size} كتاب):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (filteredBooks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.library_empty_state_1786312083136),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لا توجد كتب مطابقة للفلاتر الحالية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "حاول تغيير المستوى الدراسي أو الكلية من المكتبة العالمية.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
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
                val filteredProjects = projects

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    // Header Banner card
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
                            placeholder = { Text("بحث عن أبحاث بالاسم...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = if (projectSearchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { onProjectSearchChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                    }
                                }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_search_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "أبحاث ومشاريع التخرج الموثقة (${filteredProjects.size} مشروع):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredProjects) { proj ->
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
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
                    imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
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
    var selectedSheetTab by remember { mutableIntStateOf(0) } // 0: التفاصيل, 1: القارئ التفاعلي
    var currentPage by remember { mutableIntStateOf(12) }
    var fontSizeSp by remember { mutableIntStateOf(16) }
    var readerThemeMode by remember { mutableIntStateOf(0) } // 0: Light, 1: Sepia, 2: Dark
    var isBookmarkedPage by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("عدد الصفحات", style = MaterialTheme.typography.labelSmall)
                                Text("${book.pages}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("حجم الملف", style = MaterialTheme.typography.labelSmall)
                                Text(book.fileSize, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp))
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
                                progress = { downloadProgress },
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "مفضلة",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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
