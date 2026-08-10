package com.example.ui.screens.wallet

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
    var activeTab by remember { mutableStateOf(0) } // 0: تسديد الرسوم, 1: النفقة الخاصة والاتصالات, 2: سجل الفواتير
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedServiceForPay by remember { mutableStateOf<Pair<String, Double>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("wallet_screen_root")
    ) {
        // Balance Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💳 محفظتي والخدمات المالية",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "رصيد المحفظة الإلكترونية",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${profile.balance} ر.ي",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    onClick = {
                        selectedServiceForPay = Pair("شحن رصيد المحفظة الطلابية", 500.0)
                        showPaymentModal = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
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
            }
        }

        // SubTabs
        SecondaryTabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("تسديد الرسوم") })
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("نفقة خاصة واتصالات") })
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("سجل الفواتير") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            0 -> {
                // TUITION FEES PAYMENT
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "الاستحقاقات والرسوم الدراسية القائمة:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        FeeItemCard(
                            title = "رسوم الفصل الدراسي الثاني 2026",
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
                            dueDate = "2026-08-30",
                            amount = 15000.0,
                            isPaid = false,
                            onPayNow = {
                                selectedServiceForPay = Pair("رسوم المعامل والإنترنت والتطبيقات", 15000.0)
                                showPaymentModal = true
                            }
                        )
                    }

                    item {
                        FeeItemCard(
                            title = "رسوم الفصل الدراسي الأول 2026",
                            dueDate = "2026-01-10",
                            amount = 250000.0,
                            isPaid = true,
                            onPayNow = {}
                        )
                    }
                }
            }
            1 -> {
                // PRIVATE TUITION & TELECOM SERVICES
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "خدمات الاتصالات والاشتراكات الطلابية (نفقة خاصة):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        TelecomServiceCard(
                            title = "باقة الاتصالات الطلابية الشاملة (50GB + مكالمات)",
                            price = 5500.0,
                            features = listOf("50 جيجابايت إنترنت سريع للمناهج", "تصفح مجاني لموقع الجامعة والمكتبة", "تجديد شهري تلقائي"),
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
                            features = listOf("جلسات تقوية مع معيدين معتمدين", "مراجعة مشروعات التخرج", "مباشر عبر المنصة"),
                            onSubscribe = {
                                selectedServiceForPay = Pair("خدمة المتابعة والدروس الخاصة", 12000.0)
                                showPaymentModal = true
                            }
                        )
                    }
                }
            }
            2 -> {
                // TRANSACTION & RECEIPT HISTORY
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "سجل الفواتير والإيصالات المسددة (${payments.size}):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(payments) { payment ->
                        PaymentReceiptCard(payment = payment)
                    }
                }
            }
        }
    }

    // Payment Processing Modal
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
            }
        )
    }
}

@Composable
fun FeeItemCard(
    title: String,
    dueDate: String,
    amount: Double,
    isPaid: Boolean,
    onPayNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = "تاريخ الاستحقاق: $dueDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$amount ر.ي", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            if (isPaid) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "تم التسديد ✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = onPayNow,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تسديد الآن", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TelecomServiceCard(
    title: String,
    price: Double,
    features: List<String>,
    onSubscribe: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Text(text = "$price ر.ي/شهرياً", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            features.forEach { feat ->
                Text(text = "• $feat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("اشتراك / دفع الآن")
            }
        }
    }
}

@Composable
fun PaymentReceiptCard(payment: PaymentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = payment.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "رقم العملية: ${payment.referenceNo} | ${payment.date}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "طريقة الدفع: ${payment.paymentMethod}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Text(
                text = "${payment.amount} ${payment.currency}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
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
    val sheetState = rememberModalBottomSheetState()
    var selectedMethod by remember { mutableStateOf("محفظة الكريمي / كاش") }

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
                Text(text = "تأكيد الدفع الإلكتروني", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "الخدمة: $title", fontWeight = FontWeight.SemiBold)
            Text(text = "المبلغ المطلوب: $amount ر.ي", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "اختر وسيلة الدفع:", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            listOf("محفظة الكريمي / كاش", "تحويل بنكي / صرافة", "المحفظة الرقمية (الرصيد: $walletBalance ر.ي)").forEach { method ->
                Surface(
                    onClick = { selectedMethod = method },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMethod == method) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(text = method, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onConfirmPayment(selectedMethod) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تأكيد ودفع $amount ر.ي الآن", fontWeight = FontWeight.Bold)
            }
        }
    }
}
