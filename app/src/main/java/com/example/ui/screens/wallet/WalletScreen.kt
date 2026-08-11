package com.example.ui.screens.wallet

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PaymentEntity
import com.example.data.model.StudentProfile
import com.example.ui.components.DigitalStudentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    profile: StudentProfile,
    payments: List<PaymentEntity>,
    onProcessPayment: (String, String, Double, String) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: تسديد الرسوم, 1: النفقة الخاصة والاتصالات, 2: سجل الفواتير
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedServiceForPay by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var showStudentCard by remember { mutableStateOf(false) }
    var selectedReceiptForView by remember { mutableStateOf<PaymentEntity?>(null) }
    var feeFilter by remember { mutableStateOf("ALL") } // ALL, DUE, PAID
    var receiptSearchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("wallet_screen_root")
    ) {
        // Balance Banner & Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    // Header Title & Balance Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "محفظتي والخدمات المالية الطلابية",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "الرصيد الإلكتروني المتاح:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%,.0f", profile.balance),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ر.ي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Right Top Action Buttons
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = {
                                    selectedServiceForPay = Pair("شحن رصيد المحفظة الطلابية", 10000.0)
                                    showPaymentModal = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "شحن المحفظة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                onClick = { showStudentCard = !showStudentCard },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                contentColor = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (showStudentCard) Icons.Default.CreditCard else Icons.Default.Badge,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showStudentCard) "إخفاء البطاقة" else "بطاقتي الرقمية",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Financial Action Chips Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                Toast.makeText(context, "جاري تصدير كشف الحساب المالي كـ PDF...", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("كشف حساب", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            onClick = {
                                Toast.makeText(context, "ميزة تحويل الرصيد لزميل متوفرة قريباً عبر التطبيق", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحويل رصيد", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            onClick = {
                                Toast.makeText(context, "رمز QR الخاص برقمك الجامعي جاهز للمسح", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رمز QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Expandable Digital Student Card Widget
        AnimatedVisibility(
            visible = showStudentCard,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                DigitalStudentCard(profile = profile)
            }
        }

        // Custom Mode Switcher Navigation SubTabs Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
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
                    0 to "💳 تسديد الرسوم",
                    1 to "📱 النفقة والاتصالات",
                    2 to "🧾 الفواتير (${payments.size})"
                )

                tabs.forEach { (index, title) ->
                    val isSelected = activeTab == index
                    Surface(
                        onClick = { activeTab = index },
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

        AnimatedContent(
            targetState = activeTab,
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
        ) { tab ->
            when (tab) {
                0 -> {
                // TUITION FEES PAYMENT FLOW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        // Summary Card for Academic Fees Status
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "حالة الرسوم الدراسية القائمة 🎓",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "إجمالي الرسوم المتبقية: 265,000 ر.ي",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "الفصل الثاني 2026",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Progress Indicator for Paid Fees vs Due
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "نسبة المسدد: 49%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "المسدد: 258,000 / 523,000 ر.ي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { 0.49f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        // Filter Chips for Fees Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تصفية الرسوم:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            listOf(
                                "ALL" to "الكل",
                                "DUE" to "المستحقة ⚠️",
                                "PAID" to "المسددة ✓"
                            ).forEach { (filterKey, label) ->
                                val isSelected = feeFilter == filterKey
                                Surface(
                                    onClick = { feeFilter = filterKey },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (feeFilter == "ALL" || feeFilter == "DUE") {
                        item {
                            FeeItemCard(
                                title = "رسوم الفصل الدراسي الثاني 2026",
                                category = "رسوم دراسية معتمدة",
                                dueDate = "2026-09-01",
                                amount = 250000.0,
                                isPaid = false,
                                onPayNow = {
                                    selectedServiceForPay = Pair("رسوم الفصل الدراسي الثاني 2026", 250000.0)
                                    showPaymentModal = true
                                }
                            )
                        }

                        item {
                            FeeItemCard(
                                title = "رسوم المعامل والإنترنت والتطبيقات",
                                category = "رسوم خدمات وأنشطة",
                                dueDate = "2026-08-30",
                                amount = 15000.0,
                                isPaid = false,
                                onPayNow = {
                                    selectedServiceForPay = Pair("رسوم المعامل والإنترنت والتطبيقات", 15000.0)
                                    showPaymentModal = true
                                }
                            )
                        }
                    }

                    if (feeFilter == "ALL" || feeFilter == "PAID") {
                        item {
                            FeeItemCard(
                                title = "رسوم الفصل الدراسي الأول 2026",
                                category = "رسوم مسددة بالكامل",
                                dueDate = "2026-01-10",
                                amount = 250000.0,
                                isPaid = true,
                                onPayNow = {}
                            )
                        }

                        item {
                            FeeItemCard(
                                title = "رسوم تسجيل المواد والبطاقة الذكية",
                                category = "رسوم إدارية",
                                dueDate = "2025-09-15",
                                amount = 8000.0,
                                isPaid = true,
                                onPayNow = {}
                            )
                        }
                    }
                }
            }

            1 -> {
                // PRIVATE TUITION & TELECOM SERVICES FLOW
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
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "الخدمات الطلابية المميزة والنفقة الخاصة 📶",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "باقات إنترنت مخفضة، تقوية أكاديمية، وكوبونات خدمات جامعية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        TelecomServiceCard(
                            title = "باقة الاتصالات الطلابية الشاملة (50GB + مكالمات)",
                            price = 5500.0,
                            icon = Icons.Default.PhoneAndroid,
                            features = listOf(
                                "50 جيجابايت إنترنت سريع للمناهج والأبحاث",
                                "تصفح مجاني مفتوح لموقع الجامعة والمكتبة الرقمية",
                                "تجديد شهري تلقائي وتغطية ممتازة داخل الحرم الجامعي"
                            ),
                            onSubscribe = {
                                selectedServiceForPay = Pair("باقة الاتصالات الطلابية 50GB", 5500.0)
                                showPaymentModal = true
                            }
                        )
                    }

                    item {
                        TelecomServiceCard(
                            title = "خدمة المتابعة والدروس الخاصة (نفقة خاصة)",
                            price = 12000.0,
                            icon = Icons.Default.School,
                            features = listOf(
                                "جلسات تقوية فردية مع معيدين وأساتذة معتمدين",
                                "مراجعة وإشراف مباشر على مشروعات التخرج والأبحاث",
                                "بث مباشر وتطبيقات عملي عبر المنصة"
                            ),
                            onSubscribe = {
                                selectedServiceForPay = Pair("خدمة المتابعة والدروس الخاصة", 12000.0)
                                showPaymentModal = true
                            }
                        )
                    }

                    item {
                        TelecomServiceCard(
                            title = "كوبونات التغذية والطباعة الجامعية",
                            price = 3000.0,
                            icon = Icons.Default.Print,
                            features = listOf(
                                "رصيد طباعة وتصوير 150 صفحة بمطبعة الكلية",
                                "خصم 20% بوجبات مطعم السكن الجامعي",
                                "إصدار فوري وكود QR مجاني"
                            ),
                            onSubscribe = {
                                selectedServiceForPay = Pair("كوبونات التغذية والطباعة الجامعية", 3000.0)
                                showPaymentModal = true
                            }
                        )
                    }
                }
            }

            2 -> {
                // TRANSACTION & RECEIPT HISTORY
                val filteredPayments = payments.filter { p ->
                    receiptSearchQuery.isBlank() ||
                            p.title.contains(receiptSearchQuery, ignoreCase = true) ||
                            p.referenceNo.contains(receiptSearchQuery, ignoreCase = true) ||
                            p.paymentMethod.contains(receiptSearchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = receiptSearchQuery,
                            onValueChange = { receiptSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_search_input"),
                            placeholder = { Text("بحث في الفواتير أو برقم العملية TXN...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (receiptSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { receiptSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سجل العمليات والإيصالات (${filteredPayments.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "انقر المعاملة للعين والطباعة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (filteredPayments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "لا توجد فواتير أو عمليات سداد مسجلة حالياً",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "تأكد من اختيار التصفية الصحيحة أو قم بإجراء عملية سداد جديدة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(filteredPayments) { payment ->
                        PaymentReceiptCard(
                            payment = payment,
                            onClick = { selectedReceiptForView = payment }
                        )
                    }
                }
            }
        }
    }
}

    // Payment Processing Modal Sheet
    if (showPaymentModal && selectedServiceForPay != null) {
        val service = selectedServiceForPay!!
        PaymentModalBottomSheet(
            title = service.first,
            amount = service.second,
            walletBalance = profile.balance,
            onDismiss = { showPaymentModal = false },
            onConfirmPayment = { method ->
                onProcessPayment(service.first, "TUITION", service.second, method)
                showPaymentModal = false
                Toast.makeText(context, "تم تنفيذ عملية السداد بنجاح ✓", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Detailed Receipt Dialog Sheet
    if (selectedReceiptForView != null) {
        val receipt = selectedReceiptForView!!
        ReceiptDetailModalBottomSheet(
            payment = receipt,
            profile = profile,
            onDismiss = { selectedReceiptForView = null }
        )
    }
}

@Composable
fun FeeItemCard(
    title: String,
    category: String,
    dueDate: String,
    amount: Double,
    isPaid: Boolean,
    onPayNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPaid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تاريخ الاستحقاق: $dueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${String.format("%,.0f", amount)} ر.ي",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isPaid) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تم السداد ✓",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = onPayNow,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تسديد الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TelecomServiceCard(
    title: String,
    price: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    features: List<String>,
    onSubscribe: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            features.forEach { feat ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%,.0f", price),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ر.ي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Button(
                    onClick = onSubscribe,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("اشتراك / دفع الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentReceiptCard(
    payment: PaymentEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = payment.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TXN: ${payment.referenceNo} • ${payment.date}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "طريقة الدفع: ${payment.paymentMethod}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%,.0f", payment.amount)} ${payment.currency}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "معتمد ✓",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModalBottomSheet(
    title: String,
    amount: Double,
    walletBalance: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by remember { mutableStateOf("المحفظة الطلابية الرقمية") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تأكيد بوابات الدفع الإلكتروني",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "الخدمة المطلوبة:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "المبلغ الإجمالي المطلوب:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format("%,.0f", amount)} ر.ي",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "اختر وسيلة الدفع المعتمدة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))

            val methods = listOf(
                "المحفظة الطلابية الرقمية" to "الرصيد المتاح: ${String.format("%,.0f", walletBalance)} ر.ي",
                "محفظة الكريمي كاش" to "سداد فوري عبر تطبيق الكريمي",
                "خدمة جوالي / ون كاش / فلوسك" to "المحافظ الإلكترونية المحلية",
                "تحويل بنكي صرافة" to "إشعار تحويل مصرفي مباشر"
            )

            methods.forEach { (methodName, sub) ->
                val isSelected = selectedMethod == methodName
                Surface(
                    onClick = { selectedMethod = methodName },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = methodName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onConfirmPayment(selectedMethod) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_payment_button"),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد واعتماد الدفع الإلكتروني الآن", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailModalBottomSheet(
    payment: PaymentEntity,
    profile: StudentProfile,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "إيصال سداد إلكتروني رسمـي 📄", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "إغلاق") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Printable Voucher Card Visual
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "جامعة ذمار - الشؤون المالية والجامعية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = payment.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${String.format("%,.0f", payment.amount)} ${payment.currency}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReceiptRow("اسم الطالب:", profile.name)
                        ReceiptRow("الرقم الجامعي:", profile.studentId)
                        ReceiptRow("الكلية والفرع:", profile.faculty)
                        ReceiptRow("رقم المعاملة (TXN):", payment.referenceNo)
                        ReceiptRow("تاريخ السداد:", payment.date)
                        ReceiptRow("طريقة الدفع:", payment.paymentMethod)
                        ReceiptRow("حالة العملية:", "معتمدة ومسددة بالكامل ✓")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code visual placeholder box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "رمز التحقق الإلكتروني للعملية المعتمدة\nQR Reference: ${payment.referenceNo}",
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "جاري مشاركة الإيصال الإلكتروني الرسمي...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الإيصال")
                }

                Surface(
                    onClick = {
                        Toast.makeText(context, "جاري طباعة الإيصال بتنسيق PDF...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
