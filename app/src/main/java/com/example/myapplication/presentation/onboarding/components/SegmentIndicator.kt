package com.example.myapplication.presentation.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SegmentIndicator(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { i ->
            val color by animateColorAsState(
                targetValue = when {
                    i == currentPage -> activeColor
                    i < currentPage -> activeColor.copy(alpha = 0.45f)
                    else -> Color.White.copy(alpha = 0.14f)
                },
                animationSpec = tween(350),
                label = "seg"
            )
            Box(
                Modifier
                    .width(if (i == currentPage) 30.dp else 16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}