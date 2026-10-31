package com.example.myapplication.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.myapplication.presentation.components.StarfieldBackground
import com.example.myapplication.presentation.onboarding.components.OnboardingButton
import com.example.myapplication.presentation.onboarding.components.SegmentIndicator
import com.example.myapplication.presentation.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == onboardingPages.lastIndex
    val accent by animateColorAsState(onboardingPages[currentPage].accentColor, tween(500), label = "accent")
    val density = LocalDensity.current.density

    BackHandler(enabled = currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
    }

    Box(Modifier.fillMaxSize().background(DeepSpace)) {

        // ===== الصفحات (3D خفيف) =====
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            CinematicPage(
                page = onboardingPages[page],
                pageOffset = pageOffset,
                cameraDistance = 16f * density
            )
        }

        // ===== الشريط العلوي =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%02d / %02d".format(currentPage + 1, onboardingPages.size),
                color = TextPrimary.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
            AnimatedVisibility(visible = !isLastPage, enter = fadeIn(), exit = fadeOut()) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.10f))
                ) {
                    Text(
                        "Skip",
                        color = TextPrimary.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ===== التحكم السفلي =====
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentIndicator(
                pageCount = onboardingPages.size,
                currentPage = currentPage,
                activeColor = accent
            )
            OnboardingButton(
                isLastPage = isLastPage,
                accentColor = accent,
                onClick = {
                    if (isLastPage) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                }
            )
        }
    }
}

@Composable
private fun CinematicPage(
    page: OnboardingPage,
    pageOffset: Float,
    cameraDistance: Float
) {
    val absOff = abs(pageOffset).coerceIn(0f, 1f)

    // نبض هادي للهالة
    val pulse by rememberInfiniteTransition(label = "glowPulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                // دوران 3D أخف وأنعم، من غير rotationZ
                this.cameraDistance = cameraDistance
                rotationY = pageOffset * 22f
                val sc = 1f - 0.08f * absOff
                scaleX = sc
                scaleY = sc
                alpha = 1f - 0.35f * absOff
            }
    ) {
        // خلفية + نجوم (من غير glow عام على الصفحة كلها)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DeepSpace, Navy))))
        StarfieldBackground(starCount = 36, scan = false)

        // 🎬 الأنيميشن: كبير ومتوسّط في النص العلوي
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.lottieRes))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
                .size(310.dp)
                .graphicsLayer {
                    // parallax: نفس اتجاه النص بس أبطأ
                    translationX = pageOffset * 40f
                    val sc = 0.92f + 0.08f * (1f - absOff)
                    scaleX = sc
                    scaleY = sc
                }
        ) {
            // الهالة الوحيدة: أكبر من الأنيميشن، ناعمة، وبتنبض
            Box(
                Modifier
                    .requiredSize(400.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .background(
                        Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to page.accentColor.copy(alpha = 0.30f),
                                0.55f to page.accentColor.copy(alpha = 0.10f),
                                1.0f to Color.Transparent
                            )
                        )
                    )
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 🌑 Gradient Overlay تحت عشان الكلام يبان
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.50f to Color.Transparent,
                        0.72f to DeepSpace.copy(alpha = 0.85f),
                        1.0f to DeepSpace
                    )
                )
            )
        )

        // النصوص: متوسّطة على نفس محور الأنيميشن
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 130.dp)
                .graphicsLayer {
                    translationX = pageOffset * 70f
                    alpha = (1f - absOff * 1.2f).coerceIn(0f, 1f)
                }
        ) {
            Text(
                text = page.kicker,
                color = page.accentColor,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = page.title,
                color = TextPrimary,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = page.description,
                color = TextSecondary,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}