package com.example.ui.screens.onboarding

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileSetupScreen(
    initialProfile: StudentProfile = StudentProfile(),
    universities: List<String>,
    faculties: List<String>,
    getMajorsForFaculty: (String) -> List<String>,
    onSaveProfileAndFinish: (String, String, String, String, String, Int) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SetupHeader(
                currentStep = currentStep,
                onBack = { if (currentStep > 0) currentStep-- }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    }
                },
                label = "SetupStepTransition"
            ) { step ->
                when (step) {
                    0 -> InstitutionSelectionStep(
                        universities = universities,
                        faculties = faculties,
                        availableMajors = availableMajors,
                        selectedUniversity = selectedUniversity,
                        selectedFaculty = selectedFaculty,
                        selectedMajor = selectedMajor,
                        onUniversitySelected = { selectedUniversity = it },
                        onFacultySelected = { selectedFaculty = it },
                        onMajorSelected = { selectedMajor = it },
                        onNext = { currentStep = 1 }
                    )
                    1 -> ProfileDataStep(
                        studentName = studentName,
                        studentId = studentId,
                        selectedLevel = selectedLevel,
                        onNameChange = { studentName = it },
                        onIdChange = { studentId = it },
                        onLevelChange = { selectedLevel = it },
                        onFinish = {
                            if (studentName.isNotBlank() && studentId.isNotBlank()) {
                                onSaveProfileAndFinish(
                                    studentName,
                                    studentId,
                                    selectedUniversity,
                                    selectedFaculty,
                                    selectedMajor,
                                    selectedLevel
                                )
                            } else {
                                Toast.makeText(context, "يرجى إكمال البيانات المطلوبة", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupHeader(
    currentStep: Int,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "السابق",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Text(
                    text = if (currentStep == 0) "بيانات الدراسة" else "الملف الشخصي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${currentStep + 1} / 2",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) { index ->
                    val color = if (index <= currentStep) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstitutionSelectionStep(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                "تحديد المسار الأكاديمي",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                "اختر مؤسستك التعليمية وكليتك لتخصيص تجربتك.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OnboardingDropdown(
            label = "الجامعة أو الكلية",
            selectedItem = selectedUniversity,
            items = universities,
            icon = Icons.Default.AccountBalance,
            onItemSelected = onUniversitySelected
        )

        OnboardingDropdown(
            label = "الكلية",
            selectedItem = selectedFaculty,
            items = faculties,
            icon = Icons.Default.School,
            onItemSelected = onFacultySelected
        )

        OnboardingDropdown(
            label = "التخصص",
            selectedItem = selectedMajor,
            items = availableMajors,
            icon = Icons.Default.Category,
            onItemSelected = onMajorSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("المتابعة للملف الشخصي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingDropdown(
    label: String,
    selectedItem: String,
    items: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDataStep(
    studentName: String,
    studentId: String,
    selectedLevel: Int,
    onNameChange: (String) -> Unit,
    onIdChange: (String) -> Unit,
    onLevelChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                "البيانات الشخصية",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                "أكمل بياناتك لإصدار هويتك الرقمية وتفعيل حسابك.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = studentName,
            onValueChange = onNameChange,
            label = { Text("اسم الطالب الرباعي") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = studentId,
            onValueChange = onIdChange,
            label = { Text("الرقم الأكاديمي") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("المستوى الدراسي", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { level ->
                    val isSelected = selectedLevel == level
                    Surface(
                        onClick = { onLevelChange(level) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(level.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("إكمال التسجيل", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.CheckCircle, contentDescription = null)
        }
    }
}
