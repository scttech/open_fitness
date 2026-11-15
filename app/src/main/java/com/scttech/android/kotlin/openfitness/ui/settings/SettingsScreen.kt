package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.MAX_TIMER_COUNTDOWN_SECONDS
import com.scttech.android.kotlin.openfitness.domain.model.MIN_TIMER_COUNTDOWN_SECONDS
import com.scttech.android.kotlin.openfitness.domain.model.SkinTone
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import com.scttech.android.kotlin.openfitness.domain.model.WeightUnit
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.theme.TimerPaletteColors

/** Weight unit, appearance and workout-timer preferences, reached from the Profile tab. */
@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTimerSoundEnabledChange = viewModel::setTimerSoundEnabled,
        onTimerCountdownSecondsChange = viewModel::setTimerCountdownSeconds,
        onTimerWorkColorChange = viewModel::setTimerWorkColor,
        onTimerRestColorChange = viewModel::setTimerRestColor,
        onThemeModeChange = viewModel::setThemeMode,
        onSkinToneChange = viewModel::setSkinTone,
        onWeightUnitChange = viewModel::setWeightUnit,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onTimerSoundEnabledChange: (Boolean) -> Unit,
    onTimerCountdownSecondsChange: (Int) -> Unit,
    onTimerWorkColorChange: (Color) -> Unit,
    onTimerRestColorChange: (Color) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSkinToneChange: (SkinTone) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            SettingsUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is SettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Text("Weight unit", style = MaterialTheme.typography.titleLarge) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    WeightUnit.entries.forEachIndexed { index, unit ->
                                        SegmentedButton(
                                            selected = uiState.currentProfile.weightUnit == unit,
                                            onClick = { onWeightUnitChange(unit) },
                                            shape = SegmentedButtonDefaults.itemShape(index, WeightUnit.entries.size),
                                        ) {
                                            Text("${unit.displayName} (${unit.label})")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                    item { Text("Appearance", style = MaterialTheme.typography.titleLarge) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Theme", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    ThemeMode.entries.forEachIndexed { index, mode ->
                                        SegmentedButton(
                                            selected = uiState.themeMode == mode,
                                            onClick = { onThemeModeChange(mode) },
                                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                                        ) {
                                            Text(mode.label())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Exercise model skin tone", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    uiState.skinTone.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                SkinToneRow(selected = uiState.skinTone, onSelect = onSkinToneChange)
                            }
                        }
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                    item { Text("Timer", style = MaterialTheme.typography.titleLarge) }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Countdown sounds",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(checked = uiState.timerSoundEnabled, onCheckedChange = onTimerSoundEnabledChange)
                            }
                        }
                    }
                    if (uiState.timerSoundEnabled) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                var seconds by remember(uiState.timerCountdownSeconds) {
                                    mutableFloatStateOf(uiState.timerCountdownSeconds.toFloat())
                                }
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Start countdown sounds", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Ticks play during the last ${seconds.roundToInt()} seconds of each countdown.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Slider(
                                        value = seconds,
                                        onValueChange = { seconds = it },
                                        onValueChangeFinished = { onTimerCountdownSecondsChange(seconds.roundToInt()) },
                                        valueRange = MIN_TIMER_COUNTDOWN_SECONDS.toFloat()..MAX_TIMER_COUNTDOWN_SECONDS.toFloat(),
                                        steps = MAX_TIMER_COUNTDOWN_SECONDS - MIN_TIMER_COUNTDOWN_SECONDS - 1,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Work color", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                ColorSwatchRow(selected = uiState.timerWorkColor, onSelect = onTimerWorkColorChange)
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rest color", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                ColorSwatchRow(selected = uiState.timerRestColor, onSelect = onTimerRestColorChange)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@Composable
private fun ColorSwatchRow(
    selected: Color,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TimerPaletteColors.forEach { color ->
            val isSelected = color == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onSelect(color) }
                    .background(color = color, shape = CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun SkinToneRow(
    selected: SkinTone,
    onSelect: (SkinTone) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkinTone.entries.forEach { tone ->
            val isSelected = tone == selected
            val color = Color(tone.swatchArgb)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClickLabel = tone.label) { onSelect(tone) }
                    .background(color = color, shape = CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "${tone.label}, selected",
                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
