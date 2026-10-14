package com.scttech.android.kotlin.openfitness.ui.messages

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.MessageRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.notification.AlertExtras
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A tapped alert waiting for the app shell to navigate to it. */
data class AlertLink(
    val profileId: Long,
    val destinationType: AlertDestinationType?,
    val destinationId: Long?,
    val messageId: Long?,
)

/** App-shell state for alerts: the unread count behind the badges, and a tapped notification's pending destination. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppAlertsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val messageRepository: MessageRepository,
    private val routeResolver: AlertRouteResolver,
) : ViewModel() {

    val unreadCount: StateFlow<Int> = profileRepository.currentProfileId
        .flatMapLatest { id -> if (id == null) flowOf(0) else messageRepository.observeUnreadCount(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _pendingLink = MutableStateFlow<AlertLink?>(null)
    val pendingLink: StateFlow<AlertLink?> = _pendingLink

    init {
        viewModelScope.launch { messageRepository.purgeExpired() }
    }

    /** Reads an alert notification's extras, if [intent] came from one. */
    fun onIntent(intent: Intent?) {
        val profileId = intent?.getLongExtra(AlertExtras.PROFILE_ID, -1L)?.takeIf { it != -1L } ?: return
        _pendingLink.value = AlertLink(
            profileId = profileId,
            destinationType = intent.getStringExtra(AlertExtras.DESTINATION_TYPE)
                ?.let { name -> AlertDestinationType.entries.firstOrNull { it.name == name } },
            destinationId = intent.getLongExtra(AlertExtras.DESTINATION_ID, -1L).takeIf { it != -1L },
            messageId = intent.getLongExtra(AlertExtras.MESSAGE_ID, -1L).takeIf { it != -1L },
        )
    }

    /** Switches to the alert's profile if needed, marks it read, and returns the route to open. */
    suspend fun openPendingLink(): Any? {
        val link = _pendingLink.value ?: return null
        _pendingLink.value = null
        if (profileRepository.currentProfileId.first() != link.profileId) {
            profileRepository.setCurrentProfile(link.profileId)
        }
        link.messageId?.let { messageRepository.markRead(it) }
        return routeResolver.resolve(link.destinationType, link.destinationId)
    }
}
