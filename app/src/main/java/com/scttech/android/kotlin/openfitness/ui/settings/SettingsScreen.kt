package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SwitchAccount
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.TextInputDialog
import com.scttech.android.kotlin.openfitness.ui.theme.ProfileAccentColors
import kotlinx.coroutines.launch

@Composable
internal fun SettingsRoute(
    onSwitchProfile: () -> Unit,
    onHistoryClick: () -> Unit,
    onWeightClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    SettingsScreen(
        uiState = uiState,
        onRenameProfile = viewModel::renameProfile,
        onDeleteProfile = { profile ->
            viewModel.deleteProfile(profile)
            onSwitchProfile()
        },
        onSwitchProfile = {
            coroutineScope.launch {
                viewModel.switchProfile()
                onSwitchProfile()
            }
        },
        onHistoryClick = onHistoryClick,
        onWeightClick = onWeightClick,
        onStatsClick = onStatsClick,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onRenameProfile: (Profile, String) -> Unit,
    onDeleteProfile: (Profile) -> Unit,
    onSwitchProfile: () -> Unit,
    onHistoryClick: () -> Unit,
    onWeightClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        when (uiState) {
            SettingsUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is SettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Text("Profile", style = MaterialTheme.typography.titleLarge) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = ProfileAccentColors[uiState.currentProfile.colorIndex % ProfileAccentColors.size],
                                            shape = CircleShape,
                                        ),
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(uiState.currentProfile.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                IconButton(onClick = { showRenameDialog = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Rename profile")
                                }
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete profile")
                                }
                            }
                        }
                    }
                    item { SettingsRow(icon = Icons.Filled.SwitchAccount, label = "Switch profile", onClick = onSwitchProfile) }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                    item { Text("Data & History", style = MaterialTheme.typography.titleLarge) }
                    item { SettingsRow(icon = Icons.Filled.History, label = "History", onClick = onHistoryClick) }
                    item { SettingsRow(icon = Icons.Filled.MonitorWeight, label = "Body Weight", onClick = onWeightClick) }
                    item { SettingsRow(icon = Icons.Filled.Assessment, label = "Stats", onClick = onStatsClick) }
                }
            }
        }
    }

    if (uiState is SettingsUiState.Success) {
        if (showRenameDialog) {
            TextInputDialog(
                title = "Rename profile",
                initialValue = uiState.currentProfile.name,
                confirmLabel = "Rename",
                onConfirm = {
                    onRenameProfile(uiState.currentProfile, it)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
        if (showDeleteDialog) {
            ConfirmDialog(
                title = "Delete profile?",
                text = "This permanently deletes \"${uiState.currentProfile.name}\" and all of its workouts, programs, history, and weight entries.",
                confirmLabel = "Delete",
                onConfirm = {
                    showDeleteDialog = false
                    onDeleteProfile(uiState.currentProfile)
                },
                onDismiss = { showDeleteDialog = false },
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
