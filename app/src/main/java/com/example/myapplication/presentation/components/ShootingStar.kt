package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ShootingStar(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var start by remember { mutableStateOf(Offset(0.2f, 0.1f)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2500, 6000))
            start = Offset(Random.nextFloat() * 0.6f, Random.nextFloat() * 0.35f)
            progress.snapTo(0f)
            progress.animateTo(1f, tween(1000, easing = FastOutLinearInEasing))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        if (p <= 0f || p >= 1f) return@Canvas

        val travel = size.width * 0.5f
        val head = Offset(
            x = start.x * size.width + travel * p,
            y = start.y * size.height + travel * p * 0.5f
        )
        val tailLength = 160f
        val tail = Offset(head.x - tailLength, head.y - tailLength * 0.5f)
        val fade = 1f - p

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = fade)),
                start = tail, end = head
            ),
            start = tail, end = head,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawCircle(Color.White.copy(alpha = fade), radius = 3.5f, center = head)
    }
}
