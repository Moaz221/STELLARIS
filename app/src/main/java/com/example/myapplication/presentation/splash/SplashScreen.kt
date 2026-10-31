package com.example.myapplication.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.myapplication.data.local.AppPreferences
import com.example.myapplication.presentation.components.StarfieldBackground
import com.example.myapplication.presentation.theme.DeepSpace
import com.example.myapplication.presentation.theme.Navy
import com.example.myapplication.presentation.theme.StellarCyan
import com.example.myapplication.presentation.theme.StellarTeal
import com.example.myapplication.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val DURATION = 4500
private const val T_FADE_START = 0.88f

private val HudLine = Color(0x66A5F3FC)
private val Scrim = Color(0xE6081020)

private fun phase(t: Float, start: Float, end: Float, stagger: Float): Float {
    val dur = end - start
    val ps = start + dur * 0.4f * stagger
    val pe = end - dur * 0.15f * (1f - stagger)
    return ((t - ps) / (pe - ps)).coerceIn(0f, 1f)
}

@Composable
fun SplashScreen(
    preferences: AppPreferences,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val particles by produceState<List<Particle>?>(initialValue = null) {
        value = buildParticles()
    }

    val t = remember { Animatable(0f) }
    var showTagline by remember { mutableStateOf(false) }
    var isFadingOut by remember { mutableStateOf(false) }
    var requestedExit by remember { mutableStateOf(false) }
    var navigationTarget by remember { mutableStateOf<(() -> Unit)?>(null) }

    val fadeAlpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else 1f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "fadeOut"
    )
    val tagAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "tag"
    )
    val skipAlpha by animateFloatAsState(
        targetValue = if (t.value > 0.12f && !isFadingOut) 1f else 0f,
        animationSpec = tween(500),
        label = "skip"
    )

    LaunchedEffect(particles) {
        if (particles == null) return@LaunchedEffect
        val shouldGoToLogin = preferences.isOnboardingDone.first()
        navigationTarget = if (shouldGoToLogin) onNavigateToLogin else onNavigateToOnboarding

        launch {
            delay((DURATION * 0.62f).toLong())
            showTagline = true
        }

        t.animateTo(1f, tween(DURATION, easing = LinearEasing))
        delay(280)
        requestedExit = true
    }

    LaunchedEffect(requestedExit, navigationTarget) {
        if (!requestedExit || navigationTarget == null || isFadingOut) return@LaunchedEffect
        isFadingOut = true
        delay(450)
        navigationTarget?.invoke()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fadeAlpha }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF05070F),
                        0.38f to DeepSpace,
                        0.72f to Navy,
                        1.00f to Color(0xFF071018)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures { requestedExit = true }
            }
    ) {
        StarfieldBackground(starCount = 70, scan = false)

        val particleList = particles
        if (particleList != null) {
            Canvas(Modifier.fillMaxSize()) {
                val s = minOf(size.width, size.height) / VIRTUAL
                val ox = (size.width - VIRTUAL * s) / 2f
                val oy = (size.height - VIRTUAL * s) / 2f
                val time = t.value
                val center = Offset(ox + 500f * s, oy + 470f * s)

                // شبكة X-ray خفيفة
                val gridAlpha = 0.045f + 0.02f * sin(time * 6f)
                val step = 46f * s
                var x = 0f
                while (x < size.width) {
                    drawLine(Color.White.copy(alpha = gridAlpha), Offset(x, 0f), Offset(x, size.height), 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(Color.White.copy(alpha = gridAlpha * 0.8f), Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }

                // توهج مركزي
                val g1 = phase(time, T_LUNG_START, T_LUNG_END, 0f)
                val g2 = phase(time, T_HOLD_END, T_MORPH_END, 0f)
                val pulse = 1f + 0.07f * sin(time * 2f * PI.toFloat() * 1.8f)
                val glowA = (g1 * (1f - g2) * 0.38f + g2 * 0.16f) * pulse
                if (glowA > 0.001f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glowA * 0.18f),
                                StellarCyan.copy(alpha = glowA * 0.55f),
                                StellarTeal.copy(alpha = glowA * 0.22f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 540f * s * pulse
                        ),
                        radius = 540f * s * pulse,
                        center = center
                    )
                }

                // حلقات HUD
                val ringAlpha = (0.10f + 0.08f * g1) * (1f - g2 * 0.4f)
                for (i in 0..2) {
                    val r = (210f + i * 95f) * s * pulse
                    drawCircle(
                        color = StellarCyan.copy(alpha = ringAlpha * (1f - i * 0.22f)),
                        radius = r,
                        center = center,
                        style = Stroke(width = (1.1f + i * 0.25f) * s)
                    )
                }

                // Crosshair طبي
                val tick = 26f * s
                val gap = 18f * s
                val crossA = ringAlpha * 1.4f
                drawLine(StellarCyan.copy(alpha = crossA), center + Offset(-tick - gap, 0f), center + Offset(-gap, 0f), 1.6f * s)
                drawLine(StellarCyan.copy(alpha = crossA), center + Offset(gap, 0f), center + Offset(tick + gap, 0f), 1.6f * s)
                drawLine(StellarCyan.copy(alpha = crossA), center + Offset(0f, -tick - gap), center + Offset(0f, -gap), 1.6f * s)
                drawLine(StellarCyan.copy(alpha = crossA), center + Offset(0f, gap), center + Offset(0f, tick + gap), 1.6f * s)

                // Shockwave
                val shockP = phase(time, T_LUNG_END - 0.02f, T_LUNG_END + 0.14f, 0f)
                if (shockP > 0f && shockP < 1f) {
                    drawCircle(
                        color = StellarCyan.copy(alpha = (1f - shockP) * 0.42f),
                        radius = lerp(70f, 480f, FastOutSlowInEasing.transform(shockP)) * s,
                        center = center,
                        style = Stroke(width = lerp(5.5f, 0.6f, shockP) * s)
                    )
                }

                val breath = sin(time * 2f * PI.toFloat() * 3.2f) * 0.06f
                val scanActive = time in T_LUNG_END..T_HOLD_END
                val scanP = ((time - T_LUNG_END) / (T_HOLD_END - T_LUNG_END)).coerceIn(0f, 1f)
                val scanYv = lerp(120f, 820f, scanP)
                val scanY = oy + scanYv * s

                particleList.forEach { p ->
                    val p1 = FastOutSlowInEasing.transform(phase(time, T_LUNG_START, T_LUNG_END, p.delay))
                    val p2 = FastOutSlowInEasing.transform(phase(time, T_HOLD_END, T_MORPH_END, p.delay))

                    var vx = lerp(lerp(p.start.x, p.lung.x, p1), p.text.x, p2)
                    var vy = lerp(lerp(p.start.y, p.lung.y, p1), p.text.y, p2)

                    val k = 1f + breath * p1 * (1f - p2)
                    vx = 500f + (vx - 500f) * k
                    vy = 470f + (vy - 500f) * k

                    val arc = sin(p2 * PI.toFloat()) * p.arcAmp * 0.65f
                    vx += p.arcCos * arc
                    vy += p.arcSin * arc

                    val formed = maxOf(p1 * (1f - p2), p2)
                    val amp = lerp(5.5f, 0.7f, formed)
                    val wobX = sin(time * 16f + p.twinkle * 6.283f) * amp
                    val wobY = cos(time * 13f + p.twinkle * 9.4f) * amp

                    val pos = Offset(ox + (vx + wobX) * s, oy + (vy + wobY) * s)

                    val twAmp = lerp(0.38f, 0.07f, formed)
                    val tw = 1f - twAmp * (0.5f + 0.5f * sin(time * 26f + p.twinkle * 12.56f))
                    var alpha = tw * (time / 0.07f).coerceIn(0f, 1f)

                    var boost = 0f
                    if (scanActive) {
                        val d = abs(vy - scanYv)
                        if (d < 86f) boost = 1f - d / 86f
                    }
                    alpha = min(1f, alpha + 0.62f * boost)
                    alpha *= 1f - phase(time, T_FADE_START, 1f, p.delay * 0.3f) * 0.75f

                    val baseColor = particleColor(lerp(p.colorMix, p.textMix, p2))
                    val color = if (boost > 0f) lerpColor(baseColor, Color.White, boost * 0.85f) else baseColor
                    val radius = p.size * (1f + boost) * s

                    if (p.size > 2.6f) {
                        drawCircle(color.copy(alpha = alpha * 0.16f), radius * 3.8f, pos)
                    }
                    drawCircle(color.copy(alpha = alpha), radius, pos)
                }

                if (scanActive) {
                    val lineAlpha = sin(scanP * PI.toFloat())
                    val trail = 180f * s
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                StellarTeal.copy(alpha = 0.10f * lineAlpha),
                                StellarCyan.copy(alpha = 0.32f * lineAlpha)
                            ),
                            startY = scanY - trail,
                            endY = scanY
                        ),
                        topLeft = Offset(0f, scanY - trail),
                        size = Size(size.width, trail)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(StellarCyan.copy(alpha = 0.16f * lineAlpha), Color.Transparent),
                            startY = scanY,
                            endY = scanY + 48f * s
                        ),
                        topLeft = Offset(0f, scanY),
                        size = Size(size.width, 48f * s)
                    )
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.White, StellarCyan, Color.Transparent)
                        ),
                        start = Offset(ox + 120f * s, scanY),
                        end = Offset(ox + 880f * s, scanY),
                        strokeWidth = 2.4f * s,
                        cap = StrokeCap.Round
                    )
                }

                // vignette
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        center = center,
                        radius = size.maxDimension * 0.72f
                    )
                )

                // زوايا HUD
                val arm = 28f * s
                val inset = 36f * s
                val corners = listOf(
                    Offset(inset, inset),
                    Offset(size.width - inset, inset),
                    Offset(inset, size.height - inset),
                    Offset(size.width - inset, size.height - inset)
                )
                corners.forEachIndexed { i, c ->
                    val sx = if (i % 2 == 0) 1f else -1f
                    val sy = if (i < 2) 1f else -1f
                    drawLine(HudLine, c, c + Offset(arm * sx, 0f), 2.2f * s, StrokeCap.Round)
                    drawLine(HudLine, c, c + Offset(0f, arm * sy), 2.2f * s, StrokeCap.Round)
                }
            }
        }

        // سكرِم تحت عشان الكتابة تبان
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Scrim)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 28.dp, end = 28.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INITIALIZING DIAGNOSTIC ENGINE",
                color = StellarCyan.copy(alpha = 0.55f + 0.25f * tagAlpha),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "AI-POWERED\nCHEST X-RAY ANALYSIS",
                color = Color(0xFFD7EEF5),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = tagAlpha
                    translationY = (1f - tagAlpha) * 18f
                }
            )

            Spacer(Modifier.height(10.dp))

            Canvas(Modifier.size(width = 72.dp, height = 6.dp)) {
                drawCircle(StellarCyan.copy(alpha = 0.35f + tagAlpha * 0.5f), 3.2.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
                drawLine(
                    StellarTeal.copy(alpha = 0.55f),
                    Offset(0f, size.height / 2f),
                    Offset(size.width * 0.38f, size.height / 2f),
                    1.6f
                )
                drawLine(
                    StellarTeal.copy(alpha = 0.55f),
                    Offset(size.width * 0.62f, size.height / 2f),
                    Offset(size.width, size.height / 2f),
                    1.6f
                )
            }

            Spacer(Modifier.height(18.dp))

            Canvas(
                Modifier
                    .width(168.dp)
                    .height(4.dp)
            ) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.10f),
                    size = size,
                    cornerRadius = CornerRadius(size.height)
                )
                val w = size.width * t.value
                if (w > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(StellarTeal, StellarCyan, Color.White)),
                        size = Size(w, size.height),
                        cornerRadius = CornerRadius(size.height)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = size.height,
                        center = Offset(w, size.height / 2f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = skipAlpha * 0.9f }
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, StellarCyan.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TAP TO SKIP",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}