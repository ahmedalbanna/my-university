package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class BottomTab(
    val route: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomTab("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home)
    object Library : BottomTab("library", "المكتبة", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Results : BottomTab("results", "النتائج", Icons.Filled.Grade, Icons.Outlined.Grade)
    object Exams : BottomTab("exams", "الاختبارات", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    object Favorites : BottomTab("favorites", "المفضلة", Icons.Filled.Bookmark, Icons.Outlined.Bookmark)
    object Wallet : BottomTab("wallet", "المحفظة", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    object More : BottomTab("more", "المزيد", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)

    companion object {
        val list: List<BottomTab>
            get() = listOf(Home, Library, Exams, Wallet, More)
    }
}

@Composable
fun JamaatyBottomBar(
    currentRoute: String,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )
        NavigationBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp // We use the divider for separation
        ) {
            BottomTab.list.forEach { tab ->
                val isSelected = (currentRoute == tab.route)
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.titleAr,
                            modifier = Modifier.testTag("nav_icon_${tab.route}")
                        )
                    },
                    label = {
                        Text(
                            text = tab.titleAr,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = 0.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.testTag("nav_item_${tab.route}")
                )
            }
        }
    }
}
