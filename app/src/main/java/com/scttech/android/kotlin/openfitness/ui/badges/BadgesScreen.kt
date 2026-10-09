package com.scttech.android.kotlin.openfitness.ui.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.BadgeCategory
import com.scttech.android.kotlin.openfitness.domain.model.EarnedBadge
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun BadgesRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BadgesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BadgesScreen(uiState = uiState, onBackClick = onBackClick, modifier = modifier)
}

@Composable
internal fun BadgesScreen(
    uiState: BadgesUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Badges") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            BadgesUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is BadgesUiState.Success -> {
                val earnedAt = uiState.earned.associateBy { it.badge }
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text(
                            "${earnedAt.size} of ${Badge.entries.size} earned",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BadgeCategory.entries.forEach { category ->
                        item(key = category.name) {
                            Text(
                                category.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        items(Badge.entries.filter { it.category == category }, key = { it.name }) { badge ->
                            BadgeRow(badge = badge, earned = earnedAt[badge])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeRow(badge: Badge, earned: EarnedBadge?, modifier: Modifier = Modifier) {
    val isEarned = earned != null
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = if (isEarned) badge.category.icon() else Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline,
            )
            Column {
                Text(
                    badge.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (earned != null) {
                    Text(
                        "Earned ${earned.earnedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

private fun BadgeCategory.icon(): ImageVector = when (this) {
    BadgeCategory.WORKOUTS -> Icons.Filled.FitnessCenter
    BadgeCategory.PROGRAMS -> Icons.Filled.EmojiEvents
    BadgeCategory.BODY -> Icons.Filled.MonitorWeight
    BadgeCategory.STREAKS -> Icons.Filled.LocalFireDepartment
}
