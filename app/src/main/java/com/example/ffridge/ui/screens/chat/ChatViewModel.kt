package com.example.ffridge.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ffridge.data.model.ChatMessage
import com.example.ffridge.data.model.MessageRole
import com.example.ffridge.data.remote.GeminiService
import com.example.ffridge.data.repository.ChatRepository
import com.example.ffridge.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val chatRepository: ChatRepository = RepositoryProvider.getChatRepository()
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            chatRepository.getAllMessages()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load messages: ${e.message}"
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, error = null) }
    }

    fun sendMessage() {
        val messageText = _uiState.value.inputText.trim()
        if (messageText.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isSending = true,
                        inputText = "",
                        error = null
                    )
                }

                // Save user message
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = messageText,
                    role = MessageRole.USER,
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.insertMessage(userMessage)

                // Get AI response
                val response = geminiService.sendMessage(
                    message = messageText
                )

                // Save AI response
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response,
                    role = MessageRole.MODEL,
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.insertMessage(aiMessage)

                _uiState.update { it.copy(isSending = false, error = null) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Failed to send message"
                    )
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            try {
                chatRepository.deleteAllMessages()
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to clear chat: ${e.message}")
                }
            }
        }
    }
}
