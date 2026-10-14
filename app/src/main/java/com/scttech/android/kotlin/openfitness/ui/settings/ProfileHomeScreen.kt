package com.scttech.android.kotlin.openfitness.ui.settings

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.ui.common.AvatarPickerDialog
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.ProfileAvatar
import com.scttech.android.kotlin.openfitness.ui.common.TextInputDialog
import com.scttech.android.kotlin.openfitness.ui.messages.AppAlertsViewModel
import kotlinx.coroutines.launch

/** The Profile tab: who's signed in, plus shortcuts to messages, settings and history. */
@Composable
internal fun ProfileHomeRoute(
    onSwitchProfile: () -> Unit,
    onMessagesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onWeightClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBadgesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    alertsViewModel: AppAlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by alertsViewModel.unreadCount.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Profile") }) },
    ) { padding ->
        when (val state = uiState) {
            SettingsUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is SettingsUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProfileAvatar(profile = state.currentProfile, size = 40.dp)
                            Spacer(Modifier.width(16.dp))
                            Text(state.currentProfile.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAvatarPicker = true }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Change avatar")
                            }
                            IconButton(onClick = { showRenameDialog = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename profile")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete profile")
                            }
                        }
                    }
                }
                item {
                    SettingsRow(
                        icon = Icons.Filled.SwitchAccount,
                        label = "Switch profile",
                        onClick = {
                            coroutineScope.launch {
                                viewModel.switchProfile()
                                onSwitchProfile()
                            }
                        },
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                item { SettingsRow(icon = Icons.Filled.Notifications, label = "Message Center", badgeCount = unreadCount, onClick = onMessagesClick) }
                item { SettingsRow(icon = Icons.Filled.Settings, label = "Settings", onClick = onSettingsClick) }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                item { Text("Data & History", style = MaterialTheme.typography.titleLarge) }
                item { SettingsRow(icon = Icons.Filled.History, label = "History", onClick = onHistoryClick) }
                item { SettingsRow(icon = Icons.Filled.MonitorWeight, label = "Body Weight", onClick = onWeightClick) }
                item { SettingsRow(icon = Icons.Filled.Assessment, label = "Stats", onClick = onStatsClick) }
                item { SettingsRow(icon = Icons.Filled.WorkspacePremium, label = "Badges", onClick = onBadgesClick) }
            }
        }
    }

    (uiState as? SettingsUiState.Success)?.let { state ->
        if (showRenameDialog) {
            TextInputDialog(
                title = "Rename profile",
                initialValue = state.currentProfile.name,
                confirmLabel = "Rename",
                onConfirm = {
                    viewModel.renameProfile(state.currentProfile, it)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
        if (showDeleteDialog) {
            ConfirmDialog(
                title = "Delete profile?",
                text = "This permanently deletes \"${state.currentProfile.name}\" and all of its workouts, programs, history, and weight entries.",
                confirmLabel = "Delete",
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.deleteProfile(state.currentProfile)
                    onSwitchProfile()
                },
                onDismiss = { showDeleteDialog = false },
            )
        }
        if (showAvatarPicker) {
            AvatarPickerDialog(
                onPresetPicked = { viewModel.updateAvatarPreset(state.currentProfile, it) },
                onImagePicked = { viewModel.updateAvatarFromUri(state.currentProfile, it) },
                onCreateCaptureUri = viewModel::createAvatarCaptureUri,
                onDismiss = { showAvatarPicker = false },
            )
        }
    }
}

@Composable
internal fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgedBox(badge = { if (badgeCount > 0) Badge { Text(badgeCount.toString()) } }) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
