package com.example.myapplication.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import kotlinx.coroutines.delay
import kotlin.math.sin

enum class JourneyType {
    EARLY_DETECTION,
    UNDER_EVALUATION,
    DIAGNOSED
}

private data class HomeArticle(
    val id: String,
    val title: String,
    val summary: String,
    val body: String,
    val icon: ImageVector
)

// ---------------------------------------------------------------------------
// Modern Palette — polished glowing style
// ---------------------------------------------------------------------------
internal data class HomePalette(
    val background: Color,
    val bgGlow: Color,
    val bgGlowSecondary: Color,
    val cardTop: Color,
    val cardBottom: Color,
    val cardElevated: Color,
    val borderBright: Color,
    val borderDim: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val alert: Color,
    val success: Color,
    val warning: Color
)

private val DarkPalette = HomePalette(
    background = Color(0xFF030A12),
    bgGlow = Color(0xFF0A2A44),
    bgGlowSecondary = Color(0xFF0D1F3A),
    cardTop = Color(0xFF0C2438),
    cardBottom = Color(0xFF071522),
    cardElevated = Color(0xFF102A42),
    borderBright = Color(0xFF2B7BB5),
    borderDim = Color(0xFF3A6E94),
    accent = Color(0xFF5CC8FF),
    accentDeep = Color(0xFF1B6FA8),
    accentSoft = Color(0xFF3A9FD9),
    textPrimary = Color(0xFFEAF4FB),
    textSecondary = Color(0xFFB8CEDF),
    textTertiary = Color(0xFF8FA9BF),
    alert = Color(0xFFFF5252),
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24)
)

private val LightPalette = HomePalette(
    background = Color(0xFFF3F8FC),
    bgGlow = Color(0xFFCFE8F8),
    bgGlowSecondary = Color(0xFFDCE9F5),
    cardTop = Color(0xFFFFFFFF),
    cardBottom = Color(0xFFEAF4FB),
    cardElevated = Color(0xFFFFFFFF),
    borderBright = Color(0xFF7FB8DE),
    borderDim = Color(0xFF9DBFD8),
    accent = Color(0xFF0B7CC0),
    accentDeep = Color(0xFF0B5E94),
    accentSoft = Color(0xFF2B9BD9),
    textPrimary = Color(0xFF0B1F33),
    textSecondary = Color(0xFF3A556B),
    textTertiary = Color(0xFF647D92),
    alert = Color(0xFFE53935),
    success = Color(0xFF16A34A),
    warning = Color(0xFFEA580C)
)

@Composable
internal fun homePalette(): HomePalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) DarkPalette else LightPalette
}

// ---------------------------------------------------------------------------
// Main HomeScreen — connected to Firebase via ViewModel
// ---------------------------------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToVisionLab: () -> Unit = {},
    onNavigateToSymptoms: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToQuestions: () -> Unit = {},
    onNavigateToScreeningGuide: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    enableAnimations: Boolean = true,
    lungStatusPercent: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = homePalette()

    var showArticleLibrary by rememberSaveable { mutableStateOf(false) }
    var activeArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var entranceShown by rememberSaveable { mutableStateOf(false) }
    var heroVisible by remember { mutableStateOf(entranceShown) }

    LaunchedEffect(Unit) {
        heroVisible = true
        entranceShown = true
    }

    // Smooth entrance animation
    val heroProgress by animateFloatAsState(
        targetValue = if (heroVisible || !enableAnimations) 1f else 0f,
        animationSpec = if (enableAnimations) {
            tween(durationMillis = 450, easing = FastOutSlowInEasing)
        } else snap(),
        label = "heroProgress"
    )

    // Infinite subtle pulse for the background glow
    val infiniteTransition = rememberInfiniteTransition(label = "bgPulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    var showNotifs by remember { mutableStateOf(false) }
    if (showNotifs) NotificationsDialog(onClose = { showNotifs = false })
    val articles = starterArticles()
    val activeArticle = articles.firstOrNull { it.id == activeArticleId }
    val openArticle: (HomeArticle) -> Unit = { activeArticleId = it.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .drawBehind {
                // Main top glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.bgGlow.copy(alpha = 0.55f * glowPulse),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 1.2f
                    )
                )
                // Secondary side glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.bgGlowSecondary.copy(alpha = 0.3f * glowPulse),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.4f),
                        radius = size.width * 0.7f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item(key = "header") {
                HomeHeader(
                    userName = uiState.userName,
                    userEmail = uiState.userEmail,
                    userInitials = uiState.userInitials,
                    photoUrl = uiState.photoUrl,
                    hasUnreadNotifications = uiState.hasUnreadNotifications,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onOpenNotifications = { showNotifs = true }
                )
            }

            item(key = "journey") {
                JourneyControl(
                    selectedJourney = uiState.journeyType,
                    onJourneySelected = { viewModel.selectJourney(it) }
                )
            }

            item(key = "next_step") {
                NextStepCard(
                    journey = uiState.journeyType,
                    enableAnimations = enableAnimations,
                    lungStatusPercent = lungStatusPercent,
                    modifier = Modifier.graphicsLayer {
                        alpha = heroProgress
                        translationY = (1f - heroProgress) * 24.dp.toPx()
                    },
                    onOpenScreening = onNavigateToScreeningGuide,
                    onOpenQuestions = onNavigateToQuestions,
                    onOpenSymptoms = onNavigateToSymptoms
                )
            }

            item(key = "vision_lab") {
                VisionLabCard(onOpenLab = onNavigateToVisionLab)
            }

            item(key = "quick_actions") {
                QuickActionsSection(
                    onTrackSymptom = onNavigateToSymptoms,
                    onMedicalRecords = onNavigateToRecords,
                    onDoctorQuestions = onNavigateToQuestions,
                    onScreeningGuide = onNavigateToScreeningGuide
                )
            }

            item(key = "daily_tip") {
                DailyTipCard(onReadMore = { activeArticleId = "organize_records" })
            }

            item(key = "articles_header") {
                SectionHeading(
                    title = stringResource(R.string.home_articles_title),
                    actionText = stringResource(R.string.home_view_all),
                    onAction = { showArticleLibrary = true }
                )
            }

            items(
                items = articles.take(2),
                key = { "article_${it.id}" }
            ) { article ->
                ArticleCard(article = article, onClick = { openArticle(article) })
            }
        }
    }

    if (activeArticle != null) {
        ArticleReaderDialog(
            article = activeArticle,
            onDismiss = { activeArticleId = null }
        )
    } else if (showArticleLibrary) {
        ArticleLibraryDialog(
            articles = articles,
            onOpenArticle = openArticle,
            onDismiss = { showArticleLibrary = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Enhanced GlowCard with modern styling
// ---------------------------------------------------------------------------
@Composable
private fun GlowCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    glow: Boolean = false,
    glowIntensity: Float = 1f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val p = homePalette()

    var base = modifier
    if (false && glow) {
        base = base.shadow(
            elevation = (16 * glowIntensity).dp,
            shape = shape,
            ambientColor = p.accent.copy(alpha = 0.4f * glowIntensity),
            spotColor = p.accent.copy(alpha = 0.4f * glowIntensity)
        )
    }
    base = base
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(p.cardTop, p.cardBottom),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    p.borderBright.copy(alpha = 0.9f),
                    p.borderDim,
                    p.borderBright.copy(alpha = 0.5f)
                )
            ),
            shape = shape
        )

    Box(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        content = content
    )
}

// ---------------------------------------------------------------------------
// Modern Header with Firebase user info
// ---------------------------------------------------------------------------
@Composable
private fun HomeHeader(
    userName: String,
    userEmail: String,
    userInitials: String,
    photoUrl: String?,
    hasUnreadNotifications: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val p = homePalette()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Fallback name: username → email prefix → placeholder
    val displayName = userName.trim().ifBlank {
        userEmail.substringBefore("@").ifBlank {
            stringResource(R.string.home_welcome_title)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.home_welcome),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = p.textSecondary
            )

            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = p.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HeaderCircleButton(
            icon = Icons.Default.Language,
            description = stringResource(R.string.home_change_language),
            onClick = onToggleLanguage
        )

        HeaderCircleButton(
            icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            description = stringResource(R.string.home_change_theme),
            onClick = onToggleTheme
        )

        HeaderCircleButton(
            icon = Icons.Outlined.Notifications,
            description = stringResource(R.string.home_notifications),
            onClick = onOpenNotifications,
            showBadge = hasUnreadNotifications
        )

        // Avatar with photo or initials
        UserAvatar(
            photoUrl = photoUrl,
            initials = userInitials
        )
    }
}

@Composable
private fun UserAvatar(
    photoUrl: String?,
    initials: String
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(p.accent.copy(alpha = 0.18f))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(p.accent, p.accentDeep)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else if (initials.isBlank()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = p.accent,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = initials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = p.accent
            )
        }
    }
}

@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    val p = homePalette()

    Box(modifier = Modifier.size(42.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(p.accent.copy(alpha = 0.14f))
                .border(1.dp, p.accent.copy(alpha = 0.55f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = p.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(p.alert)
                    .border(2.dp, p.background, CircleShape)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Journey Control (modern pill with dropdown)
// ---------------------------------------------------------------------------
@Composable
private fun JourneyControl(
    selectedJourney: JourneyType?,
    onJourneySelected: (JourneyType) -> Unit
) {
    val p = homePalette()
    var expanded by remember { mutableStateOf(false) }

    Box {
        GlowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.accent.copy(alpha = 0.12f))
                        .border(1.dp, p.borderBright.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_your_journey),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = p.textSecondary
                    )
                    Text(
                        text = journeyLabel(selectedJourney),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    p.accent.copy(alpha = 0.18f),
                                    p.accent.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (selectedJourney == null) R.string.home_choose
                            else R.string.home_edit
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.accent
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(p.cardElevated)
        ) {
            JourneyType.entries.forEach { journey ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = journeyLabel(journey),
                            color = p.textPrimary,
                            fontWeight = if (selectedJourney == journey) FontWeight.SemiBold
                            else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedJourney == journey)
                                Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedJourney == journey) p.accent else p.textSecondary
                        )
                    },
                    onClick = {
                        expanded = false
                        onJourneySelected(journey)
                    }
                )
            }
        }
    }
}

@Composable
private fun journeyLabel(journey: JourneyType?): String {
    return stringResource(
        when (journey) {
            JourneyType.EARLY_DETECTION -> R.string.home_journey_early
            JourneyType.UNDER_EVALUATION -> R.string.home_journey_evaluation
            JourneyType.DIAGNOSED -> R.string.home_journey_diagnosed
            null -> R.string.home_journey_unselected
        }
    )
}

// ---------------------------------------------------------------------------
// Hero NextStep Card — Fully Animated & Living
// ---------------------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NextStepCard(
    journey: JourneyType?,
    enableAnimations: Boolean,
    lungStatusPercent: Int?,
    modifier: Modifier = Modifier,
    onOpenScreening: () -> Unit,
    onOpenQuestions: () -> Unit,
    onOpenSymptoms: () -> Unit
) {
    val p = homePalette()

    // ---------- Rotating messages based on journey ----------
    val rotatingMessages = when (journey) {
        JourneyType.EARLY_DETECTION -> listOf(
            R.string.home_rotating_early_1,
            R.string.home_rotating_early_2,
            R.string.home_rotating_early_3,
            R.string.home_rotating_early_4
        )
        JourneyType.UNDER_EVALUATION -> listOf(
            R.string.home_rotating_eval_1,
            R.string.home_rotating_eval_2,
            R.string.home_rotating_eval_3,
            R.string.home_rotating_eval_4
        )
        JourneyType.DIAGNOSED -> listOf(
            R.string.home_rotating_care_1,
            R.string.home_rotating_care_2,
            R.string.home_rotating_care_3,
            R.string.home_rotating_care_4
        )
        null -> listOf(
            R.string.home_rotating_default_1,
            R.string.home_rotating_default_2,
            R.string.home_rotating_default_3,
            R.string.home_rotating_default_4
        )
    }

    var messageIndex by remember(journey) { mutableIntStateOf(0) }

    // Auto-rotate messages every 4 seconds
    LaunchedEffect(journey, enableAnimations) {
        if (!enableAnimations) return@LaunchedEffect
        while (true) {
            delay(4000)
            messageIndex = (messageIndex + 1) % rotatingMessages.size
        }
    }

    // ---------- Animations ----------
    val infiniteTransition = rememberInfiniteTransition(label = "nextStepAnims")

    // Lung pulse animation
    val lungPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lungPulse"
    )

    // Lung breathing scale
    val lungScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lungScale"
    )

    // Shimmer effect across the card
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Floating particles
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    // Rotating dot indicator
    val dotRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotRotation"
    )

    // ---------- Journey-based content ----------
    val titleRes = when (journey) {
        JourneyType.EARLY_DETECTION -> R.string.home_step_early_title
        JourneyType.UNDER_EVALUATION -> R.string.home_step_eval_title
        JourneyType.DIAGNOSED -> R.string.home_step_care_title
        null -> R.string.home_step_default_title
    }

    val actionRes = when (journey) {
        JourneyType.EARLY_DETECTION -> R.string.home_open_screening
        JourneyType.UNDER_EVALUATION -> R.string.home_open_questions
        JourneyType.DIAGNOSED -> R.string.home_track_symptom
        null -> R.string.home_open_questions
    }

    val onAction = when (journey) {
        JourneyType.EARLY_DETECTION -> onOpenScreening
        JourneyType.UNDER_EVALUATION -> onOpenQuestions
        JourneyType.DIAGNOSED -> onOpenSymptoms
        null -> onOpenQuestions
    }

    // Fixed dark "gaming" hero: same look in light and dark mode, no pale gradients.
    val heroTop = Color(0xFF060D22)
    val heroBottom = Color(0xFF0B2150)
    val neon = Color(0xFF39D0FF)
    val heroShape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(Brush.linearGradient(listOf(heroTop, heroBottom)))
            .border(1.5.dp, Brush.linearGradient(listOf(neon, Color(0xFF6A5CFF))), heroShape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(neon.copy(alpha = lungPulse)))
            Text(
                text = stringResource(R.string.home_next_step).uppercase(),
                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, color = neon,
                modifier = Modifier.weight(1f)
            )
            if (lungStatusPercent != null) {
                Text(
                    text = stringResource(R.string.home_lung_status) + " " + lungStatusPercent + "%",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0x3339D0FF)).padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(titleRes),
                    fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                )
                androidx.compose.animation.Crossfade(targetState = messageIndex, label = "heroMsg") { idx ->
                    Text(
                        text = stringResource(rotatingMessages[idx % rotatingMessages.size]),
                        fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFFC3DAF2)
                    )
                }
            }
            Canvas(Modifier.size(104.dp)) {
                val c = Offset(size.width / 2, size.height / 2)
                val r = size.minDimension / 2
                drawCircle(neon.copy(alpha = 0.10f), radius = r * 0.92f)
                drawCircle(neon.copy(alpha = 0.55f), radius = r * 0.92f, style = Stroke(2.dp.toPx()))
                drawCircle(Color(0xFF6A5CFF).copy(alpha = 0.6f), radius = r * 0.70f, style = Stroke(1.dp.toPx()))
                // orbiting star
                val ang = Math.toRadians(dotRotation.toDouble())
                val sx = c.x + (r * 0.92f) * Math.cos(ang).toFloat()
                val sy = c.y + (r * 0.92f) * Math.sin(ang).toFloat()
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(sx, sy))
                drawCircle(neon.copy(alpha = 0.35f), radius = 8.dp.toPx(), center = Offset(sx, sy))
                // lungs
                val w = size.width; val h = size.height
                fun lung(mirror: Boolean): Path {
                    fun x(v: Float) = if (mirror) w - v * w else v * w
                    return Path().apply {
                        moveTo(x(0.47f), h * 0.36f)
                        cubicTo(x(0.30f), h * 0.34f, x(0.20f), h * 0.52f, x(0.22f), h * 0.66f)
                        cubicTo(x(0.24f), h * 0.74f, x(0.40f), h * 0.74f, x(0.46f), h * 0.64f)
                        close()
                    }
                }
                val stroke = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round)
                drawPath(lung(false), neon.copy(alpha = 0.25f * lungPulse + 0.1f))
                drawPath(lung(true), neon.copy(alpha = 0.25f * lungPulse + 0.1f))
                drawPath(lung(false), neon, style = stroke)
                drawPath(lung(true), neon, style = stroke)
                drawLine(neon, Offset(w * 0.5f, h * 0.22f), Offset(w * 0.5f, h * 0.45f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(neon, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.40f, h * 0.52f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(neon, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.60f, h * 0.52f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            rotatingMessages.indices.forEach { i ->
                val sel = i == messageIndex % rotatingMessages.size
                Box(
                    Modifier.height(6.dp).width(if (sel) 24.dp else 6.dp).clip(RoundedCornerShape(50))
                        .background(if (sel) neon else Color(0x5539D0FF))
                )
            }
        }
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neon, contentColor = Color(0xFF04101F)),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
        ) {
            Text(text = stringResource(actionRes), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Lung Illustration (polished)
// ---------------------------------------------------------------------------
@Composable
private fun LungIllustration(
    modifier: Modifier = Modifier,
    showFrame: Boolean = false,
    alpha: Float = 1f
) {
    val p = homePalette()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = p.accent.copy(alpha = alpha)
        val mainStroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        if (showFrame) {
            val len = w * 0.22f
            val sw = 2.2.dp.toPx()
            val inset = sw / 2f

            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(color, Offset(x, y), Offset(x + dx * len, y), sw, StrokeCap.Round)
                drawLine(color, Offset(x, y), Offset(x, y + dy * len), sw, StrokeCap.Round)
            }

            corner(inset, inset, 1f, 1f)
            corner(w - inset, inset, -1f, 1f)
            corner(inset, h - inset, 1f, -1f)
            corner(w - inset, h - inset, -1f, -1f)
        }

        val scale = if (showFrame) 0.62f else 1f
        val ox = w * (1f - scale) / 2f
        val oy = h * (1f - scale) / 2f

        fun lung(mirror: Boolean): Path {
            fun x(v: Float) = ox + (if (mirror) 1f - v else v) * w * scale
            fun y(v: Float) = oy + v * h * scale

            return Path().apply {
                moveTo(x(0.43f), y(0.34f))
                cubicTo(x(0.30f), y(0.30f), x(0.12f), y(0.50f), x(0.10f), y(0.78f))
                cubicTo(x(0.09f), y(0.92f), x(0.30f), y(0.95f), x(0.42f), y(0.86f))
                cubicTo(x(0.47f), y(0.70f), x(0.47f), y(0.50f), x(0.43f), y(0.34f))
                close()
            }
        }

        listOf(false, true).forEach { mirror ->
            val path = lung(mirror)
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    listOf(color.copy(alpha = alpha * 0.25f), Color.Transparent)
                )
            )
            drawPath(path = path, color = color, style = mainStroke)
        }

        val thin = 1.3.dp.toPx()
        fun px(v: Float) = ox + v * w * scale
        fun py(v: Float) = oy + v * h * scale

        drawLine(color, Offset(px(0.5f), py(0.06f)), Offset(px(0.5f), py(0.34f)), thin, StrokeCap.Round)

        listOf(1f, -1f).forEach { side ->
            val bx = 0.5f - 0.14f * side
            drawLine(color, Offset(px(0.5f), py(0.34f)), Offset(px(bx), py(0.52f)), thin, StrokeCap.Round)
            drawLine(color, Offset(px(bx), py(0.52f)), Offset(px(bx - 0.09f * side), py(0.63f)), thin, StrokeCap.Round)
            drawLine(color, Offset(px(bx), py(0.52f)), Offset(px(bx + 0.02f * side), py(0.72f)), thin, StrokeCap.Round)
        }
    }
}

// ---------------------------------------------------------------------------
// Vision Lab Card (eye-catching)
// ---------------------------------------------------------------------------
@Composable
private fun VisionLabCard(onOpenLab: () -> Unit) {
    val p = homePalette()

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanOffset"
    )

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        glow = true,
        glowIntensity = 1.1f,
        onClick = onOpenLab
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 100.dp)
                    .drawBehind {
                        // Animated scan line
                        val lineY = size.height * scanOffset
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    p.accent.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(0f, lineY),
                            end = Offset(size.width, lineY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
            ) {
                LungIllustration(
                    modifier = Modifier.fillMaxSize(),
                    showFrame = true
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = (
                                stringResource(R.string.home_vision_title) + " · " +
                                        stringResource(R.string.home_demo)
                                ).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = p.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = stringResource(R.string.home_vision_headline),
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )

                Text(
                    text = stringResource(R.string.home_vision_description),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = p.textSecondary
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_vision_open),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.accent
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Actions
// ---------------------------------------------------------------------------
@Composable
private fun QuickActionsSection(
    onTrackSymptom: () -> Unit,
    onMedicalRecords: () -> Unit,
    onDoctorQuestions: () -> Unit,
    onScreeningGuide: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeading(title = stringResource(R.string.home_quick_actions))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = stringResource(R.string.home_track_symptom),
                hint = stringResource(R.string.home_track_symptom_hint),
                icon = Icons.Outlined.Add,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onTrackSymptom
            )
            QuickActionCard(
                title = stringResource(R.string.home_open_screening),
                hint = stringResource(R.string.home_open_screening_hint),
                icon = Icons.Outlined.Air,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onScreeningGuide
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = stringResource(R.string.home_open_questions),
                hint = stringResource(R.string.home_open_questions_hint),
                icon = Icons.Outlined.QuestionAnswer,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onDoctorQuestions
            )
            QuickActionCard(
                title = stringResource(R.string.home_medical_records),
                hint = stringResource(R.string.home_medical_records_hint),
                icon = Icons.Outlined.Description,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onMedicalRecords
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    hint: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = homePalette()

    GlowCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                p.accent.copy(alpha = 0.15f),
                                p.accent.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(1.dp, p.borderBright.copy(alpha = 0.7f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hint,
                    fontSize = 12.sp,
                    color = p.accent.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Daily Tip, Section Heading, Articles
// ---------------------------------------------------------------------------
@Composable
private fun DailyTipCard(onReadMore: () -> Unit) {
    val p = homePalette()

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    p.warning.copy(alpha = 0.2f),
                                    p.warning.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = p.warning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.home_tip_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )
            }

            Text(
                text = stringResource(R.string.home_tip_body),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = p.textSecondary
            )

            TextButton(
                onClick = onReadMore,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.home_read_more),
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val p = homePalette()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = p.textPrimary
        )

        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: HomeArticle,
    onClick: () -> Unit
) {
    val p = homePalette()

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                p.accent.copy(alpha = 0.15f),
                                p.accent.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(1.dp, p.borderBright.copy(alpha = 0.7f), RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = article.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary
                )

                Text(
                    text = article.summary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = p.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.home_read_article),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = p.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------
@Composable
private fun ArticleLibraryDialog(
    articles: List<HomeArticle>,
    onOpenArticle: (HomeArticle) -> Unit,
    onDismiss: () -> Unit
) {
    val p = homePalette()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = p.cardBottom,
        titleContentColor = p.textPrimary,
        textContentColor = p.textSecondary,
        title = {
            Text(
                text = stringResource(R.string.home_articles_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = { onOpenArticle(article) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.home_close),
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent
                )
            }
        }
    )
}

@Composable
private fun ArticleReaderDialog(
    article: HomeArticle,
    onDismiss: () -> Unit
) {
    val p = homePalette()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = p.cardBottom,
        titleContentColor = p.textPrimary,
        textContentColor = p.textSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(p.accent.copy(alpha = 0.15f))
                    .border(1.dp, p.borderBright.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(text = article.title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = article.body,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = p.textPrimary
                )

                Text(
                    text = stringResource(R.string.home_article_disclaimer),
                    fontSize = 12.sp,
                    color = p.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.accent.copy(alpha = 0.1f))
                        .padding(14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.home_close),
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent
                )
            }
        }
    )
}

@Composable
private fun starterArticles(): List<HomeArticle> {
    return listOf(
        HomeArticle(
            id = "prepare_visit",
            title = stringResource(R.string.home_article_visit_title),
            summary = stringResource(R.string.home_article_visit_summary),
            body = stringResource(R.string.home_article_visit_body),
            icon = Icons.AutoMirrored.Filled.EventNote
        ),
        HomeArticle(
            id = "prepare_questions",
            title = stringResource(R.string.home_article_questions_title),
            summary = stringResource(R.string.home_article_questions_summary),
            body = stringResource(R.string.home_article_questions_body),
            icon = Icons.Default.QuestionAnswer
        ),
        HomeArticle(
            id = "organize_records",
            title = stringResource(R.string.home_article_records_title),
            summary = stringResource(R.string.home_article_records_summary),
            body = stringResource(R.string.home_article_records_body),
            icon = Icons.Default.FolderOpen
        )
    )
}