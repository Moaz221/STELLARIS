package com.example.myapplication.presentation.assistant

import com.example.myapplication.core.ui.locNow
import com.example.myapplication.core.ui.loc
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.assistant.FailureKind
import com.example.myapplication.presentation.home.homePalette
import com.example.myapplication.presentation.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OnlineGreen = Color(0xFF22C55E)

private fun FailureKind.toMessageRes(): Int = when (this) {
    FailureKind.NO_INTERNET, FailureKind.HOST_UNREACHABLE -> R.string.error_offline
    FailureKind.PERMISSION -> R.string.error_network_permission
    FailureKind.TIMEOUT -> R.string.error_timeout
    FailureKind.TLS -> R.string.error_secure_connection
    FailureKind.UNKNOWN -> R.string.error_generic
}

@Composable
private fun ChatError.asText(): String = when (this) {
    is ChatError.Server -> stringResource(R.string.chat_error_server, code, message)
    is ChatError.Network -> stringResource(kind.toMessageRes())
    ChatError.Interrupted -> stringResource(R.string.chat_error_interrupted)
}

/* ───────────────────────── الربط بالـ ViewModel ───────────────────────── */

/**
 * الـ draft (النص اللي المستخدم بيكتبه) متشال فوق في AssistantFlow عشان ما يضيعش
 * لما المستخدم يفتح شاشة الـ History ويرجع.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    input: String,
    onInputChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val fallbackName = stringResource(R.string.chat_default_user_name)
    val userName = remember(fallbackName) {
        FirebaseAuth.getInstance().currentUser?.displayName
            ?.trim()
            ?.substringBefore(' ')
            ?.takeIf { it.isNotEmpty() }
            ?: fallbackName
    }

    AssistantContent(
        conversationId = state.conversationId,
        messages = state.messages,
        isAiTyping = state.isLoading,
        isRestoring = state.isRestoring,
        error = state.error,
        showUrgentNotice = state.showEmergencyAlert,
        userName = userName,
        input = input,
        onInputChange = onInputChange,
        onSend = viewModel::sendMessage,
        onRetry = viewModel::retry,
        onDismissUrgentNotice = viewModel::dismissEmergencyAlert,
        onBack = onBack,
        onOpenHistory = onOpenHistory,
        onNewChat = viewModel::startNewConversation,
        modifier = modifier
    )
}

/* ───────────────────────── الشاشة (Stateless) ───────────────────────── */

@Composable
fun AssistantContent(
    conversationId: String,
    messages: List<ChatMessage>,
    isAiTyping: Boolean,
    isRestoring: Boolean,
    error: ChatError?,
    showUrgentNotice: Boolean,
    userName: String,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onDismissUrgentNotice: () -> Unit,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = homePalette()
    val colors = MaterialTheme.colorScheme
    val hasConversation = messages.any { it.isUser }
    val errorText = error?.asText()

    // الرسايل الموجودة وقت فتح المحادثة (أو بعد ما تخلّص تحميل) تتعتبر "اتشافت"
    // فمتعملش animation ولا typewriter. بس الرسايل الجديدة هي اللي بتتحرك.
    val seenKeys = remember(conversationId, isRestoring) {
        messages.mapTo(mutableSetOf()) { it.id }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }
    val canSend = !isAiTyping && !isRestoring
    val submit: (String) -> Unit = { text ->
        val clean = text.trim()
        if (clean.isNotEmpty() && canSend) {
            onSend(clean)
            onInputChange("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.background)
            // توهّج هادي من فوق + توهّج ثانوي من تحت، بيدّي إحساس "عمق" طبي نضيف
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(p.bgGlow.copy(alpha = 0.50f), Color.Transparent),
                        center = Offset(size.width * 0.85f, 0f),
                        radius = size.width * 1.1f
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(p.bgGlowSecondary.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(0f, size.height * 0.95f),
                        radius = size.width * 1.0f
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        FloatingAssistantBar(
            isAiTyping = isAiTyping,
            showRobot = hasConversation,
            scrolled = scrolled,
            canStartNew = hasConversation,
            onBack = onBack,
            onOpenHistory = onOpenHistory,
            onNewChat = onNewChat
        )

        AnimatedVisibility(
            visible = showUrgentNotice,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            UrgentNotice(onDismiss = onDismissUrgentNotice)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = when {
                    isRestoring -> BodyState.Restoring
                    hasConversation -> BodyState.Conversation
                    else -> BodyState.Welcome
                },
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "assistantBody"
            ) { body ->
                when (body) {
                    BodyState.Restoring -> RestoringIndicator()
                    BodyState.Conversation -> ConversationList(
                        messages = messages,
                        isAiTyping = isAiTyping,
                        errorText = errorText,
                        seenKeys = seenKeys,
                        listState = listState,
                        onRetry = onRetry
                    )

                    BodyState.Welcome -> WelcomeHero(userName = userName, onPromptClick = submit)
                }
            }
        }

        ChatInputBar(
            value = input,
            onValueChange = onInputChange,
            isBusy = isAiTyping,
            canSend = canSend && input.isNotBlank(),
            onSend = { submit(input) }
        )
    }
}

private enum class BodyState { Restoring, Conversation, Welcome }

@Composable
private fun bubbleTextStyle(): TextStyle = MaterialTheme.typography.bodyMedium.copy(
    fontSize = 15.sp,
    lineHeight = 24.sp,
    textDirection = TextDirection.Content
)

@Composable
private fun RestoringIndicator() {
    val label = stringResource(R.string.chat_loading_conversation)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
    }
}

/* ───────────────────────── Header ───────────────────────── */

@Composable
private fun RoundIconButton(icon: ImageVector, description: String, enabled: Boolean = true, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = colors.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = if (enabled) 0.7f else 0.3f)),
        shadowElevation = 3.dp,
        modifier = Modifier.size(42.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f), modifier = Modifier.size(21.dp))
        }
    }
}

/** No app bar: transparent row with floating round buttons and a small animated robot. */
@Composable
private fun FloatingAssistantBar(
    isAiTyping: Boolean,
    showRobot: Boolean,
    scrolled: Boolean,
    canStartNew: Boolean,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).height(56.dp)) {
        Box(Modifier.align(Alignment.CenterStart)) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.chat_back), onClick = onBack)
        }
        if (showRobot) {
            val robotSize by androidx.compose.animation.core.animateDpAsState(if (scrolled) 30.dp else 52.dp, tween(250), label = "robotSize")
            val robotAlpha by androidx.compose.animation.core.animateFloatAsState(if (scrolled) 0.45f else 1f, tween(250), label = "robotAlpha")
            Row(Modifier.align(Alignment.Center).graphicsLayer { alpha = robotAlpha }, verticalAlignment = Alignment.CenterVertically) {
                AiAvatar(size = robotSize, pulsing = isAiTyping, thinking = isAiTyping)
                if (isAiTyping) {
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.assistant_status_typing), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoundIconButton(Icons.Filled.Add, stringResource(R.string.chat_new_chat), enabled = canStartNew, onClick = onNewChat)
            RoundIconButton(Icons.Outlined.History, stringResource(R.string.chat_history), onClick = onOpenHistory)
        }
    }
}

@Composable
private fun AssistantHeader(
    isAiTyping: Boolean,
    canStartNew: Boolean,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface.copy(alpha = 0.88f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.chat_back),
                        tint = colors.onSurface
                    )
                }

                // أفاتار + نقطة حالة (أخضر = متصل)
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    AiAvatar(size = 40.dp, pulsing = isAiTyping)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(13.dp)
                            .background(colors.surface, CircleShape)
                            .padding(2.dp)
                            .background(
                                if (isAiTyping) colors.primary else OnlineGreen,
                                CircleShape
                            )
                    )
                }

                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.assistant_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1
                    )
                    AnimatedContent(
                        targetState = isAiTyping,
                        transitionSpec = {
                            fadeIn() + slideInVertically { it / 2 } togetherWith
                                    fadeOut() + slideOutVertically { -it / 2 }
                        },
                        label = "assistantStatus"
                    ) { typing ->
                        Text(
                            text = stringResource(
                                if (typing) R.string.assistant_status_typing
                                else R.string.assistant_status_online
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (typing) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onNewChat, enabled = canStartNew) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.chat_new_chat),
                        tint = if (canStartNew) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
                    )
                }
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = stringResource(R.string.chat_history),
                        tint = colors.onSurface
                    )
                }
            }
        }
        // خط فاصل رفيع بتدرّج بدل البوردر الصلب
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            colors.primary.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun AiAvatar(size: Dp, pulsing: Boolean = false, animated: Boolean = true, thinking: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        if (pulsing) {
            val transition = rememberInfiniteTransition(label = "avatarPulse")
            val pulse by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
                label = "pulse"
            )
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val s = 1f + 0.40f * pulse
                        scaleX = s
                        scaleY = s
                        alpha = 0.30f * (1f - pulse)
                    }
                    .background(colors.primary, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(colors.primary, lerp(colors.primary, colors.secondary, 0.65f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val comp by com.airbnb.lottie.compose.rememberLottieComposition(
                com.airbnb.lottie.compose.LottieCompositionSpec.RawRes(if (thinking) R.raw.robot_think else R.raw.robot_face)
            )
            if (animated) {
                com.airbnb.lottie.compose.LottieAnimation(
                    composition = comp,
                    iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(0.92f)
                )
            } else {
                com.airbnb.lottie.compose.LottieAnimation(
                    composition = comp,
                    progress = { 0.35f },
                    modifier = Modifier.fillMaxSize(0.92f)
                )
            }
        }
    }
}

/* ───────────────────────── Welcome ───────────────────────── */

/** الأفاتار الكبير بحلقات نبض بتتوسّع حواليه، زي إشارة "مراقبة" طبية هادية. */
@Composable
private fun HeroOrb(size: Dp) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "heroOrb")
    val ring1 = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart),
        label = "ring1"
    )
    val ring2 = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart, StartOffset(1500)),
        label = "ring2"
    )
    val primary = colors.primary
    val secondary = colors.secondary

    Box(modifier = Modifier.size(size * 2f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val maxR = this.size.minDimension / 2f
            // هالة ناعمة ثابتة
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primary.copy(alpha = 0.22f), secondary.copy(alpha = 0.08f), Color.Transparent)
                ),
                radius = maxR
            )
            listOf(ring1.value, ring2.value).forEach { t ->
                drawCircle(
                    color = primary.copy(alpha = 0.35f * (1f - t)),
                    radius = maxR * (0.50f + 0.50f * t),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
        Box(
            Modifier.shadow(
                elevation = 18.dp,
                shape = CircleShape,
                ambientColor = primary,
                spotColor = primary
            )
        ) {
            AiAvatar(size = size)
        }
    }
}

@Composable
private fun WelcomeHero(userName: String, onPromptClick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val prompts: List<Pair<ImageVector, String>> = listOf(
        Icons.Outlined.HealthAndSafety to stringResource(R.string.assistant_prompt_eligibility),
        Icons.Outlined.MonitorHeart to stringResource(R.string.assistant_prompt_symptoms),
        Icons.Outlined.QuestionAnswer to stringResource(R.string.assistant_prompt_questions)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeroOrb(size = 104.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.assistant_welcome_title, userName),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.assistant_welcome_body),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(28.dp))

        prompts.forEachIndexed { index, (icon, text) ->
            Entrance(animate = true, delayMillis = 150 + index * 100) {
                PromptCard(icon = icon, text = text, onClick = { onPromptClick(text) })
            }
            if (index != prompts.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PromptCard(icon: ImageVector, text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Surface(
        shape = shape,
        color = colors.surface,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    colors.primary.copy(alpha = 0.35f),
                    colors.outlineVariant.copy(alpha = 0.5f)
                )
            )
        ),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.primary.copy(alpha = 0.18f),
                                colors.secondary.copy(alpha = 0.10f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/* ───────────────────────── Conversation ───────────────────────── */

@Composable
private fun ConversationList(
    messages: List<ChatMessage>,
    isAiTyping: Boolean,
    errorText: String?,
    seenKeys: MutableSet<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRetry: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val showJumpToLatest by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }
    val reversed = remember(messages) { messages.asReversed() }
    val lastIsUser = messages.lastOrNull()?.isUser == true

    // بننزل لآخر الشات بس لو المستخدم قريب من الآخر أو هو اللي لسه باعت رسالة.
    // لو بيقرا رسايل قديمة منسحبوش غصب (وزرار "انزل لآخر الشات" بيظهر).
    LaunchedEffect(messages.size, isAiTyping, errorText) {
        val nearBottom = listState.firstVisibleItemIndex <= 1
        if (nearBottom || lastIsUser) listState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorText != null) {
                item(key = "error") {
                    Entrance(animate = true) { ErrorBubble(message = errorText, onRetry = onRetry) }
                }
            }
            if (isAiTyping) {
                item(key = "typing") {
                    Entrance(animate = true) { TypingBubble() }
                }
            }
            items(items = reversed, key = { it.id }) { message ->
                val isNew = remember(message.id) { seenKeys.add(message.id) }
                Entrance(animate = isNew, fromEnd = message.isUser) {
                    if (message.isUser) UserBubble(message)
                    else AiBubble(message = message, typewriter = isNew)
                }
            }
        }

        AnimatedVisibility(
            visible = showJumpToLatest,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = colors.surface,
                contentColor = colors.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.assistant_scroll_bottom)
                )
            }
        }
    }
}

@Composable
private fun UserBubble(message: ChatMessage) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 4.dp, bottomStart = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = colors.primary.copy(alpha = 0.4f),
                    spotColor = colors.primary.copy(alpha = 0.4f)
                )
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(colors.primary, lerp(colors.primary, colors.secondary, 0.60f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                style = bubbleTextStyle(),
                color = colors.onPrimary
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = rememberTimeText(message.timestamp),
            fontSize = 11.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(end = 6.dp)
        )
    }
}

@Composable
private fun AiBubble(message: ChatMessage, typewriter: Boolean) {
    val colors = MaterialTheme.colorScheme
    val isEmergency = message.isEmergency

    // رسالة الطوارئ بتتخزن فاضية وبتتعرض مترجمة من strings.xml
    val displayText = if (isEmergency) {
        stringResource(R.string.chat_emergency_message)
    } else {
        message.text
    }
    val (visibleText, isComplete) = rememberTypewriter(displayText, enabled = typewriter)
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 32.dp),
        verticalAlignment = Alignment.Top
    ) {
        AiAvatar(size = 32.dp, animated = false)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Surface(
                shape = shape,
                color = if (isEmergency) colors.errorContainer else colors.surface,
                border = BorderStroke(
                    1.dp,
                    if (isEmergency) colors.error else colors.outlineVariant.copy(alpha = 0.6f)
                ),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (isEmergency) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = buildAnnotatedString {
                            append(visibleText)
                            if (!isComplete) withStyle(SpanStyle(color = colors.primary)) { append(" ●") }
                        },
                        style = bubbleTextStyle(),
                        color = if (isEmergency) colors.onErrorContainer else colors.onSurface,
                        modifier = Modifier
                            // قارئ الشاشة يقرا النص كامل مش وهو بيتكتب كلمة كلمة
                            .semantics { contentDescription = displayText }
                    )
                }
            }
            AnimatedVisibility(visible = isComplete, enter = fadeIn() + expandVertically()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.assistant_ai_label) + " · " +
                                rememberTimeText(message.timestamp),
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    CopyButton(text = displayText)
                }
            }
        }
    }
}

/**
 * بيظهر النص كلمة كلمة. بيرجع (النص الظاهر، خلص ولا لسه).
 * الزمن الكلي ثابت تقريباً (~3 ثواني): النص الطويل بيظهر أكتر من كلمة في كل خطوة.
 */
@Composable
private fun rememberTypewriter(text: String, enabled: Boolean): Pair<String, Boolean> {
    val stops = remember(text) {
        buildList {
            text.forEachIndexed { i, c -> if (c.isWhitespace()) add(i + 1) }
            add(text.length)
        }.distinct()
    }
    val step = remember(stops) { (stops.size / 120).coerceAtLeast(1) }
    var stopIndex by remember(text) { mutableIntStateOf(if (enabled) 0 else stops.lastIndex) }

    LaunchedEffect(text) {
        while (stopIndex < stops.lastIndex) {
            delay(25L)
            stopIndex = (stopIndex + step).coerceAtMost(stops.lastIndex)
        }
    }
    return text.take(stops[stopIndex]) to (stopIndex >= stops.lastIndex)
}

@Suppress("DEPRECATION")
@Composable
private fun CopyButton(text: String) {
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    IconButton(onClick = {
        clipboard.setText(AnnotatedString(text))
        copied = true
    }) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
            label = "copyIcon"
        ) { done ->
            Icon(
                imageVector = if (done) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                contentDescription = stringResource(
                    if (done) R.string.assistant_copied else R.string.assistant_copy
                ),
                tint = if (done) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    val colors = MaterialTheme.colorScheme
    val typingLabel = stringResource(R.string.assistant_status_typing)
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.semantics { contentDescription = typingLabel }
    ) {
        AiAvatar(size = 36.dp, thinking = true)
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.7f)),
            shadowElevation = 2.dp
        ) {
            Box(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                TypingDots(color = colors.primary)
            }
        }
    }
}

@Composable
private fun TypingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val bounce by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0f at 0
                        1f at 300
                        0f at 600
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 160)
                ),
                label = "dot$index"
            )
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        translationY = -bounce * 6.dp.toPx()
                        alpha = 0.4f + 0.6f * bounce
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun ErrorBubble(message: String, onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.errorContainer,
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive }
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = colors.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.onErrorContainer)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.assistant_retry))
            }
        }
    }
}

@Composable
private fun UrgentNotice(onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.errorContainer,
        border = BorderStroke(1.dp, colors.error),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // التنبيه الطبي لازم قارئ الشاشة يعلنه فوراً
            .semantics { liveRegion = LiveRegionMode.Assertive }
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.error.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).padding(top = 2.dp)) {
                Text(
                    text = stringResource(R.string.assistant_urgent_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onErrorContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.assistant_urgent_body),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.assistant_dismiss),
                    tint = colors.onErrorContainer
                )
            }
        }
    }
}

/* ───────────────────────── Input Bar ───────────────────────── */

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isBusy: Boolean,
    canSend: Boolean,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.primary else colors.outline,
        label = "inputBorder"
    )
    val pillShape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
    ) {
        // كبسولة عايمة (Floating pill) فيها الحقل + زرار الإرسال جوّاها
        Surface(
            shape = pillShape,
            color = colors.surface,
            border = BorderStroke(if (focused) 1.5.dp else 1.dp, borderColor),
            shadowElevation = if (focused) 8.dp else 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            stringResource(R.string.ask_placeholder),
                            fontSize = 14.sp,
                            color = colors.onSurfaceVariant
                        )
                    },
                    textStyle = bubbleTextStyle(),
                    maxLines = 4,
                    interactionSource = interaction,
                    // مفيش capitalization (ملهاش لازمة للعربي) وزرار الكيبورد بيبعت
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                SendButton(enabled = canSend, isBusy = isBusy, onClick = onSend)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = colors.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(13.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.assistant_disclaimer),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, isBusy: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val content by animateColorAsState(
        if (enabled) colors.onPrimary else colors.onSurfaceVariant, label = "sendContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )
    val activeBrush = Brush.linearGradient(
        listOf(colors.primary, lerp(colors.primary, colors.secondary, 0.6f))
    )

    Box(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .size(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (enabled) activeBrush
                else Brush.linearGradient(listOf(colors.surfaceVariant, colors.surfaceVariant))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = content,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = stringResource(R.string.assistant_send),
                tint = content
            )
        }
    }
}

/* ───────────────────────── حركة دخول مشتركة ───────────────────────── */

@Composable
private fun Entrance(
    animate: Boolean,
    fromEnd: Boolean = false,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LaunchedEffect(Unit) {
        if (animate) {
            delay(delayMillis.toLong())
            progress.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    Box(
        Modifier.graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * 28.dp.toPx()
            scaleX = 0.92f + 0.08f * p
            scaleY = 0.92f + 0.08f * p
            transformOrigin = TransformOrigin(if (fromEnd != isRtl) 1f else 0f, 1f)
        }
    ) { content() }
}

/* ───────────────────────── Preview تفاعلي (يستخدم ثيم الأبلكيشن) ───────────────────────── */

@Preview(name = "Light · EN", showBackground = true)
@Preview(name = "Dark · AR", locale = "ar", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AssistantDemoPreview() {
    MyApplicationTheme {
        var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
        var typing by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        AssistantContent(
            conversationId = "preview",
            messages = messages,
            isAiTyping = typing,
            isRestoring = false,
            error = null,
            showUrgentNotice = false,
            userName = "Moaz",
            input = input,
            onInputChange = { input = it },
            onSend = { text ->
                messages = messages + ChatMessage(
                    text = text,
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )
                typing = true
                scope.launch {
                    delay(1600)
                    messages = messages + ChatMessage(
                        text = locNow("This is a demo reply so you can see the typing animation word by word. Real answers will come from your backend once it is connected.", "ده رد تجريبي. الردود الحقيقية بتيجي من السيرفر لما يتوصل."),
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    typing = false
                }
            },
            onRetry = {},
            onDismissUrgentNotice = {},
            onBack = {},
            onOpenHistory = {},
            onNewChat = { messages = emptyList() }
        )
    }
}