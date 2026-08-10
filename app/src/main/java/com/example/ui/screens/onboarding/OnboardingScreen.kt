package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudentProfile
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    initialProfile: StudentProfile = StudentProfile(),
    universities: List<String> = listOf(
        "جامعة الملك سعود",
        "جامعة الملك عبدالعزيز",
        "جامعة أم القرى",
        "جامعة الملك فهد للبترول والمعادن",
        "جامعة القاهرة",
        "جامعة الملك خالد",
        "جامعة الإمام محمد بن سعود الإسلامية"
    ),
    faculties: List<String> = listOf(
        "كلية الحاسبات والمعلومات",
        "كلية الهندسة",
        "كلية الطب والجراحة",
        "كلية إدارة الأعمال",
        "كلية العلوم"
    ),
    getMajorsForFaculty: (String) -> List<String> = { faculty ->
        when (faculty) {
            "كلية الحاسبات والمعلومات" -> listOf("علوم الحاسب والذكاء الاصطناعي", "هندسة البرمجيات", "نظم المعلومات", "الأمن السيبراني")
            "كلية الهندسة" -> listOf("الهندسة المدنية", "الهندسة الكهربائية", "الهندسة الميكانيكية", "الهندسة المعمارية")
            "كلية الطب والجراحة" -> listOf("الطب والجراحة العامة", "طب وجراحة الأسنان", "الصيدلة الإكلينيكية")
            "كلية إدارة الأعمال" -> listOf("إدارة الأعمال", "المحاسبة والمالية", "التسويق الرقمي")
            else -> listOf("علوم الحاسب والذكاء الاصطناعي", "هندسة البرمجيات", "تخصص عام")
        }
    },
    onSaveProfileAndFinish: (name: String, studentId: String, university: String, faculty: String, major: String, level: Int) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) } // 0: Overview, 1: Institution & Faculty, 2: Student Profile

    // User Selection States
    var selectedUniversity by remember { mutableStateOf(initialProfile.university) }
    var selectedFaculty by remember { mutableStateOf(initialProfile.faculty) }
    var selectedMajor by remember { mutableStateOf(initialProfile.major) }
    var studentName by remember { mutableStateOf(initialProfile.name) }
    var studentId by remember { mutableStateOf(initialProfile.studentId) }
    var selectedLevel by remember { mutableIntStateOf(initialProfile.level) }

    val availableMajors = remember(selectedFaculty) { getMajorsForFaculty(selectedFaculty) }

    // Keep selected major valid when faculty changes
    LaunchedEffect(selectedFaculty) {
        if (!availableMajors.contains(selectedMajor) && availableMajors.isNotEmpty()) {
            selectedMajor = availableMajors.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        // Step Header Bar
        OnboardingHeaderBar(
            currentStep = currentStep,
            onBack = { if (currentStep > 0) currentStep-- }
        )

        AnimatedContent(
            targetState = currentStep,
            modifier = Modifier.weight(1f),
            label = "OnboardingStepTransition"
        ) { step ->
            when (step) {
                0 -> FeatureOverviewStep(
                    onNext = { currentStep = 1 }
                )
                1 -> InstitutionSelectionStep(
                    universities = universities,
                    faculties = faculties,
                    availableMajors = availableMajors,
                    selectedUniversity = selectedUniversity,
                    selectedFaculty = selectedFaculty,
                    selectedMajor = selectedMajor,
                    onUniversitySelected = { selectedUniversity = it },
                    onFacultySelected = { selectedFaculty = it },
                    onMajorSelected = { selectedMajor = it },
                    onNext = { currentStep = 2 }
                )
                2 -> ProfileDataStep(
                    studentName = studentName,
                    studentId = studentId,
                    selectedLevel = selectedLevel,
                    onNameChange = { studentName = it },
                    onIdChange = { studentId = it },
                    onLevelChange = { selectedLevel = it },
                    onFinish = {
                        onSaveProfileAndFinish(
                            studentName.ifBlank { "أحمد محمد العباسي" },
                            studentId.ifBlank { "2024-CSC-88412" },
                            selectedUniversity,
                            selectedFaculty,
                            selectedMajor,
                            selectedLevel
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingHeaderBar(
    currentStep: Int,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // RTL back arrow
                            contentDescription = "السابق",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = when (currentStep) {
                        0 -> "مرحباً بك في منصة جامعتي"
                        1 -> "اختيار الجامعة والكلية"
                        else -> "الملف الشخصي للطالب"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${currentStep + 1} من 3",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Indicator Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val color = if (index <= currentStep) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureOverviewStep(
    onNext: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "المكتبة الرقمية والمراجع",
            description = "وصول سريع لجميع الكتب والمراجع والمشاريع البحثية لكل المستويات الدراسية مع خيارات التصفية الذكية.",
            imageResId = R.drawable.onboarding_library_1786313456922
        ),
        OnboardingPage(
            title = "بنك الاختبارات التفاعلي",
            description = "تدرب على نماذج الاختبارات السابقة وحسن مستواك الأكاديمي مع نظام الإجابات والحلول الفورية.",
            imageResId = R.drawable.onboarding_exams_1786313467712
        ),
        OnboardingPage(
            title = "المحفظة الطلابية والخدمات",
            description = "أدر مدفوعاتك الجامعية، تابع هويتك الرقمية وحاسبة المعدل التراكمي في مكان واحد أمن ومريح.",
            imageResId = R.drawable.onboarding_wallet_1786313479402
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicators
            Row {
                repeat(pages.size) { index ->
                    val color = if (pagerState.currentPage == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onNext()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("onboarding_next_step_button")
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "متابعة الإعداد" else "التالي",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowBack, contentDescription = null) // RTL arrow
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionSelectionStep(
    universities: List<String>,
    faculties: List<String>,
    availableMajors: List<String>,
    selectedUniversity: String,
    selectedFaculty: String,
    selectedMajor: String,
    onUniversitySelected: (String) -> Unit,
    onFacultySelected: (String) -> Unit,
    onMajorSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "تحديد الجامعة والكلية",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "اختر مؤسستك التعليمية وكليتك لتخصيص المحتوى والمناهج الدراسية المناسبة لك.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. University Selection Dropdown / Selector
        Text(
            text = "المؤسسة التعليمية / الجامعة:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownSelectorCard(
            label = "اختر الجامعة",
            selectedItem = selectedUniversity,
            items = universities,
            icon = Icons.Default.AccountBalance,
            onItemSelected = onUniversitySelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Faculty Selection
        Text(
            text = "الكلية:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownSelectorCard(
            label = "اختر الكلية",
            selectedItem = selectedFaculty,
            items = faculties,
            icon = Icons.Default.School,
            onItemSelected = onFacultySelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Major Selection
        Text(
            text = "التخصص الأكاديمي:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownSelectorCard(
            label = "اختر التخصص",
            selectedItem = selectedMajor,
            items = availableMajors,
            icon = Icons.Default.MenuBook,
            onItemSelected = onMajorSelected
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_institution_next_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "متابعة بيانات الطالب",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelectorCard(
    label: String,
    selectedItem: String,
    items: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal,
                            color = if (item == selectedItem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    leadingIcon = if (item == selectedItem) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDataStep(
    studentName: String,
    studentId: String,
    selectedLevel: Int,
    onNameChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    onLevelChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "بيانات الملف الشخصي للطالب",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "أدخل اسمك ورقتك الجامعية ومستواك الدراسي لإصدار بطاقتك الطلابية الرقمية.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Student Name Field
        OutlinedTextField(
            value = studentName,
            onValueChange = onNameChange,
            label = { Text("اسم الطالب الكامل") },
            placeholder = { Text("مثال: أحمد محمد العباسي") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_student_name_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Student ID Field
        OutlinedTextField(
            value = studentId,
            onValueChange = onIdChange,
            label = { Text("الرقم الجامعي") },
            placeholder = { Text("مثال: 442109852") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_student_id_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Academic Level Selection
        Text(
            text = "المستوى الأكاديمي الحالي:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        val levelTitles = listOf(
            1 to "المستوى 1 (السنة الأولى - الإعدادية)",
            2 to "المستوى 2 (السنة الثانية)",
            3 to "المستوى 3 (السنة الثالثة)",
            4 to "المستوى 4 (السنة الرابعة)",
            5 to "المستوى 5 (السنة الخامسة - التخرج)"
        )

        levelTitles.forEach { (levelNum, title) ->
            val isSelected = levelNum == selectedLevel
            Card(
                onClick = { onLevelChange(levelNum) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onLevelChange(levelNum) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_submit_profile_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ والبدء برحلتك الأكاديمية",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
