package com.example.myapplication.presentation.assistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import com.example.myapplication.R
import com.example.myapplication.core.ui.loc
import com.example.myapplication.data.local.chat.ConversationSummary
import com.example.myapplication.presentation.home.homePalette
import java.util.Calendar

private fun dayBucket(millis: Long): Int {   // 0 today, 1 yesterday, 2 earlier
    val now = Calendar.getInstance()
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    fun key(x: Calendar) = x.get(Calendar.YEAR) * 1000 + x.get(Calendar.DAY_OF_YEAR)
    val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when (key(c)) { key(now) -> 0; key(yest) -> 1; else -> 2 }
}

private sealed interface Row2 {
    data class Header(val text: String) : Row2
    data class Item(val c: ConversationSummary) : Row2
}

/**
 * @param conversations null = still loading.
 * @param currentConversationId the conversation open in the chat now (marked "Current").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    conversations: List<ConversationSummary>?,
    currentConversationId: String?,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onNewChat: () -> Unit,
    onDelete: (String) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier,
    onRename: (String, String) -> Unit = { _, _ -> }
) {
    val p = homePalette()
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var renameFor by remember { mutableStateOf<ConversationSummary?>(null) }
    var renameText by remember { mutableStateOf("") }
    val hidden = remember { mutableStateListOf<String>() }   // swiped away, waiting for the undo window
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val deletedMsg = loc("Conversation deleted", "تم حذف المحادثة"); val undoLabel = loc("Undo", "تراجع")
    fun requestDelete(c: ConversationSummary) {
        hidden.add(c.id)
        val msg = deletedMsg; val undoL = undoLabel
        scope.launch {
            val r = snack.showSnackbar(msg, actionLabel = undoL, duration = SnackbarDuration.Short)
            if (r == SnackbarResult.ActionPerformed) hidden.remove(c.id) else { onDelete(c.id); hidden.remove(c.id) }
        }
    }

    val shown = conversations.orEmpty().filter { it.id !in hidden }.filter {
        query.isBlank() || it.title.contains(query, true) || (it.lastMessage ?: "").contains(query, true)
    }
    val todayL = loc("Today", "النهاردة"); val yestL = loc("Yesterday", "امبارح"); val earlierL = loc("Earlier", "أقدم")
    val rows = buildList<Row2> {
        var last = -1
        shown.forEach { c ->
            val b = dayBucket(c.updatedAt)
            if (b != last) { add(Row2.Header(when (b) { 0 -> todayL; 1 -> yestL; else -> earlierL })); last = b }
            add(Row2.Item(c))
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(p.background)
            .background(Brush.verticalGradient(0f to p.bgGlow.copy(alpha = 0.45f), 0.35f to p.bgGlowSecondary.copy(alpha = 0.20f), 0.75f to Color.Transparent))
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            HistoryHeader(count = conversations?.size ?: 0, hasItems = !conversations.isNullOrEmpty(), onBack = onBack, onDeleteAll = { confirmDeleteAll = true })

            if (!conversations.isNullOrEmpty()) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    placeholder = { Text(loc("Search conversations", "ابحث في المحادثات"), color = p.textTertiary) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = p.accent) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.accent, unfocusedBorderColor = p.borderBright.copy(alpha = 0.5f),
                        focusedTextColor = p.textPrimary, unfocusedTextColor = p.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            when {
                conversations == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = p.accent)
                }
                conversations.isEmpty() -> EmptyHistory(onNewChat)
                shown.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                    Text(loc("No conversation matches your search.", "مفيش محادثة مطابقة للبحث."), color = p.textSecondary, fontSize = 14.sp)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = rows, key = { if (it is Row2.Item) it.c.id else (it as Row2.Header).text }) { row ->
                        when (row) {
                            is Row2.Header -> Text(row.text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.accent, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
                            is Row2.Item -> {
                                val c = row.c
                                val dismiss = rememberSwipeToDismissBoxState(confirmValueChange = { v ->
                                    if (v != SwipeToDismissBoxValue.Settled) { requestDelete(c); true } else false
                                })
                                SwipeToDismissBox(
                                    state = dismiss,
                                    backgroundContent = {
                                        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(p.alert.copy(alpha = 0.25f)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                            Icon(Icons.Outlined.Delete, null, tint = p.alert)
                                        }
                                    }
                                ) {
                                    ConversationCard(
                                        item = c, isCurrent = c.id == currentConversationId,
                                        onClick = { onOpen(c.id) },
                                        onRename = { renameFor = c; renameText = c.title.trimEnd('…', ' ') },
                                        onDelete = { requestDelete(c) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // New chat button: neon pill, centered at the bottom
        if (!conversations.isNullOrEmpty()) {
            Button(
                onClick = onNewChat,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.background),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp).height(54.dp)
                    .shadow(14.dp, RoundedCornerShape(28.dp), ambientColor = p.accent, spotColor = p.accent)
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.chat_history_new_chat), fontWeight = FontWeight.Bold)
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 84.dp))
    }

    renameFor?.let { c ->
        AlertDialog(
            onDismissRequest = { renameFor = null },
            title = { Text(loc("Rename conversation", "تغيير اسم المحادثة")) },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it.take(60) }, singleLine = true) },
            confirmButton = { TextButton(onClick = { if (renameText.isNotBlank()) onRename(c.id, renameText); renameFor = null }) { Text(loc("Save", "حفظ")) } },
            dismissButton = { TextButton(onClick = { renameFor = null }) { Text(stringResource(R.string.chat_history_cancel)) } }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            icon = { Icon(Icons.Outlined.DeleteSweep, null, tint = p.alert) },
            title = { Text(stringResource(R.string.chat_history_delete_all_title)) },
            text = { Text(stringResource(R.string.chat_history_delete_all_body)) },
            confirmButton = {
                TextButton(onClick = { onDeleteAll(); confirmDeleteAll = false }) {
                    Text(stringResource(R.string.chat_history_confirm_delete), color = p.alert)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text(stringResource(R.string.chat_history_cancel)) } }
        )
    }
}

@Composable
private fun HistoryHeader(count: Int, hasItems: Boolean, onBack: () -> Unit, onDeleteAll: () -> Unit) {
    val p = homePalette()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, Brush.linearGradient(listOf(p.accent.copy(alpha = 0.7f), p.borderBright.copy(alpha = 0.3f))), RoundedCornerShape(22.dp))
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.chat_back), tint = p.textPrimary)
        }
        Box(Modifier.size(42.dp).clip(CircleShape).background(p.accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.History, null, tint = p.accent)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.chat_history_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, maxLines = 1)
            Text(
                stringResource(R.string.chat_history_local_note) + if (hasItems) "  -  $count" else "",
                fontSize = 12.sp, color = p.textSecondary, maxLines = 1
            )
        }
        if (hasItems) {
            TextButton(onClick = onDeleteAll) {
                Icon(Icons.Outlined.DeleteSweep, null, tint = p.alert, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(loc("Clear all", "مسح الكل"), color = p.alert, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyHistory(onNewChat: () -> Unit) {
    val p = homePalette()
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        com.example.myapplication.core.ui.RobotFace(modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.chat_history_empty_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.chat_history_empty_body), fontSize = 14.sp, lineHeight = 22.sp, color = p.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onNewChat, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.background)) {
            Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.chat_history_new_chat), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConversationCard(item: ConversationSummary, isCurrent: Boolean, onClick: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    val p = homePalette()
    val shape = RoundedCornerShape(18.dp)
    val dateText = rememberHistoryDateText(item.updatedAt)
    val countText = LocalContext.current.resources.getQuantityString(R.plurals.chat_history_message_count, item.messageCount, item.messageCount)
    val contentStyle = TextStyle(textDirection = TextDirection.Content)
    val title = item.title.trimEnd('…', ' ').ifBlank { stringResource(R.string.chat_history_untitled) }
    val border = if (isCurrent) Brush.linearGradient(listOf(p.accent, p.accent.copy(alpha = 0.4f)))
                 else Brush.linearGradient(listOf(p.borderBright.copy(alpha = 0.6f), p.borderDim.copy(alpha = 0.25f)))

    Row(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(if (isCurrent) 1.5.dp else 1.dp, border, shape)
            .clickable(onClick = onClick).padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).background(p.accent.copy(alpha = 0.14f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = p.accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = contentStyle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, maxLines = 1, overflow = TextOverflow.Clip)
            if (!item.lastMessage.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(item.lastMessage.trim().replace(Regex("\\s+"), " ").take(110), style = contentStyle, fontSize = 13.sp, lineHeight = 19.sp,
                    color = p.textSecondary, maxLines = 2, overflow = TextOverflow.Clip)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$dateText · $countText", fontSize = 11.sp, color = p.textTertiary)
                if (isCurrent) {
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.chat_history_current), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.accent)
                }
            }
        }
        Column {
            IconButton(onClick = onRename, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.Edit, loc("Rename", "تغيير الاسم"), tint = p.textSecondary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.Delete, stringResource(R.string.chat_history_delete), tint = p.textSecondary, modifier = Modifier.size(19.dp))
            }
        }
    }
}
