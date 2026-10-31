package com.example.myapplication.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.StellarTeal
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private class Dust(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val cycles: Float,   // رقم صحيح عشان اللوب يبقى سلس
    val drift: Float,    // رقم صحيح برضو
    val maxAlpha: Float
)

/**
 * خلفية "أشعة": شبكة رفيعة جدًا بتتلاشى ناحية الحواف + ذرات غبار هادية
 * + (اختياري) شريط مسح ضوئي ناعم.
 * الاسم والـ starCount زي ما هم عشان الاستدعاءات القديمة تفضل شغالة.
 */
@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 120,
    scan: Boolean = true
) {
    val dust = remember(starCount) {
        List(starCount) {
            Dust(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = 0.6f + Random.nextFloat() * 0.9f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                cycles = (2 + Random.nextInt(5)).toFloat(),
                drift = (1 + Random.nextInt(3)).toFloat(),
                maxAlpha = 0.14f + Random.nextFloat() * 0.30f
            )
        }
    }

    val progress by rememberInfiniteTransition(label = "bg").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )

    Box(modifier.fillMaxSize()) {

        // الشبكة (ثابتة، مابتتعملهاش redraw)
        Canvas(Modifier.fillMaxSize()) {
            val gap = 44.dp.toPx()
            val line = StellarTeal.copy(alpha = 0.055f)
            val vBrush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, line, Color.Transparent),
                startY = 0f, endY = size.height
            )
            val hBrush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, line, Color.Transparent),
                startX = 0f, endX = size.width
            )
            var x = gap / 2f
            while (x < size.width) {
                drawLine(vBrush, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += gap
            }
            var y = gap / 2f
            while (y < size.height) {
                drawLine(hBrush, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += gap
            }
        }

        // الغبار + المسح (المتحرك)
        Canvas(Modifier.fillMaxSize()) {
            val p = progress
            val twoPi = 2f * PI.toFloat()

            dust.forEach { d ->
                val yy = (((d.y - p * d.drift) % 1f) + 1f) % 1f
                val a = d.maxAlpha * (0.5f + 0.5f * sin(twoPi * p * d.cycles + d.phase))
                drawCircle(
                    color = Color.White.copy(alpha = a.coerceIn(0f, 1f)),
                    radius = d.radius,
                    center = Offset(d.x * size.width, yy * size.height)
                )
            }

            if (scan) {
                val bandH = 140f
                val sy = ((p * 5f) % 1f) * (size.height + bandH)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, StellarTeal.copy(alpha = 0.06f)),
                        startY = sy - bandH, endY = sy
                    ),
                    topLeft = Offset(0f, sy - bandH),
                    size = Size(size.width, bandH)
                )
            }
        }
    }
}