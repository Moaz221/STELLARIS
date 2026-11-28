package com.example.myapplication.presentation.assistant

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.local.chat.ChatDatabase
import com.example.myapplication.data.local.chat.ChatHistoryRepository
import com.example.myapplication.data.local.chat.ConversationSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val history: ChatHistoryRepository
) : ViewModel() {

    /** null = لسه بيتحمّل، قائمة فاضية = مفيش محادثات. */
    val conversations: StateFlow<List<ConversationSummary>?> =
        history.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                history.deleteConversation(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete conversation $id", e)
            }
        }
    }

    fun rename(id: String, title: String) {
        viewModelScope.launch {
            try { history.rename(id, title) } catch (e: CancellationException) { throw e } catch (e: Exception) { Log.e(TAG, "rename failed", e) }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try {
                history.deleteAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete all conversations", e)
            }
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    HistoryViewModel(ChatHistoryRepository(ChatDatabase.get(appContext)))
                }
            }
        }
    }
}
