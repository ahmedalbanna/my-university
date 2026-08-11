package com.example.ui.screens.favorites

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.FavoriteEntity

private data class FavTypeInfo(
    val label: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val textColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<FavoriteEntity>,
    onToggleFavorite: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }
    val context = LocalContext.current

    val filteredFavorites = favorites.filter { fav ->
        val matchesType = when (selectedType) {
            "ALL" -> true
            "BOOK" -> fav.type == "BOOK"
            "PROJECT" -> fav.type == "PROJECT"
            "LIBRARY" -> fav.type == "LIBRARY"
            "QUESTION" -> fav.type == "QUESTION"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                fav.title.contains(searchQuery, ignoreCase = true) ||
                fav.subtitle.contains(searchQuery, ignoreCase = true) ||
                fav.faculty.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("favorites_screen_root")
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "المفضلة الأكاديمية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "المرجعيات والكتب والأبحاث المحفوظة",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (favorites.isNotEmpty()) {
                                Toast.makeText(context, "جاري تحضير ملف المفضلة للتصدير...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "تصدير",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("بحث في المفضلة...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        val filters = listOf(
                            "ALL" to "الكل",
                            "BOOK" to "📚 كتب",
                            "PROJECT" to "🔬 أبحاث",
                            "LIBRARY" to "🌐 مكتبات",
                            "QUESTION" to "💡 اختبارات"
                        )

                        items(filters) { (typeKey, label) ->
                            val isSelected = selectedType == typeKey
                            val count = if (typeKey == "ALL") favorites.size else favorites.count { it.type == typeKey }
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = typeKey },
                                label = { Text("$label ($count)", fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            if (filteredFavorites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (favorites.isEmpty()) "قائمة المفضلة فارغة" else "لا توجد نتائج تطابق البحث",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredFavorites) { fav ->
                    FavoriteCard(
                        fav = fav,
                        onRemove = { onToggleFavorite(fav.id, fav.type, fav.title, fav.subtitle, fav.faculty) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    fav: FavoriteEntity,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val favInfo = when (fav.type) {
        "BOOK" -> FavTypeInfo("كتاب دراسي", Icons.Default.MenuBook, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "PROJECT" -> FavTypeInfo("مشروع بحثي", Icons.Default.Science, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "LIBRARY" -> FavTypeInfo("مكتبة رقمية", Icons.Default.Public, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        "QUESTION" -> FavTypeInfo("سؤال / اختبار", Icons.Default.Quiz, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        else -> FavTypeInfo("مرجع", Icons.Default.Bookmark, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = favInfo.badgeColor
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(favInfo.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = favInfo.textColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(favInfo.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = favInfo.textColor)
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = fav.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${fav.subtitle} • ${fav.faculty}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { Toast.makeText(context, "جاري فتح: ${fav.title}", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح المرجع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
