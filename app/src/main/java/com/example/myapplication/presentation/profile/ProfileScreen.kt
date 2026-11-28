package com.example.myapplication.presentation.profile

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import kotlinx.coroutines.delay

/** الرحلات التلاتة. لو عندك enum جاهز للرحلة في المشروع، اربطه بده عن طريق onJourneyChange. */
enum class ProfileJourney(
    @StringRes val chipLabel: Int,
    @StringRes val title: Int
) {
    EARLY_DETECTION(R.string.profile_journey_chip_early, R.string.profile_journey_early_title),
    EVALUATION(R.string.profile_journey_chip_evaluation, R.string.profile_journey_evaluation_title),
    PATIENT_CARE(R.string.profile_journey_chip_patient, R.string.profile_journey_patient_title)
}

private fun ProfileJourney.icon(): ImageVector = when (this) {
    ProfileJourney.EARLY_DETECTION -> Icons.Outlined.Shield
    ProfileJourney.EVALUATION -> Icons.Outlined.MedicalServices
    ProfileJourney.PATIENT_CARE -> Icons.Outlined.FavoriteBorder
}

/* ───────────────────────── الشاشة ─────────────────────────
   أول 3 باراميترز زي ما هم، فالاستدعاء الحالي في الـ NavHost هيفضل شغال. */
@Composable
fun ProfileScreen(
    onToggleTheme: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onNavigateToDesignPreview: () -> Unit = {},
    userName: String = "",
    userEmail: String = "",
    initialJourney: ProfileJourney = ProfileJourney.EARLY_DETECTION,
    onJourneyChange: (ProfileJourney) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onPersonalInfo: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onSecurity: () -> Unit = {},
    onHelp: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    // بيتحسب من الـ Theme الفعلي، فبيمشي مع زرار التبديل جوه التطبيق مش مع إعداد الموبايل
    val isDark = colors.background.luminance() < 0.5f

    var journey by rememberSaveable { mutableStateOf(initialJourney) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    // حركة الدخول تشتغل أول مرة بس، مش كل ما ترجع للتاب
    var entrancePlayed by rememberSaveable { mutableStateOf(false) }
    val animate = remember { !entrancePlayed }
    LaunchedEffect(Unit) { entrancePlayed = true }

    val p = com.example.myapplication.presentation.home.homePalette()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    0f to p.bgGlow.copy(alpha = 0.45f),
                    0.35f to p.bgGlowSecondary.copy(alpha = 0.20f),
                    0.75f to Color.Transparent
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1) العنوان
            Entrance(index = 0, animate = animate) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() }
                    )
                    SquareIconButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.profile_open_settings),
                        onClick = onOpenSettings
                    )
                }
            }

            // 2) كارت المستخدم
            Entrance(index = 1, animate = animate) {
                ProfileCard(cornerRadius = 20.dp, highlighted = true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileAvatar(initials = initialsOf(userName))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        SquareIconButton(
                            icon = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.profile_edit),
                            onClick = onEditProfile
                        )
                    }
                }
            }

            // 3) المظهر واللغة
            Entrance(index = 2, animate = animate) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(stringResource(R.string.profile_section_appearance))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppearanceCard(
                            title = stringResource(
                                if (isDark) R.string.profile_theme_to_light
                                else R.string.profile_theme_to_dark
                            ),
                            subtitle = stringResource(R.string.profile_theme_hint),
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            AnimatedContent(
                                targetState = isDark,
                                transitionSpec = {
                                    (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                                },
                                label = "themeIcon"
                            ) { dark ->
                                IconTile(if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode)
                            }
                        }
                        AppearanceCard(
                            title = stringResource(R.string.profile_language_target),
                            subtitle = stringResource(R.string.profile_language_hint),
                            onClick = onToggleLanguage,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            IconTile(Icons.Outlined.Language)
                        }
                    }
                }
            }

            // 4) الرحلة
            Entrance(index = 3, animate = animate) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(stringResource(R.string.profile_section_journey))
                    ProfileCard {
                        AnimatedContent(
                            targetState = journey,
                            transitionSpec = {
                                (fadeIn() + slideInVertically { it / 3 }) togetherWith
                                        (fadeOut() + slideOutVertically { -it / 3 })
                            },
                            label = "journeyCard"
                        ) { current ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconTile(current.icon())
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(current.title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.profile_journey_hint),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileJourney.values().forEach { option ->
                            JourneyChip(
                                label = stringResource(option.chipLabel),
                                selected = option == journey,
                                onClick = {
                                    if (option != journey) {
                                        journey = option
                                        onJourneyChange(option)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 5) الإعدادات
            Entrance(index = 4, animate = animate) {
                val entries = listOf(
                    SettingsEntry(Icons.Outlined.Person, R.string.profile_item_personal_title, R.string.profile_item_personal_subtitle, onPersonalInfo),
                    SettingsEntry(Icons.Outlined.Notifications, R.string.profile_item_notifications_title, R.string.profile_item_notifications_subtitle, onNotifications),
                    SettingsEntry(Icons.Outlined.Shield, R.string.profile_item_privacy_title, R.string.profile_item_privacy_subtitle, onPrivacy),
                    SettingsEntry(Icons.Outlined.Lock, R.string.profile_item_security_title, R.string.profile_item_security_subtitle, onSecurity),
                    SettingsEntry(Icons.Outlined.Palette, R.string.profile_item_design_title, R.string.profile_item_design_subtitle, onNavigateToDesignPreview),
                    SettingsEntry(Icons.Outlined.SupportAgent, R.string.profile_item_help_title, R.string.profile_item_help_subtitle, onHelp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(stringResource(R.string.profile_section_settings))
                    ProfileCard {
                        Column {
                            entries.forEachIndexed { index, entry ->
                                SettingsRow(
                                    icon = entry.icon,
                                    title = stringResource(entry.title),
                                    subtitle = stringResource(entry.subtitle),
                                    onClick = entry.onClick
                                )
                                if (index != entries.lastIndex) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.primary.copy(alpha = 0.12f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6) تسجيل الخروج + الإصدار
            Entrance(index = 5, animate = animate) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colors.primary.copy(alpha = 0.14f),
                            contentColor = colors.onSurface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.profile_sign_out),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val context = LocalContext.current
                    val versionName = remember {
                        runCatching {
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName
                        }.getOrNull().orEmpty()
                    }
                    if (versionName.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.profile_app_version, versionName),
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.profile_sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) { Text(stringResource(R.string.profile_sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }
}

private data class SettingsEntry(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val onClick: () -> Unit
)

private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "•" }

/* ───────────────────────── المكوّنات ───────────────────────── */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.semantics { heading() }
    )
}

/** كارت بحدود خفيفة وتدرّج لوني، وبينضغط بحركة لو ليه onClick. */
@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(cornerRadius)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardPress"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(colors.surface)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primary.copy(alpha = if (highlighted) 0.20f else 0.08f),
                        Color.Transparent
                    )
                )
            )
            .border(1.dp, colors.primary.copy(alpha = if (highlighted) 0.30f else 0.18f), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) { content() }
}

@Composable
private fun IconTile(icon: ImageVector) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(colors.primary.copy(alpha = 0.14f))
            .border(1.dp, colors.primary.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SquareIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.primary.copy(alpha = 0.18f), shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileAvatar(initials: String, size: Dp = 64.dp) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "avatarRing")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "ringAngle"
    )

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        // الحلقة اللي بتلف
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { rotationZ = angle }
                .background(
                    Brush.sweepGradient(
                        listOf(
                            colors.primary,
                            colors.secondary,
                            colors.primary.copy(alpha = 0.10f),
                            colors.primary
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(3.dp)
                .background(colors.surface, CircleShape)
                .background(colors.primary.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun AppearanceCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ProfileCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            icon()
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JourneyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50)
    val container by animateColorAsState(
        if (selected) colors.primary.copy(alpha = 0.18f) else colors.surface,
        label = "chipContainer"
    )
    val borderColor by animateColorAsState(
        if (selected) colors.primary else colors.primary.copy(alpha = 0.18f),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        if (selected) colors.primary else colors.onSurfaceVariant,
        label = "chipText"
    )

    // مساحة اللمس 48dp حتى لو شكل الـ chip أصغر
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(container)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // علامة الصح عشان الاختيار ما يعتمدش على اللون بس
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(16.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 68.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { scaleX = if (isRtl) -1f else 1f } // السهم يتقلب في العربي
        )
    }
}

/** حركة دخول: كل قسم يطلع من تحت ورا اللي قبله. */
@Composable
private fun Entrance(index: Int, animate: Boolean, content: @Composable () -> Unit) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animate) {
            delay(index * 70L)
            progress.animateTo(
                1f,
                spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }
    Box(
        Modifier.graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * 24.dp.toPx()
        }
    ) { content() }
}

/* ───────────────────────── Preview تفاعلي ───────────────────────── */

private val PreviewLightColors = lightColorScheme(
    primary = Color(0xFF176B67), onPrimary = Color.White,
    secondary = Color(0xFF2F8F89),
    background = Color(0xFFF7F9FB), onBackground = Color(0xFF1D2939),
    surface = Color.White, onSurface = Color(0xFF1D2939),
    surfaceVariant = Color(0xFFEAF6F3), onSurfaceVariant = Color(0xFF667085),
    outlineVariant = Color(0xFFE4E7EC), error = Color(0xFFC83E4D)
)

private val PreviewDarkColors = darkColorScheme(
    primary = Color(0xFF56C5B3), onPrimary = Color(0xFF081011),
    secondary = Color(0xFF73D2C2),
    background = Color(0xFF081011), onBackground = Color(0xFFF3F7F6),
    surface = Color(0xFF111A1B), onSurface = Color(0xFFF3F7F6),
    surfaceVariant = Color(0xFF172526), onSurfaceVariant = Color(0xFF9BADAA),
    outlineVariant = Color(0xFF263334)
)

@Preview(name = "Dark · EN", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, heightDp = 1100)
@Preview(name = "Light · AR", locale = "ar", showBackground = true, heightDp = 1100)
@Composable
private fun ProfileScreenPreview() {
    val systemDark = isSystemInDarkTheme()
    var dark by remember { mutableStateOf(systemDark) }
    MaterialTheme(colorScheme = if (dark) PreviewDarkColors else PreviewLightColors) {
        ProfileScreen(onToggleTheme = { dark = !dark })
    }
}