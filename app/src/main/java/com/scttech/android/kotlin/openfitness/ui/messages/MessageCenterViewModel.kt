package com.scttech.android.kotlin.openfitness.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.MessageRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.AppMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MessageCenterUiState {
    data object Loading : MessageCenterUiState
    data class Success(val messages: List<AppMessage>) : MessageCenterUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessageCenterViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository,
    private val routeResolver: AlertRouteResolver,
) : ViewModel() {

    val uiState: StateFlow<MessageCenterUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { messageRepository.observeMessages(it) }
        .map<List<AppMessage>, MessageCenterUiState> { MessageCenterUiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MessageCenterUiState.Loading)

    init {
        viewModelScope.launch { messageRepository.purgeExpired() }
    }

    /** Marks [message] read and hands back the screen its alert was about. */
    fun open(message: AppMessage, onRoute: (Any) -> Unit) {
        viewModelScope.launch {
            messageRepository.markRead(message.id)
            onRoute(routeResolver.resolve(message.destinationType, message.destinationId))
        }
    }

    fun markAllRead() = withProfile { messageRepository.markAllRead(it) }

    fun clearAll() = withProfile { messageRepository.clear(it) }

    fun delete(message: AppMessage) {
        viewModelScope.launch { messageRepository.delete(message.id) }
    }

    private fun withProfile(block: suspend (Long) -> Unit) {
        viewModelScope.launch { block(profileRepository.currentProfileId.filterNotNull().first()) }
    }
}
