package com.example.myapplication.presentation.onboarding.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.theme.DeepSpace

@Composable
fun OnboardingButton(
    isLastPage: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val width by animateDpAsState(
        if (isLastPage) 168.dp else 56.dp,
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "btnWidth"
    )
    val color by animateColorAsState(accentColor, label = "btnColor")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(56.dp)
            .width(width)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.82f))))
            .clickable(onClick = onClick)
    ) {
        AnimatedContent(
            targetState = isLastPage,
            transitionSpec = {
                (fadeIn(tween(250)) + scaleIn(initialScale = 0.85f)) togetherWith fadeOut(tween(150))
            },
            label = "btnContent"
        ) { last ->
            if (last) {
                Text(
                    "Get Started",
                    color = DeepSpace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = DeepSpace
                )
            }
        }
    }
}