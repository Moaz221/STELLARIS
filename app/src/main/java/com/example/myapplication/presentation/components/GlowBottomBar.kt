
package com.example.myapplication.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

/**
 * 0 = Home
 * 1 = My Health
 * 2 = AI Assistant
 * 3 = Care Journey
 * 4 = Profile
 */
@Composable
fun GlowBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = homePalette()
    val shape = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .height(92.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating translucent glass panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            p.cardTop.copy(alpha = 0.68f),
                            p.cardBottom.copy(alpha = 0.82f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            p.borderBright.copy(alpha = 0.80f),
                            p.borderDim.copy(alpha = 0.45f),
                            p.borderBright.copy(alpha = 0.65f)
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem(
                icon = Icons.Outlined.Home,
                label = stringResource(R.string.nav_home),
                selected = selectedIndex == 0,
                onClick = { onSelect(0) },
                modifier = Modifier.weight(1f)
            )

            BarItem(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.nav_my_health),
                selected = selectedIndex == 1,
                onClick = { onSelect(1) },
                modifier = Modifier.weight(1f)
            )

            // Center AI slot
            Box(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        role = Role.Button,
                        onClick = { onSelect(2) }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = stringResource(R.string.nav_ai_assistant),
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 10.sp,
                    fontWeight = if (selectedIndex == 2) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (selectedIndex == 2) {
                        p.accent
                    } else {
                        p.textSecondary
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }

            BarItem(
                icon = Icons.Outlined.Route,
                label = stringResource(R.string.nav_care_journey),
                selected = selectedIndex == 3,
                onClick = { onSelect(3) },
                modifier = Modifier.weight(1f)
            )

            BarItem(
                icon = Icons.Outlined.PersonOutline,
                label = stringResource(R.string.nav_profile),
                selected = selectedIndex == 4,
                onClick = { onSelect(4) },
                modifier = Modifier.weight(1f)
            )
        }

        AiOrb(
            selected = selectedIndex == 2,
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = { onSelect(2) }
        )
    }
}

@Composable
private fun BarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = homePalette()

    val tint by animateColorAsState(
        targetValue = if (selected) p.accent else p.textSecondary,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "navigationTint"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = tween(250),
        label = "navigationIconScale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 32.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selected) {
                        p.accent.copy(alpha = 0.12f)
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            color = tint,
            maxLines = 1,
            softWrap = false
        )

        Spacer(Modifier.height(3.dp))

        // Orbital light indicator
        Box(
            modifier = Modifier
                .width(if (selected) 12.dp else 3.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    if (selected) tint else Color.Transparent
                )
        )
    }
}

@Composable
private fun AiOrb(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = homePalette()

    val transition = rememberInfiniteTransition(
        label = "interstellarOrb"
    )

    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    val glow by transition.animateFloat(
        initialValue = 0.16f,
        targetValue = if (selected) 0.48f else 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbGlow"
    )

    val ringColor by animateColorAsState(
        targetValue = if (selected) {
            Color.White
        } else {
            p.accent
        },
        animationSpec = tween(350),
        label = "orbRingColor"
    )

    Box(
        modifier = modifier
            .size(68.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient blue glow
        Box(
            modifier = Modifier
                .size(62.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            p.accent.copy(alpha = glow),
                            p.accent.copy(alpha = glow * 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Thin orbital ring
        Box(
            modifier = Modifier
                .size(53.dp)
                .border(
                    width = 0.8.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            p.accent.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.90f),
                            p.accent.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Glass shell and luminous core
        Box(
            modifier = Modifier
                .size(43.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            p.cardTop.copy(alpha = 0.98f),
                            p.cardBottom.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.4.dp,
                    color = ringColor.copy(alpha = 0.95f),
                    shape = CircleShape
                )
                .padding(5.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            p.accent,
                            p.accentDeep
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = stringResource(
                    R.string.nav_ai_assistant
                ),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Tiny specular highlight
        Box(
            modifier = Modifier
                .offset(x = (-9).dp, y = (-9).dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
        )
    }
}
