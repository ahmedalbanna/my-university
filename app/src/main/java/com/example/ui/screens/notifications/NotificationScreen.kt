package com.example.ui.screens.notifications

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
            NotificationTopBar(
                unreadCount = unreadCount,
                onBack = onBack,
                onSendDemo = { showSendNotifModal = true },
                onClearAll = { if (notifications.isNotEmpty()) showClearConfirmDialog = true }
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
            // Stats Banner
            NotificationStatsBanner(
                totalCount = notifications.size,
                unreadCount = unreadCount,
                onMarkAllRead = {
                    viewModel.markAllNotificationsAsRead()
                    Toast.makeText(context, "تم تحديد كافة التنبيهات كمقروءة ✓", Toast.LENGTH_SHORT).show()
                }
            )

            // Filter Section
            NotificationFilterSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it },
                totalCount = notifications.size,
                unreadCount = unreadCount,
                examCount = notifications.count { it.type == "EXAM" },
                feeCount = notifications.count { it.type == "FEE" },
                libraryCount = notifications.count { it.type == "LIBRARY" },
                announcementCount = notifications.count { it.type == "ANNOUNCEMENT" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            if (filteredNotifications.isEmpty()) {
                EmptyNotificationsState(
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        NotificationCardItem(
                            notification = notification,
                            onMarkAsRead = { viewModel.markNotificationAsRead(notification.id) },
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

    // Modal Components
    if (selectedNotifForDetail != null) {
        NotificationDetailSheet(
            notification = selectedNotifForDetail!!,
            onDismiss = { selectedNotifForDetail = null }
        )
    }

    if (showSendNotifModal) {
        SendNotificationSheet(
            onDismiss = { showSendNotifModal = false },
            onSend = { title, message, type ->
                viewModel.sendNotification(title, message, type)
                showSendNotifModal = false
                Toast.makeText(context, "تم إرسال التنبيه بنجاح ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showClearConfirmDialog) {
        ClearNotificationsDialog(
            onDismiss = { showClearConfirmDialog = false },
            onConfirm = {
                viewModel.deleteAllNotifications()
                showClearConfirmDialog = false
                Toast.makeText(context, "تمت إزالة كافة التنبيهات", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onBack: () -> Unit,
    onSendDemo: () -> Unit,
    onClearAll: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "مركز التنبيهات والأخبار",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unreadCount > 0) {
                    Text(
                        text = "لديك $unreadCount تنبيهات جديدة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
        },
        actions = {
            IconButton(onClick = onSendDemo) {
                Icon(Icons.Default.AddAlert, contentDescription = "إرسال تجريبي", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onClearAll) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "مسح الكل", tint = MaterialTheme.colorScheme.error)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun NotificationStatsBanner(
    totalCount: Int,
    unreadCount: Int,
    onMarkAllRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "التنبيهات الجامعية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "إجمالي $totalCount • $unreadCount غير مقروء",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (unreadCount > 0) {
                Button(
                    onClick = onMarkAllRead,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("قراءة الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    totalCount: Int,
    unreadCount: Int,
    examCount: Int,
    feeCount: Int,
    libraryCount: Int,
    announcementCount: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث في التنبيهات والأخبار...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            val categories = listOf(
                "ALL" to "الكل ($totalCount)",
                "UNREAD" to "غير مقروء 🔴 ($unreadCount)",
                "EXAM" to "📝 اختبارات ($examCount)",
                "FEE" to "💰 رسوم ($feeCount)",
                "LIBRARY" to "📚 مكتبة ($libraryCount)",
                "ANNOUNCEMENT" to "📢 إعلانات ($announcementCount)"
            )

            items(categories) { (key, label) ->
                val isSelected = selectedCategory == key
                Surface(
                    onClick = { onCategoryChange(key) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    tonalElevation = if (isSelected) 4.dp else 0.dp
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCardItem(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val style = getNotificationStyle(notification.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.5.dp),
        border = if (!notification.isRead) 
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) 
        else 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(style.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = style.bg
                    ) {
                        Text(
                            text = style.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = style.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = formatTimeAgo(notification.timestamp),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = notification.title,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenDetails() }
                    ) {
                        Text(
                            text = "عرض التفاصيل",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp).padding(start = 2.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!notification.isRead) {
                            IconButton(onClick = onMarkAsRead, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "مقروء", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationDetailSheet(
    notification: NotificationEntity,
    onDismiss: () -> Unit
) {
    val style = getNotificationStyle(notification.type)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(style.bg)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = style.bg) {
                    Text(
                        text = style.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formatFullTimestamp(notification.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val actionLabel = when (notification.type) {
                "EXAM" -> "عرض جدول الاختبارات"
                "FEE" -> "سداد الرسوم المستحقة"
                "LIBRARY" -> "فتح المكتبة الرقمية"
                else -> "إغلاق التنبيه"
            }

            Button(
                onClick = {
                    onDismiss()
                    Toast.makeText(context, "جاري الانتقال للخدمة...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = actionLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendNotificationSheet(
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXAM") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "إرسال تنبيه تجريبي 🔔",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "تصنيف التنبيه:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf(
                        "EXAM" to "📝 اختبار",
                        "FEE" to "💰 رسوم",
                        "LIBRARY" to "📚 كتاب",
                        "ANNOUNCEMENT" to "📢 إعلان"
                    )
                    items(types) { (key, label) ->
                        val isSelected = selectedType == key
                        Surface(
                            onClick = { selectedType = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان التنبيه") },
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نص التنبيه") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = { onSend(title, message, selectedType) },
                enabled = title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("إرسال التنبيه الآن", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ClearNotificationsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("مسح كافة التنبيهات؟", fontWeight = FontWeight.Bold) },
        text = { Text("هل أنت متأكد من رغبتك في حذف جميع التنبيهات؟ لا يمكن التراجع عن هذا الإجراء.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("حذف الكل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun EmptyNotificationsState(
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
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (hasNotificationsAtAll) "لا توجد نتائج بحث" else "لا توجد تنبيهات حالياً",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasNotificationsAtAll) "جرب البحث بكلمات أخرى أو تغيير التصفية." else "سيتم إرسال إشعارات الجامعة والنتائج والرسوم إليك هنا.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!hasNotificationsAtAll) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onGenerateDemo,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة تنبيهات تجريبية")
                }
            }
        }
    }
}

private data class NotificationStyle(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bg: Color,
    val color: Color
)

@Composable
private fun getNotificationStyle(type: String): NotificationStyle {
    return when (type) {
        "EXAM" -> NotificationStyle("اختبار أكاديمي", Icons.Default.EventNote, Color(0xFFFFEBEE), Color(0xFFD32F2F))
        "FEE" -> NotificationStyle("رسوم ومحفظة", Icons.Default.Payments, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "LIBRARY" -> NotificationStyle("مرجع ومكتبة", Icons.Default.LibraryBooks, Color(0xFFE3F2FD), Color(0xFF1976D2))
        "ANNOUNCEMENT" -> NotificationStyle("إعلان جامعي", Icons.Default.Campaign, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        else -> NotificationStyle("تنبيه عام", Icons.Default.Notifications, Color(0xFFFFF3E0), Color(0xFFE65100))
    }
}

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
