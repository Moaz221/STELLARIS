package com.example.myapplication.presentation.assistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myapplication.R
import com.example.myapplication.data.local.chat.ConversationSummary
import com.example.myapplication.presentation.home.homePalette

/**
 * @param conversations null = لسه بيتحمّل.
 * @param currentConversationId المحادثة المفتوحة دلوقتي في الشات (بتتعلّم بـ "الحالية").
 */
@Composable
fun HistoryScreen(
    conversations: List<ConversationSummary>?,
    currentConversationId: String?,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onNewChat: () -> Unit,
    onDelete: (String) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = homePalette()
    val colors = MaterialTheme.colorScheme

    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }

    Box(
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
    ) {
        Column(Modifier.fillMaxSize()) {
            HistoryHeader(
                hasItems = !conversations.isNullOrEmpty(),
                onBack = onBack,
                onDeleteAll = { confirmDeleteAll = true }
            )

            when {
                conversations == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    }
                }

                conversations.isEmpty() -> EmptyHistory()

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // مساحة تحت عشان الـ FAB ما يغطيش آخر عنصر
                        contentPadding = PaddingValues(
                            start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items = conversations, key = { it.id }) { item ->
                            ConversationCard(
                                item = item,
                                isCurrent = item.id == currentConversationId,
                                onClick = { onOpen(item.id) },
                                onDelete = { pendingDeleteId = item.id }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNewChat,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.chat_history_new_chat)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.chat_history_delete_title)) },
            text = { Text(stringResource(R.string.chat_history_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(id)
                    pendingDeleteId = null
                }) {
                    Text(
                        text = stringResource(R.string.chat_history_confirm_delete),
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.chat_history_cancel))
                }
            }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.chat_history_delete_all_title)) },
            text = { Text(stringResource(R.string.chat_history_delete_all_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAll()
                    confirmDeleteAll = false
                }) {
                    Text(
                        text = stringResource(R.string.chat_history_confirm_delete),
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.chat_history_cancel))
                }
            }
        )
    }
}

@Composable
private fun HistoryHeader(
    hasItems: Boolean,
    onBack: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface.copy(alpha = 0.95f),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f))
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.chat_history_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.chat_history_local_note),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (hasItems) {
                IconButton(onClick = onDeleteAll) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.chat_history_delete_all),
                        tint = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.chat_history_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.chat_history_empty_body),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConversationCard(
    item: ConversationSummary,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val dateText = rememberHistoryDateText(item.updatedAt)
    val countText = LocalContext.current.resources.getQuantityString(
        R.plurals.chat_history_message_count,
        item.messageCount,
        item.messageCount
    )
    val contentStyle = TextStyle(textDirection = TextDirection.Content)

    Surface(
        shape = shape,
        color = colors.surface,
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = if (isCurrent) colors.primary else colors.outlineVariant
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.ifBlank { stringResource(R.string.chat_history_untitled) },
                    style = contentStyle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!item.lastMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.lastMessage,
                        style = contentStyle,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateText · $countText",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.chat_history_current),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.chat_history_delete),
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}
