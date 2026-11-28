package com.example.myapplication.presentation.assistant

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * شاشة كاملة (Overlay) من غير الـ Bottom Navbar. جواها شاشتين:
 *  - الشات (الأساسية)
 *  - الـ History
 *
 * زرار الرجوع: من الـ History → للشات، ومن الشات → بيقفل الـ Overlay (onBack)
 * ويرجع المستخدم للتاب اللي كان فيه.
 */
@Composable
fun AssistantFlow(onBack: () -> Unit) {
    val context = LocalContext.current
    val assistantViewModel: AssistantViewModel =
        viewModel(factory = AssistantViewModel.factory(context))
    val historyViewModel: HistoryViewModel =
        viewModel(factory = HistoryViewModel.factory(context))

    var showHistory by rememberSaveable { mutableStateOf(false) }

    // الـ draft هنا (مش جوه الشات) عشان ما يضيعش لما المستخدم يفتح الـ History ويرجع
    var draft by rememberSaveable { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        ShareWithAi.take()?.let { draft = it }
    }

    BackHandler(enabled = showHistory) { showHistory = false }

    AnimatedContent(
        targetState = showHistory,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
        label = "assistantFlow"
    ) { isHistory ->
        if (isHistory) {
            val conversations by historyViewModel.conversations.collectAsStateWithLifecycle()
            val assistantState by assistantViewModel.uiState.collectAsStateWithLifecycle()

            HistoryScreen(
                conversations = conversations,
                currentConversationId = assistantState.conversationId,
                onBack = { showHistory = false },
                onOpen = { id ->
                    assistantViewModel.openConversation(id)
                    showHistory = false
                },
                onNewChat = {
                    assistantViewModel.startNewConversation()
                    showHistory = false
                },
                onDelete = { id ->
                    historyViewModel.delete(id)
                    assistantViewModel.onConversationDeleted(id)
                },
                onDeleteAll = {
                    historyViewModel.deleteAll()
                    assistantViewModel.onAllConversationsDeleted()
                }
            )
        } else {
            AssistantScreen(
                viewModel = assistantViewModel,
                input = draft,
                onInputChange = { draft = it },
                onBack = onBack,
                onOpenHistory = { showHistory = true }
            )
        }
    }
}
