package com.scttech.android.kotlin.openfitness.ui.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
internal fun ProfileRoute(
    onProfileSelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ProfileUiState.Loaded && state.currentProfileId != null) {
            onProfileSelected()
        }
    }

    ProfileScreen(
        uiState = uiState,
        onCreateProfile = viewModel::createAndSelectProfile,
        onSelectProfile = viewModel::selectProfile,
        onRenameProfile = viewModel::renameProfile,
        onDeleteProfile = viewModel::deleteProfile,
        modifier = modifier,
    )
}

@Composable
internal fun ProfileScreen(
    uiState: ProfileUiState,
    onCreateProfile: (String) -> Unit,
    onSelectProfile: (Profile) -> Unit,
    onRenameProfile: (Profile, String) -> Unit,
    onDeleteProfile: (Profile) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var profileToRename by remember { mutableStateOf<Profile?>(null) }
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New profile") },
            )
        },
    ) { padding ->
        when (uiState) {
            ProfileUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ProfileUiState.Loaded -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Spacer(Modifier.height(24.dp))
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally).size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Open Fitness",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                    )
                    Text(
                        text = "Who's training today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 16.dp),
                    )
                    if (uiState.profiles.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No profiles yet. Tap \"New profile\" to get started.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.profiles, key = { it.id }) { profile ->
                                ProfileCard(
                                    profile = profile,
                                    onClick = { onSelectProfile(profile) },
                                    onRenameClick = { profileToRename = profile },
                                    onDeleteClick = { profileToDelete = profile },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TextInputDialog(
            title = "New profile",
            label = "Profile name",
            confirmLabel = "Create",
            onConfirm = {
                onCreateProfile(it)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    profileToRename?.let { profile ->
        TextInputDialog(
            title = "Rename profile",
            initialValue = profile.name,
            confirmLabel = "Rename",
            onConfirm = {
                onRenameProfile(profile, it)
                profileToRename = null
            },
            onDismiss = { profileToRename = null },
        )
    }

    profileToDelete?.let { profile ->
        ConfirmDialog(
            title = "Delete profile?",
            text = "This permanently deletes \"${profile.name}\" and all of its workouts, history, and weight entries.",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteProfile(profile)
                profileToDelete = null
            },
            onDismiss = { profileToDelete = null },
        )
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = ProfileAccentColors[profile.colorIndex % ProfileAccentColors.size],
                        shape = CircleShape,
                    ),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRenameClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename ${profile.name}")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${profile.name}")
            }
        }
    }
}
