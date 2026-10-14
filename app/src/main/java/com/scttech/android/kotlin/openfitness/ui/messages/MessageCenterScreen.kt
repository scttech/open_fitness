package com.scttech.android.kotlin.openfitness.ui.messages

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.data.repository.MessageRepository
import com.scttech.android.kotlin.openfitness.domain.model.AppMessage
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun MessageCenterRoute(
    onBackClick: () -> Unit,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessageCenterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }
    val messages = (uiState as? MessageCenterUiState.Success)?.messages.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Message Center") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (messages.any { !it.isRead }) {
                        IconButton(onClick = viewModel::markAllRead) { Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read") }
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) { Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear all") }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            MessageCenterUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is MessageCenterUiState.Success -> if (messages.isEmpty()) {
                EmptyState(
                    message = "No recent alerts. Alerts are kept for ${MessageRepository.RETENTION_HOURS} hours.",
                    modifier = Modifier.padding(padding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageCard(
                            message = message,
                            onClick = { viewModel.open(message, onNavigate) },
                            onDelete = { viewModel.delete(message) },
                        )
                    }
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Clear all messages?",
            text = "This removes every alert from the Message Center.",
            confirmLabel = "Clear",
            onConfirm = {
                confirmClear = false
                viewModel.clearAll()
            },
            onDismiss = { confirmClear = false },
        )
    }
}

@Composable
private fun MessageCard(message: AppMessage, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!message.isRead) {
                        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        message.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Bold,
                    )
                }
                Text(message.body, style = MaterialTheme.typography.bodyMedium)
                Text(
                    DateUtils.getRelativeTimeSpanString(
                        message.createdAt.toEpochMilliseconds(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Dismiss") }
        }
    }
}
