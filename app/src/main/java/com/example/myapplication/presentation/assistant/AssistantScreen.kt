package com.example.myapplication.presentation.assistant

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.assistant.FailureKind
import com.example.myapplication.presentation.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun FailureKind.toMessageRes(): Int = when (this) {
    FailureKind.NO_INTERNET, FailureKind.HOST_UNREACHABLE -> R.string.error_offline
    FailureKind.PERMISSION -> R.string.error_network_permission
    FailureKind.TIMEOUT -> R.string.error_timeout
    FailureKind.TLS -> R.string.error_secure_connection
    FailureKind.UNKNOWN -> R.string.error_generic
}

/* ───────────────────────── الربط بالـ ViewModel ───────────────────────── */
@Composable
fun AssistantScreen(viewModel: AssistantViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var localUrgentNotice by rememberSaveable { mutableStateOf(false) }

    val showUrgentNotice = localUrgentNotice || state.showEmergencyAlert

    val resolvedErrorMsg = if (state.failureKind != null) {
        stringResource(state.failureKind!!.toMessageRes())
    } else {
        state.errorMessage
    }

    AssistantContent(
        messages = state.messages,
        isAiTyping = state.isLoading,
        errorMessage = resolvedErrorMsg,
        showUrgentNotice = showUrgentNotice,
        userName = "Moaz",
        onSend = { text ->
            if (UrgentInputDetector.isUrgent(text)) localUrgentNotice = true
            viewModel.sendMessage(text)
        },
        onRetry = viewModel::retry,
        onDismissUrgentNotice = {
            localUrgentNotice = false
            viewModel.dismissEmergencyAlert()
        }
    )
}

/** قائمة الكلمات العاجلة للتنبيه المباشر في الواجهة */
internal object UrgentInputDetector {
    private val phrases = listOf(
        "coughing up blood", "coughing blood", "cough up blood",
        "can't breathe", "cannot breathe", "severe chest pain",
        "سعال دم", "بكح دم", "كحة بدم", "مش قادر اتنفس", "مش قادر أتنفس",
        "لا أستطيع التنفس", "ألم شديد في الصدر", "الم شديد في الصدر"
    )

    fun isUrgent(text: String): Boolean {
        val normalized = text.lowercase()
        return phrases.any { it in normalized } || MedicalSafetyGuard.containsEmergencySymptoms(text)
    }
}

/* ───────────────────────── الشاشة (Stateless) ───────────────────────── */

@Composable
fun AssistantContent(
    messages: List<ChatMessage>,
    isAiTyping: Boolean,
    errorMessage: String?,
    showUrgentNotice: Boolean,
    userName: String,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onDismissUrgentNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = com.example.myapplication.presentation.home.homePalette()
    val colors = MaterialTheme.colorScheme
    var input by rememberSaveable { mutableStateOf("") }
    val hasConversation = messages.any { it.isUser }

    val seenKeys = remember {
        messages.indices.mapTo(mutableSetOf()) { messageKey(it, messages[it]) }
    }

    val submit: (String) -> Unit = { text ->
        val clean = text.trim()
        if (clean.isNotEmpty() && !isAiTyping) {
            onSend(clean)
            input = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    0f to p.bgGlow.copy(alpha = 0.45f),
                    0.35f to p.bgGlowSecondary.copy(alpha = 0.20f),
                    0.75f to Color.Transparent
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        AssistantHeader(isAiTyping = isAiTyping)

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
                targetState = hasConversation,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "assistantBody"
            ) { started ->
                if (started) {
                    ConversationList(
                        messages = messages,
                        isAiTyping = isAiTyping,
                        errorMessage = errorMessage,
                        seenKeys = seenKeys,
                        onRetry = onRetry
                    )
                } else {
                    WelcomeHero(userName = userName, onPromptClick = submit)
                }
            }
        }

        ChatInputBar(
            value = input,
            onValueChange = { input = it },
            isBusy = isAiTyping,
            onSend = { submit(input) }
        )
    }
}

private fun messageKey(index: Int, message: ChatMessage) = "$index-${message.id}"

@Composable
private fun bubbleTextStyle(): TextStyle = MaterialTheme.typography.bodyMedium.copy(
    fontSize = 14.sp,
    lineHeight = 22.sp,
    textDirection = TextDirection.Content
)

/* ───────────────────────── Header ───────────────────────── */

@Composable
private fun AssistantHeader(isAiTyping: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface.copy(alpha = 0.95f),
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AiAvatar(size = 46.dp, pulsing = isAiTyping)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.assistant_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                if (isAiTyping) colors.primary else colors.secondary,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(6.dp))
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
            }
        }
    }
}

@Composable
private fun AiAvatar(size: Dp, pulsing: Boolean = false) {
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
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/* ───────────────────────── Welcome ───────────────────────── */

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
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AiAvatar(size = 80.dp, pulsing = true)
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.assistant_welcome_title, userName),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.assistant_welcome_body),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        prompts.forEachIndexed { index, (icon, text) ->
            Entrance(animate = true, delayMillis = 120 + index * 90) {
                PromptCard(icon = icon, text = text, onClick = { onPromptClick(text) })
            }
            if (index != prompts.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PromptCard(icon: ImageVector, text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Surface(
        shape = shape,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/* ───────────────────────── Conversation ───────────────────────── */

@Composable
private fun ConversationList(
    messages: List<ChatMessage>,
    isAiTyping: Boolean,
    errorMessage: String?,
    seenKeys: MutableSet<String>,
    onRetry: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showJumpToLatest by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    LaunchedEffect(messages.size, isAiTyping, errorMessage) {
        listState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                item(key = "error") {
                    Entrance(animate = true) { ErrorBubble(message = errorMessage, onRetry = onRetry) }
                }
            }
            if (isAiTyping) {
                item(key = "typing") {
                    Entrance(animate = true) { TypingBubble() }
                }
            }
            itemsIndexed(
                items = messages.asReversed(),
                key = { reversedIndex, message -> messageKey(messages.lastIndex - reversedIndex, message) }
            ) { reversedIndex, message ->
                val key = messageKey(messages.lastIndex - reversedIndex, message)
                val isNew = remember(key) { seenKeys.add(key) }
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
                .padding(bottom = 8.dp)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 52.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(colors.primary, lerp(colors.primary, colors.secondary, 0.60f))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = bubbleTextStyle(),
                color = colors.onPrimary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = message.timestamp, fontSize = 11.sp, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun AiBubble(message: ChatMessage, typewriter: Boolean) {
    val colors = MaterialTheme.colorScheme
    val (visibleText, isComplete) = rememberTypewriter(message.text, enabled = typewriter)

    val isEmergency = message.isEmergency

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 40.dp),
        verticalAlignment = Alignment.Top
    ) {
        AiAvatar(size = 30.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp),
                color = if (isEmergency) colors.errorContainer else colors.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isEmergency) colors.error else colors.outlineVariant.copy(alpha = 0.7f)
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(visibleText)
                        if (!isComplete) withStyle(SpanStyle(color = colors.primary)) { append("●") }
                    },
                    style = bubbleTextStyle(),
                    color = if (isEmergency) colors.onErrorContainer else colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            AnimatedVisibility(visible = isComplete, enter = fadeIn() + expandVertically()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.assistant_ai_label) + " · " + message.timestamp,
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    CopyButton(text = message.text)
                }
            }
        }
    }
}

/** بيظهر النص كلمة كلمة. بيرجع (النص الظاهر، خلص ولا لسه). */
@Composable
private fun rememberTypewriter(text: String, enabled: Boolean): Pair<String, Boolean> {
    val stops = remember(text) {
        buildList {
            text.forEachIndexed { i, c -> if (c.isWhitespace()) add(i + 1) }
            add(text.length)
        }
    }
    var stopIndex by remember(text) { mutableIntStateOf(if (enabled) 0 else stops.lastIndex) }

    LaunchedEffect(text) {
        val perWord = (3000L / stops.size).coerceIn(12L, 45L)
        while (stopIndex < stops.lastIndex) {
            delay(perWord)
            stopIndex++
        }
    }
    return text.take(stops[stopIndex]) to (stopIndex >= stops.lastIndex)
}

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
        AiAvatar(size = 30.dp)
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f)),
            shadowElevation = 1.dp
        ) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
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
        shape = RoundedCornerShape(16.dp),
        color = colors.errorContainer,
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
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
        shape = RoundedCornerShape(16.dp),
        color = colors.errorContainer,
        border = BorderStroke(1.dp, colors.error),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = colors.error)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
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
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.primary else colors.outlineVariant,
        label = "inputBorder"
    )

    Surface(
        color = colors.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = { Text(stringResource(R.string.ask_placeholder), fontSize = 14.sp, color = colors.onSurfaceVariant) },
                        textStyle = bubbleTextStyle(),
                        maxLines = 4,
                        interactionSource = interaction,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.width(8.dp))
                SendButton(enabled = value.isNotBlank() && !isBusy, isBusy = isBusy, onClick = onSend)
            }
            Text(
                text = stringResource(R.string.assistant_disclaimer),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, isBusy: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (enabled) colors.primary else colors.surfaceVariant, label = "sendContainer"
    )
    val content by animateColorAsState(
        if (enabled) colors.onPrimary else colors.onSurfaceVariant, label = "sendContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )

    Box(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(container)
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
        val scope = rememberCoroutineScope()

        AssistantContent(
            messages = messages,
            isAiTyping = typing,
            errorMessage = null,
            showUrgentNotice = false,
            userName = "Moaz",
            onSend = { text ->
                messages = messages + ChatMessage("u${messages.size}", text, true, "10:00")
                typing = true
                scope.launch {
                    delay(1600)
                    messages = messages + ChatMessage(
                        "a${messages.size}",
                        "This is a demo reply so you can see the typing animation word by word. " +
                            "Real answers will come from your backend once it is connected.",
                        false,
                        "10:00"
                    )
                    typing = false
                }
            },
            onRetry = {},
            onDismissUrgentNotice = {}
        )
    }
}
