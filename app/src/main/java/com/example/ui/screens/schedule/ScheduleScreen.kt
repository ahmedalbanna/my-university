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
            TopAppBar(
                title = {
                    Text("جدول المحاضرات", fontWeight = FontWeight.Bold)
                }
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
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        if (showAddSheet) {
            AddScheduleSheet(
                initialDay = selectedDay,
                days = days,
                onDismiss = { showAddSheet = false },
                onConfirm = { subject, day, start, end, room, doctor, color ->
                    val ok = viewModel.addSchedule(subject, day, start, end, room, doctor, color)
                    if (ok) showAddSheet = false
                }
            )
        }
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
                text = "استمتع بوقتك الدراسي في هذا اليوم",
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
    onDelete: () -> Unit = {}
) {
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
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
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
private fun AddScheduleSheet(
    initialDay: String,
    days: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Int) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("08:00") }
    var end by remember { mutableStateOf("10:00") }
    var selectedDay by remember { mutableStateOf(initialDay) }
    var showDayMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val colors = listOf(0xFF6200EE.toInt(), 0xFF03DAC6.toInt(), 0xFFFFAB00.toInt(), 0xFFD81B60.toInt(), 0xFF4CAF50.toInt())
    var selectedColor by remember { mutableStateOf(colors.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("إضافة محاضرة جديدة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = subject, onValueChange = { subject = it; error = null }, label = { Text("المادة") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("الدكتور") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = start, onValueChange = { start = it; error = null }, label = { Text("من HH:mm") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = end, onValueChange = { end = it; error = null }, label = { Text("إلى HH:mm") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            ExposedDropdownMenuBox(expanded = showDayMenu, onExpandedChange = { showDayMenu = !showDayMenu }) {
                OutlinedTextField(value = selectedDay, onValueChange = {}, readOnly = true, label = { Text("اليوم") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showDayMenu) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = showDayMenu, onDismissRequest = { showDayMenu = false }) {
                    days.forEach { d -> DropdownMenuItem(text = { Text(d) }, onClick = { selectedDay = d; showDayMenu = false }) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("اللون:", style = MaterialTheme.typography.labelMedium)
                colors.forEach { c ->
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(c)).border(if (c==selectedColor) 2.dp else 0.dp, if (c==selectedColor) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape).clickable { selectedColor = c })
                }
            }
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(onClick = {
                if (subject.isBlank() || doctor.isBlank() || room.isBlank()) { error = "يرجى ملء جميع الحقول"; return@Button }
                // conflict will be checked in ViewModel; show error if overlaps
                // we can't know here without ViewModel, so caller validates
                onConfirm(subject.trim(), selectedDay, start.trim(), end.trim(), room.trim(), doctor.trim(), selectedColor)
                // error handling via return value is inside ViewModel; if overlaps, caller will keep sheet open — for now just close and let ViewModel reject silently, show toast via error
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

