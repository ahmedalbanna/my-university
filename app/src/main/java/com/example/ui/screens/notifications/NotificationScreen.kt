package com.example.ui.screens.notifications

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NotificationEntity
import com.example.ui.viewmodel.JamaatyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: JamaatyViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNotifForDetail by remember { mutableStateOf<NotificationEntity?>(null) }
    var showSendNotifModal by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredNotifications = notifications.filter { notif ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            "UNREAD" -> !notif.isRead
            "EXAM" -> notif.type == "EXAM"
            "FEE" -> notif.type == "FEE"
            "LIBRARY" -> notif.type == "LIBRARY"
            "ANNOUNCEMENT" -> notif.type == "ANNOUNCEMENT"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                notif.title.contains(searchQuery, ignoreCase = true) ||
                notif.message.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("مركز التنبيهات والأخبار", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "$unreadCount غير مقروء",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showSendNotifModal = true }) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "إرسال تنبيه تجريبي",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "مسح جميع التنبيهات",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("notification_screen_root")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "متابعة التنبيهات الأكاديمية والمالية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "إجمالي التنبيهات: ${notifications.size} • غير مقروءة: $unreadCount",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (unreadCount > 0) {
                        Surface(
                            onClick = {
                                viewModel.markAllNotificationsAsRead()
                                Toast.makeText(context, "تم تحديد كافة التنبيهات كمقروءة ✓", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحديد الكل كمقروء", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Search and Category Chips
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("بحث في الإشعارات بالتصنيف أو الكلمات...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        "ALL" to "الكل (${notifications.size})",
                        "UNREAD" to "غير مقروء 🔴 ($unreadCount)",
                        "EXAM" to "📝 اختبارات (${notifications.count { it.type == "EXAM" }})",
                        "FEE" to "💰 رسوم ومحفظة (${notifications.count { it.type == "FEE" }})",
                        "LIBRARY" to "📚 كتب ومكتبة (${notifications.count { it.type == "LIBRARY" }})",
                        "ANNOUNCEMENT" to "📢 إعلانات عامة (${notifications.count { it.type == "ANNOUNCEMENT" }})"
                    )

                    items(categories) { (catKey, label) ->
                        val isSelected = selectedCategory == catKey
                        Surface(
                            onClick = { selectedCategory = catKey },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Notifications List
            if (filteredNotifications.isEmpty()) {
                EmptyNotifications(
                    hasNotificationsAtAll = notifications.isNotEmpty(),
                    onGenerateDemo = {
                        viewModel.sendNotification("اختبار نصفي قريب", "تذكير: اختبار مادة أمن الشبكات يوم الثلاثاء القادم بالقاعة 201.", "EXAM")
                        viewModel.sendNotification("تنبيه سداد القسط الثاني", "تم إصدار فاتورة القسط الثاني لرسوم الساعات الأكاديمية.", "FEE")
                        viewModel.sendNotification("إصدار مرجع أبحاث جديد", "تمت إضافة المرجع الشامل في الذكاء الاصطناعي للمكتبة الرقمية.", "LIBRARY")
                        viewModel.sendNotification("إعلان جدول المحاضرات", "تم تعديل مواعيد المحاضرات للمستوى الثالث ابتداءً من الأسبوع القادم.", "ANNOUNCEMENT")
                        Toast.makeText(context, "تمت إضافة تنبيهات تعليمية تجريبية ✓", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 4.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationCardItem(
                            notification = notification,
                            onMarkAsRead = {
                                viewModel.markNotificationAsRead(notification.id)
                            },
                            onDelete = {
                                viewModel.deleteNotification(notification)
                                Toast.makeText(context, "تم حذف التنبيه", Toast.LENGTH_SHORT).show()
                            },
                            onOpenDetails = {
                                viewModel.markNotificationAsRead(notification.id)
                                selectedNotifForDetail = notification
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedNotifForDetail != null) {
        val notif = selectedNotifForDetail!!
        NotificationDetailModalBottomSheet(
            notification = notif,
            onDismiss = { selectedNotifForDetail = null }
        )
    }

    // Send Demo Notification Modal Sheet
    if (showSendNotifModal) {
        SendNotificationModalBottomSheet(
            onDismiss = { showSendNotifModal = false },
            onSend = { title, message, type ->
                viewModel.sendNotification(title, message, type)
                showSendNotifModal = false
                Toast.makeText(context, "تم إرسال التنبيه بنجاح ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Confirm Clear All Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("مسح كافة التنبيهات؟", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت تأكد من رغبتك في حذف جميع التنبيهات والأخبار من السجل؟ لن يمكنك استرجاعها بعد ذلك.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllNotifications()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "تمت إزالة كافة التنبيهات", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، مسح الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun NotificationCardItem(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val (typeLabel, icon, iconBg, iconColor) = when (notification.type) {
        "EXAM" -> QuadrupleNotif("اختبار أكاديمي", Icons.Default.EventNote, Color(0xFFFFEBEE), Color(0xFFD32F2F))
        "FEE" -> QuadrupleNotif("رسوم ومحفظة", Icons.Default.Payments, Color(0xE8F5E9), Color(0xFF2E7D32))
        "LIBRARY" -> QuadrupleNotif("مرجع ومكتبة", Icons.Default.LibraryBooks, Color(0xFFE3F2FD), Color(0xFF1976D2))
        "ANNOUNCEMENT" -> QuadrupleNotif("إعلان جامعي", Icons.Default.Campaign, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        else -> QuadrupleNotif("تنبيه عام", Icons.Default.Notifications, Color(0xFFFFF3E0), Color(0xFFE65100))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.5.dp),
        border = if (!notification.isRead) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = iconBg
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Box(modifier = Modifier.size(8.dp))
                            }
                        }
                    }

                    Text(
                        text = formatTimeAgo(notification.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انقر لمشاهدة التفاصيل الكاملة ↗",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Row {
                        if (!notification.isRead) {
                            IconButton(
                                onClick = onMarkAsRead,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "تحديد كمقروء",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailModalBottomSheet(
    notification: NotificationEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

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
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "تفاصيل التنبيه الجامعي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "نوع التنبيه: ${notification.type}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = formatFullTimestamp(notification.timestamp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = notification.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val buttonText = when (notification.type) {
                "EXAM" -> "الانتقال إلى جدول الاختبارات القادمة"
                "FEE" -> "الانتقال إلى بوابات الدفع والمحفظة"
                "LIBRARY" -> "الانتقال إلى المكتبة والمراجع"
                else -> "فهمت واستوعبت التنبيه"
            }

            Button(
                onClick = {
                    onDismiss()
                    Toast.makeText(context, "جاري الانتقال إلى القسم المختص بالخدمة...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationModalBottomSheet(
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXAM") }

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
                Text(text = "إرسال تنبيه أو إعلان تجريبي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "تصنيف التنبيه:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(6.dp))

            val types = listOf(
                "EXAM" to "📝 اختبار",
                "FEE" to "💰 رسوم",
                "LIBRARY" to "📚 كتاب",
                "ANNOUNCEMENT" to "📢 إعلان"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedType == typeKey
                    Surface(
                        onClick = { selectedType = typeKey },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان التنبيه") },
                placeholder = { Text("مثال: تذكير بموعد تسليم البحث") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نص الرسالة أو التنبيه") },
                placeholder = { Text("أدخل التفاصيل والتعليمات الخاصة بالطالب...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        return@Button
                    }
                    onSend(title, message, selectedType)
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إرسال التنبيه فوراً 🔔", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyNotifications(
    hasNotificationsAtAll: Boolean,
    onGenerateDemo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasNotificationsAtAll) "لا توجد تنبيهات تطابق تصفية البحث الحالية" else "سجل التنبيهات والأخبار فارغ حالياً",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "سيتم عرض جميع الإشعارات والإنذارات الأكاديمية والمالية الصادرة من الجامعة هنا فور صدورها.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            if (!hasNotificationsAtAll) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGenerateDemo,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة تنبيهات تعليمية تجريبية", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

private data class QuadrupleNotif(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bg: Color,
    val color: Color
)

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 2 -> "الآن"
        minutes < 60 -> "منذ $minutes دقيقة"
        hours < 24 -> "منذ $hours ساعة"
        days < 7 -> "منذ $days أيام"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("ar"))
            sdf.format(Date(timestamp))
        }
    }
}

private fun formatFullTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE dd MMMM yyyy - hh:mm a", Locale("ar"))
    return sdf.format(Date(timestamp))
}
