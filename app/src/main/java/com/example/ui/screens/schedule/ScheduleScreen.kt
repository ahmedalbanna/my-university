package com.example.ui.screens.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ScheduleEntity
import com.example.ui.viewmodel.JamaatyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: JamaatyViewModel,
    onBack: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    val days = listOf("السبت", "الأحد", "الأثنين", "الثلاثاء", "الأربعاء", "الخميس")
    var selectedDay by remember { mutableStateOf("الأحد") }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("جدول المحاضرات", fontWeight = FontWeight.Bold)
                        Text(
                            text = "تنظيم جدولك الأكاديمي الأسبوعي",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "إضافة")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة محاضرة") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Day Selector Section
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                ScrollableTabRow(
                    selectedTabIndex = days.indexOf(selectedDay),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[days.indexOf(selectedDay)]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    days.forEach { day ->
                        Tab(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            text = {
                                Text(
                                    text = day,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        )
                    }
                }
            }

            val daySchedules = schedules.filter { it.dayOfWeek == selectedDay }
                .sortedBy { it.startTime }

            if (daySchedules.isEmpty()) {
                EmptyScheduleState(selectedDay)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "محاضرات يوم $selectedDay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(daySchedules, key = { it.id }) { schedule ->
                        ScheduleCardItem(
                            schedule = schedule,
                            onDelete = { viewModel.deleteSchedule(schedule) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddScheduleBottomSheet(
            initialDay = selectedDay,
            onDismiss = { showAddSheet = false },
            onConfirm = { subject, day, start, end, room, doctor, color ->
                viewModel.addSchedule(subject, day, start, end, room, doctor, color)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun EmptyScheduleState(day: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "يوم $day خالٍ من المحاضرات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "استمتع بوقتك أو قم بإضافة محاضرات جديدة لجدولك",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScheduleCardItem(
    schedule: ScheduleEntity,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left color strip with time
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .background(Color(schedule.color).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = schedule.startTime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(schedule.color)
                    )
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp)
                            .background(Color(schedule.color).copy(alpha = 0.5f))
                    )
                    Text(
                        text = schedule.endTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = schedule.subject,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ScheduleInfoRow(
                        icon = Icons.Default.Person,
                        text = schedule.doctor,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ScheduleInfoRow(
                        icon = Icons.Default.MeetingRoom,
                        text = "القاعة: ${schedule.room}",
                        color = MaterialTheme.colorScheme.primary,
                        isBold = true
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف المحاضرة؟") },
            text = { Text("هل أنت متأكد من حذف '${schedule.subject}' من جدولك؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ScheduleInfoRow(
    icon: ImageVector,
    text: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleBottomSheet(
    initialDay: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var subject by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(initialDay) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedColor by remember { mutableStateOf(0xFF673AB7.toInt()) }

    val days = listOf("السبت", "الأحد", "الأثنين", "الثلاثاء", "الأربعاء", "الخميس")
    val colors = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF009688, 0xFF4CAF50,
        0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF607D8B
    ).map { it.toInt() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "إضافة محاضرة جديدة",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("اسم المادة / المساق") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) }
            )

            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text("اسم الدكتور / المحاضر") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("القاعة أو المعمل") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اليوم", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days) { d ->
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = { Text(d) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("وقت البدء") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("وقت الانتهاء") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Update, contentDescription = null) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("لون المحاضرة المميز", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colors) { c ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .clickable { selectedColor = c }
                                .border(
                                    width = if (selectedColor == c) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == c) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onConfirm(subject, day, startTime, endTime, room, doctor, selectedColor)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("حفظ المحاضرة في الجدول", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

