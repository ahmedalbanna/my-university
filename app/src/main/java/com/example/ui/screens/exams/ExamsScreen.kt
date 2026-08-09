package com.example.ui.screens.exams

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.QuestionVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    questions: List<Question>,
    searchQuery: String,
    selectedDifficulty: String,
    selectedTag: String,
    selectedAnswers: Map<Int, Int>,
    quizSubmitted: Boolean,
    onSearchChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onAnswerSelected: (Int, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onResetQuiz: () -> Unit
) {
    var selectedQuestionForHistory by remember { mutableStateOf<Question?>(null) }
    var practiceMode by remember { mutableStateOf(false) } // Browse vs Interactive Exam Practice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("exams_screen_root")
    ) {
        // Top Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📝 منصة نماذج الاختبارات وبنك الأسئلة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تمارين تفاعلية وسجل مراجعة تاريخ الإصدارات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mode Switch Button
                    Surface(
                        onClick = { practiceMode = !practiceMode },
                        shape = RoundedCornerShape(12.dp),
                        color = if (practiceMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (practiceMode) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (practiceMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (practiceMode) "تصفح الأسئلة" else "بدء اختبار تفاعلي",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (practiceMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("بحث عن سؤال، موضوع أكاديمي، أو كلمة رئيسية...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Difficulty Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مستوى الصعوبة: ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("الكل", "سهل", "متوسط", "صعب")) { diff ->
                            FilterChip(
                                selected = (selectedDifficulty == diff),
                                onClick = { onDifficultyChange(diff) },
                                label = { Text(diff, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Tags Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الوسوم: ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("الكل", "#خوارزميات", "#OOP", "#فيزياء", "#طب", "#SQL")) { tag ->
                            FilterChip(
                                selected = (selectedTag == tag),
                                onClick = { onTagChange(tag) },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (practiceMode) {
            // INTERACTIVE QUIZ MODE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Score Summary Banner
                if (quizSubmitted) {
                    val scoreCount = questions.indices.count { idx ->
                        selectedAnswers[idx] == questions[idx].correctIndex
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉 نتيجة الاختبار التفاعلي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "حصلت على $scoreCount من أصل ${questions.size} إجابات صحيحة",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onResetQuiz) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(questions) { qIndex, question ->
                        QuizQuestionCard(
                            questionIndex = qIndex,
                            question = question,
                            selectedOption = selectedAnswers[qIndex],
                            showResult = quizSubmitted,
                            onOptionSelected = { optionIdx -> onAnswerSelected(qIndex, optionIdx) }
                        )
                    }
                }

                if (!quizSubmitted && questions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSubmitQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_quiz_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إنهاء وتصحيح الاختبار", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // QUESTION BANK BROWSE & VERSION HISTORY
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "بنك الأسئلة المتاح (${questions.size} سؤال):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(questions) { q ->
                        QuestionBrowseCard(
                            question = q,
                            onViewHistory = { selectedQuestionForHistory = q }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Revision & Version History Log
    selectedQuestionForHistory?.let { q ->
        QuestionHistoryBottomSheet(
            question = q,
            onDismiss = { selectedQuestionForHistory = null }
        )
    }
}

@Composable
fun QuizQuestionCard(
    questionIndex: Int,
    question: Question,
    selectedOption: Int?,
    showResult: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "السؤال ${questionIndex + 1} • ${question.subject}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            when (question.difficulty) {
                                "سهل" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                "متوسط" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                else -> Color(0xFFF44336).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = question.difficulty,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (question.difficulty) {
                            "سهل" -> Color(0xFF2E7D32)
                            "متوسط" -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            question.options.forEachIndexed { optIndex, optionText ->
                val isSelected = (selectedOption == optIndex)
                val isCorrect = (optIndex == question.correctIndex)

                val optionColor = when {
                    showResult && isCorrect -> Color(0xFF4CAF50)
                    showResult && isSelected && !isCorrect -> Color(0xFFF44336)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Surface(
                    onClick = { if (!showResult) onOptionSelected(optIndex) },
                    shape = RoundedCornerShape(10.dp),
                    color = optionColor.copy(alpha = if (isSelected || (showResult && isCorrect)) 0.15f else 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { if (!showResult) onOptionSelected(optIndex) },
                            enabled = !showResult
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = optionText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (showResult) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "💡 الشرح الأكاديمي:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = question.explanation, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionBrowseCard(
    question: Question,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${question.faculty} • ${question.subject}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // History & Revision Log Button
                Surface(
                    onClick = onViewHistory,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سجل التعديلات",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                question.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionHistoryBottomSheet(
    question: Question,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سجل التحديثات والمراجعة الأكاديمية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "السؤال: ${question.questionText}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "تاريخ الإصدارات والمراجعات التاريخية:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (question.versionHistory.isEmpty()) {
                Text(text = "لا توجد تعديلات سابقة لهذا السؤال.", fontSize = 12.sp)
            } else {
                question.versionHistory.forEach { version ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "الإصدار ${version.version}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = version.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = "بواسطة: ${version.modifiedBy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "ملاحظة التعديل: ${version.changeNote}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
