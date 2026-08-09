package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepLinkDialog(
    currentRoute: String,
    currentLink: String,
    onExecuteDeepLink: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf(currentLink) }

    val presetLinks = listOf(
        "jamaaty://library?level=4&fac=حاسبات&q=خوارزميات",
        "jamaaty://exams?diff=صعب&tag=#خوارزميات",
        "jamaaty://chat",
        "jamaaty://wallet",
        "jamaaty://translate",
        "https://jamaaty.app/library?level=2&semester=1"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("deep_link_dialog_root")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔗 الربط العميق والتنقل السريع (Deep Linking)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Screen Deep Link Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "رابط التوجيه المباشر للشاشة الحالية:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentLink,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Jamaaty DeepLink", currentLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الرابط المباشر إلى الحافظة بنجاح! 📋", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "نسخ الرابط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deep Link Tester Section
            Text(
                text = "🧪 أداة اختبار الروابط العميقة (Deep Link Tester):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset Chips
            Text(
                text = "اختر رابط تجريبي سريع:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetLinks) { link ->
                    FilterChip(
                        selected = (inputUrl == link),
                        onClick = { inputUrl = link },
                        label = { Text(text = link, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Custom Deep Link URL
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                placeholder = { Text("أدخل رابط deep link مثل jamaaty://library?level=4...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deep_link_input_field"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputUrl.isNotBlank()) {
                                try {
                                    val uri = Uri.parse(inputUrl)
                                    onExecuteDeepLink(uri)
                                    Toast.makeText(context, "تم تنفيذ الرابط العميق بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "الرابط غير صالح", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل الرابط", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (inputUrl.isNotBlank()) {
                        try {
                            val uri = Uri.parse(inputUrl)
                            onExecuteDeepLink(uri)
                            Toast.makeText(context, "تم التنقل عبر الرابط العميق! 🚀", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "الرابط غير صحيح", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_deep_link_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تشغيل وتطبيق التنقل العميق", fontWeight = FontWeight.Bold)
            }
        }
    }
}
