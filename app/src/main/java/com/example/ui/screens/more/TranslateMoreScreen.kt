package com.example.ui.screens.more

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.FavoriteEntity
import com.example.data.model.ExamResult
import com.example.data.model.VocabularyCard
import com.example.data.model.WorldLibrary

private data class FavTypeInfo(
    val label: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val textColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateMoreScreen(
    translationInput: String,
    translationOutput: String,
    isTranslating: Boolean,
    vocabularyCards: List<VocabularyCard>,
    worldLibraries: List<WorldLibrary>,
    examResults: List<ExamResult>,
    favorites: List<FavoriteEntity>,
    onTranslateInput: (String) -> Unit,
    onToggleFavorite: (String, String, String, String, String) -> Unit,
    onOpenGpaCalculator: () -> Unit
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: المترجم الأكاديمي, 1: المفضلة, 2: نتائج الامتحانات, 3: مكتبات عالمية
    var sourceLang by remember { mutableStateOf("الإنجليزية") }
    var targetLang by remember { mutableStateOf("العربية") }
    var vocabSearchQuery by remember { mutableStateOf("") }
    var favSearchQuery by remember { mutableStateOf("") }
    var favSelectedType by remember { mutableStateOf("ALL") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("translate_more_screen_root")
    ) {
        // Hero Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "الترجمة والأدوات الأكاديمية 🌐",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "قاموس فورى • مرجعيات • نتائج • مكتبات رقمية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Quick GPA Badge
                        Surface(
                            onClick = onOpenGpaCalculator,
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "حاسبة GPA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Segmented SubTab Mode Switcher
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
                    0 to "🔤 الترجمة",
                    1 to "🔖 المفضلة (${favorites.size})",
                    2 to "📊 النتائج",
                    3 to "🌍 مكتبات"
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
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tab Content
        when (activeSubTab) {
            0 -> {
                // ACADEMIC TRANSLATION & FLASHCARDS
                val filteredVocab = vocabularyCards.filter { v ->
                    vocabSearchQuery.isBlank() ||
                            v.termAr.contains(vocabSearchQuery, ignoreCase = true) ||
                            v.termEn.contains(vocabSearchQuery, ignoreCase = true) ||
                            v.definitionAr.contains(vocabSearchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Language Swap Direction Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = sourceLang,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val temp = sourceLang
                                            sourceLang = targetLang
                                            targetLang = temp
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "تبديل اللغات",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = targetLang,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Input Text Field
                                OutlinedTextField(
                                    value = translationInput,
                                    onValueChange = onTranslateInput,
                                    placeholder = { Text("أدخل النص أو المصطلح الأكاديمي للترجمة المباشرة...", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .testTag("translation_input_field"),
                                    shape = RoundedCornerShape(14.dp),
                                    trailingIcon = if (translationInput.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { onTranslateInput("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "مسح النص", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    } else null
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Sample Tag Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("خوارزميات البرمجة", "Data Structures", "Quantum Physics", "الهندسة المعمارية", "Medical Terms")) { sample ->
                                        Surface(
                                            onClick = { onTranslateInput(sample) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "+ $sample",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Loading state vs Output Box
                                if (isTranslating) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "جاري معالجة النص وضبط المصطلحات الأكاديمية...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (translationOutput.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "الترجمة الأكاديمية المعتمدة:",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            Toast.makeText(context, "تم نسخ الترجمة الحافظة", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            Toast.makeText(context, "تمت إضافة المصطلح إلى قاموسك الشخصي", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.BookmarkAdd, contentDescription = "حفظ", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = translationOutput,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 بطاقات المصطلحات الأكاديمية (Flashcards):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${filteredVocab.size} مصطلح",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = vocabSearchQuery,
                            onValueChange = { vocabSearchQuery = it },
                            placeholder = { Text("بحث في المصطلحات والمفاهيم...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = if (vocabSearchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { vocabSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else null,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (filteredVocab.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد مصطلحات تطابق البحث الحرفي",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredVocab) { card ->
                            VocabularyFlashcard(card = card)
                        }
                    }
                }
            }

            1 -> {
                // FAVORITES MANAGEMENT
                val filteredFavorites = favorites.filter { fav ->
                    val matchesType = when (favSelectedType) {
                        "ALL" -> true
                        "BOOK" -> fav.type == "BOOK"
                        "PROJECT" -> fav.type == "PROJECT"
                        "LIBRARY" -> fav.type == "LIBRARY"
                        "QUESTION" -> fav.type == "QUESTION"
                        else -> true
                    }
                    val matchesSearch = favSearchQuery.isBlank() ||
                            fav.title.contains(favSearchQuery, ignoreCase = true) ||
                            fav.subtitle.contains(favSearchQuery, ignoreCase = true) ||
                            fav.faculty.contains(favSearchQuery, ignoreCase = true)
                    matchesType && matchesSearch
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "المفضلة والمرجعيات الأكاديمية",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "إجمالي العناصر المحفوظة: ${favorites.size}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (favorites.isNotEmpty()) {
                                                Toast.makeText(context, "جاري تصدير قائمة المفضلة الأكاديمية بصيغة TXT/PDF...", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "قائمة المفضلة فارغة حالياً", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "تصدير القائمة",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = favSearchQuery,
                                onValueChange = { favSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("بحث في العناصر المحفوظة بالاسم أو الكلية...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                trailingIcon = {
                                    if (favSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { favSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val filters = listOf(
                                    "ALL" to "الكل (${favorites.size})",
                                    "BOOK" to "📚 كتب (${favorites.count { it.type == "BOOK" }})",
                                    "PROJECT" to "🔬 أبحاث (${favorites.count { it.type == "PROJECT" }})",
                                    "LIBRARY" to "🌐 مكتبات (${favorites.count { it.type == "LIBRARY" }})",
                                    "QUESTION" to "💡 اختبارات (${favorites.count { it.type == "QUESTION" }})"
                                )

                                items(filters) { (typeKey, label) ->
                                    val isSelected = favSelectedType == typeKey
                                    Surface(
                                        onClick = { favSelectedType = typeKey },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredFavorites.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkRemove,
                                        contentDescription = null,
                                        modifier = Modifier.size(54.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (favorites.isEmpty()) "لم تقم بإضافة أية مرجعيات للمفضلة بعد" else "لا توجد نتائج تطابق بحثك الفعلي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "يمكنك حفظ الكتب والأبحاث والمكتبات بالضغط على أيقونة الإشارة المرجعية 🔖",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredFavorites) { fav ->
                            val favInfo = when (fav.type) {
                                "BOOK" -> FavTypeInfo("كتاب دراسي", Icons.Default.MenuBook, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                                "PROJECT" -> FavTypeInfo("مشروع بحثي", Icons.Default.Science, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                                "LIBRARY" -> FavTypeInfo("مكتبة رقمية", Icons.Default.Public, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                                "QUESTION" -> FavTypeInfo("سؤال / اختبار", Icons.Default.Quiz, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                                else -> FavTypeInfo("مرجع محفوظ", Icons.Default.Bookmark, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = favInfo.badgeColor
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = favInfo.icon,
                                                    contentDescription = null,
                                                    tint = favInfo.textColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = favInfo.label,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = favInfo.textColor
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    Toast.makeText(context, "تم نسخ رابط المرجع: ${fav.title}", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "مشاركة",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    onToggleFavorite(fav.id, fav.type, fav.title, fav.subtitle, fav.faculty)
                                                    Toast.makeText(context, "تمت إزالة المرجع من المفضلة", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = fav.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${fav.subtitle} • ${fav.faculty}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            onClick = {
                                                Toast.makeText(context, "فتح معاينة المرجع: ${fav.title}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "معاينة المرجع",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // EXAM RESULTS & GPA DASHBOARD
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Grade,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "كشف نتائج الامتحانات والتقييم الأكاديمي 🎓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "المعدل التراكمي للفصل الحالي",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "3.84 / 4.00",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "التقدير العام: ممتاز مرتفع (Excellent)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onOpenGpaCalculator,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("حاسبة GPA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Surface(
                                        onClick = {
                                            Toast.makeText(context, "جاري طباعة وتصدير كشف الدرجات الأكاديمي بصيغة PDF...", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تصدير الكشف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تفاصيل المواد والنتائج المسجلة:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${examResults.size} مواد معتمدة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(examResults) { res ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (res.score >= 90) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.secondaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = res.grade,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = if (res.score >= 90) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = res.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = res.code,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${res.credits} ساعات معتمدة",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${res.score}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (res.score >= 90) "ممتاز" else if (res.score >= 80) "جيد جداً" else "ناجح",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // WORLD LIBRARIES
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المكتبات الرقمية العالمية الموثوقة 🌐",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "وصول مباشر لقواعد البيانات والأبحاث العالمية المعتمدة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(worldLibraries) { lib ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = lib.nameAr,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = lib.nameEn,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            onToggleFavorite(lib.id, "LIBRARY", lib.nameAr, lib.nameEn, "مكتبة عالمية")
                                            Toast.makeText(context, "تم حفظ المكتبة في المفضلة 🔖", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkBorder,
                                            contentDescription = "حفظ",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = lib.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lib.url,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Surface(
                                        onClick = {
                                            Toast.makeText(context, "جاري فتح الرابط: ${lib.url}", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "زيارة الموقع",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyFlashcard(card: VocabularyCard) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = card.termAr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = card.termEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "توسيع",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "الشرح بالعربية: ${card.definitionAr}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "English Definition: ${card.definitionEn}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💡 مثال: ${card.example}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
