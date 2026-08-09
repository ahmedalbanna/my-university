package com.example.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageResId: Int
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "المكتبة الرقمية",
            description = "وصول سريع لجميع الكتب والمراجع والمشاريع البحثية لكل المستويات الدراسية.",
            imageResId = R.drawable.onboarding_library_1786313456922
        ),
        OnboardingPage(
            title = "بنك الاختبارات",
            description = "تدرب على نماذج الاختبارات السابقة وحسن مستواك الأكاديمي مع الحلول التفاعلية.",
            imageResId = R.drawable.onboarding_exams_1786313467712
        ),
        OnboardingPage(
            title = "المحفظة الطلابية",
            description = "أدر مدفوعاتك الجامعية وتابع سجل معاملاتك المالية بكل سهولة وأمان.",
            imageResId = R.drawable.onboarding_wallet_1786313479402
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("onboarding_screen")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pager Indicator and Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicators
            Row {
                repeat(pages.size) { index ->
                    val color = if (pagerState.currentPage == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Navigation Button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "ابدأ الآن" else "التالي",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(32.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
