package com.example.myapplication.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/** Compose-drawn assistant robot: head, blinking eyes, glowing antenna. No external assets. */
@Composable
fun RobotFace(modifier: Modifier = Modifier, thinking: Boolean = false, animated: Boolean = true) {
    val t = rememberInfiniteTransition(label = "robot")
    val blinkAnim by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "blink"
    )
    val pulseAnim by t.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    val lookAnim by t.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "look"
    )
    val blink = if (!animated) 1f else when {
        blinkAnim < 0.90f -> 1f
        blinkAnim < 0.95f -> 1f - (blinkAnim - 0.90f) / 0.05f * 0.9f
        else -> 0.1f + (blinkAnim - 0.95f) / 0.05f * 0.9f
    }
    val pulse = if (animated) pulseAnim else 0.8f
    val look = if (animated && thinking) lookAnim * 0.03f else 0f
    val neon = Color(0xFF39D0FF)
    Canvas(modifier) {
        val w = size.width; val h = size.height
        fun rr(x0: Float, y0: Float, x1: Float, y1: Float, r: Float, c: Color) = drawRoundRect(
            c, Offset(x0 * w, y0 * h), Size((x1 - x0) * w, (y1 - y0) * h), CornerRadius(r * w, r * w)
        )
        // antenna
        drawLine(Color(0xFFB8D4EA), Offset(0.5f * w, 0.12f * h), Offset(0.5f * w, 0.26f * h), strokeWidth = 0.035f * w, cap = StrokeCap.Round)
        drawCircle(neon.copy(alpha = 0.35f * pulse), 0.10f * w, Offset(0.5f * w, 0.10f * h))
        drawCircle(neon, 0.055f * w, Offset(0.5f * w, 0.10f * h))
        // ears
        rr(0.07f, 0.50f, 0.17f, 0.70f, 0.04f, neon)
        rr(0.83f, 0.50f, 0.93f, 0.70f, 0.04f, neon)
        // head + visor
        rr(0.14f, 0.24f, 0.86f, 0.84f, 0.20f, Color(0xFFF2F8FD))
        rr(0.23f, 0.35f, 0.77f, 0.68f, 0.12f, Color(0xFF0B1F3A))
        // eyes
        val eh = 0.15f * blink
        for (cx in listOf(0.385f, 0.615f)) {
            val x = cx + look
            rr(x - 0.055f, 0.515f - eh / 2, x + 0.055f, 0.515f + eh / 2, 0.05f, neon)
        }
        // mouth
        drawLine(Color(0xFF7FA3C4), Offset(0.42f * w, 0.76f * h), Offset(0.58f * w, 0.76f * h), strokeWidth = 0.03f * w, cap = StrokeCap.Round)
    }
}
