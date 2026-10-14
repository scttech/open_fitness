package com.scttech.android.kotlin.openfitness.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItem
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemStatus
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemType
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun CalendarRoute(
    onStartWorkout: (Long) -> Unit,
    onStartProgramSession: (Long) -> Unit,
    onRecordTest: (Long) -> Unit,
    onAddEvent: (LocalDate) -> Unit,
    onEditEvent: (eventId: Long, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedItem by remember { mutableStateOf<CalendarItem?>(null) }

    CalendarScreen(
        uiState = uiState,
        onModeChange = viewModel::setMode,
        onDateSelect = viewModel::selectDate,
        onStep = viewModel::step,
        onToday = viewModel::goToToday,
        onItemClick = { selectedItem = it },
        onAddEvent = { onAddEvent(uiState.selectedDate) },
        modifier = modifier,
    )

    selectedItem?.let { item ->
        CalendarItemSheet(
            item = item,
            onDismiss = { selectedItem = null },
            onStart = {
                selectedItem = null
                when (item.type) {
                    CalendarItemType.WORKOUT -> item.workoutId?.let(onStartWorkout)
                    CalendarItemType.PROGRAM_SESSION -> item.programId?.let(onStartProgramSession)
                    CalendarItemType.PROGRAM_TEST -> item.programId?.let(onRecordTest)
                    CalendarItemType.CUSTOM -> Unit
                }
            },
            onSetDone = { done ->
                selectedItem = null
                viewModel.setDone(item, done)
            },
            onMove = { newDate ->
                selectedItem = null
                viewModel.moveOccurrence(item, newDate)
            },
            onEdit = {
                selectedItem = null
                item.eventId?.let { onEditEvent(it, item.date) }
            },
            onRemoveOccurrence = {
                selectedItem = null
                viewModel.removeOccurrence(item)
            },
            onDeleteSeries = {
                selectedItem = null
                viewModel.deleteSeries(item)
            },
        )
    }
}

@Composable
internal fun CalendarScreen(
    uiState: CalendarUiState,
    onModeChange: (CalendarMode) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onStep: (Int) -> Unit,
    onToday: () -> Unit,
    onItemClick: (CalendarItem) -> Unit,
    onAddEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                actions = {
                    if (uiState.streak > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("${uiState.streak}-day streak") },
                            leadingIcon = { Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        )
                    }
                    IconButton(onClick = onToday) { Icon(Icons.Filled.Today, contentDescription = "Go to today") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvent) { Icon(Icons.Filled.Add, contentDescription = "Schedule") }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                CalendarMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = uiState.mode == mode,
                        onClick = { onModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, CalendarMode.entries.size),
                    ) { Text(mode.label) }
                }
            }
            PeriodHeader(uiState, onStep)
            if (uiState.isLoading) {
                FullScreenLoading()
            } else {
                var dragTotal by remember { mutableStateOf(0f) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.mode) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragTotal = 0f },
                                onDragEnd = {
                                    if (dragTotal > SWIPE_THRESHOLD) onStep(-1) else if (dragTotal < -SWIPE_THRESHOLD) onStep(1)
                                },
                            ) { _, amount -> dragTotal += amount }
                        },
                ) {
                    when (uiState.mode) {
                        CalendarMode.DAY -> DayView(uiState, onItemClick)
                        CalendarMode.WEEK -> WeekView(uiState, onDateSelect, onModeChange, onItemClick)
                        CalendarMode.MONTH -> MonthView(uiState, onDateSelect, onItemClick)
                    }
                }
            }
        }
    }
}

private const val SWIPE_THRESHOLD = 120f

/** Keeps the last list item scrollable out from under the floating add button. */
private val FAB_CLEARANCE_PADDING = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp)

@Composable
private fun PeriodHeader(uiState: CalendarUiState, onStep: (Int) -> Unit) {
    val title = when (uiState.mode) {
        CalendarMode.DAY -> uiState.selectedDate.format(FormatStyle.FULL)
        CalendarMode.WEEK -> {
            val range = uiState.visibleRange
            "${range.start.formatShort()} – ${range.endInclusive.formatShort()}"
        }
        CalendarMode.MONTH -> uiState.selectedDate.toJavaLocalDate()
            .format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onStep(-1) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous") }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = { onStep(1) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next") }
    }
}

@Composable
private fun DayView(uiState: CalendarUiState, onItemClick: (CalendarItem) -> Unit) {
    val items = uiState.items.filter { it.date == uiState.selectedDate }
    if (items.isEmpty()) {
        EmptyDay("Nothing scheduled. Tap + to add a workout or event.")
    } else {
        LazyColumn(contentPadding = FAB_CLEARANCE_PADDING, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { CalendarItemCard(it, onClick = { onItemClick(it) }) }
        }
    }
}

@Composable
private fun WeekView(
    uiState: CalendarUiState,
    onDateSelect: (LocalDate) -> Unit,
    onModeChange: (CalendarMode) -> Unit,
    onItemClick: (CalendarItem) -> Unit,
) {
    val days = generateSequence(uiState.visibleRange.start) { it.plus(1, DateTimeUnit.DAY) }.take(7).toList()
    val byDate = uiState.itemsByDate
    LazyColumn(contentPadding = FAB_CLEARANCE_PADDING, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { day ->
            val dayItems = byDate[day].orEmpty()
            item(key = "header-$day") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDateSelect(day)
                            onModeChange(CalendarMode.DAY)
                        }
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = day.toJavaLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (day == uiState.today) FontWeight.Bold else FontWeight.Normal,
                        color = if (day == uiState.today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (dayItems.isEmpty()) {
                item(key = "empty-$day") {
                    Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(dayItems, key = { "${day}-${it.eventId}-${it.programId}-${it.type}-${it.title}-${it.startMinutes}" }) {
                    CalendarItemCard(it, onClick = { onItemClick(it) })
                }
            }
        }
    }
}

@Composable
private fun MonthView(
    uiState: CalendarUiState,
    onDateSelect: (LocalDate) -> Unit,
    onItemClick: (CalendarItem) -> Unit,
) {
    val range = uiState.visibleRange
    val byDate = uiState.itemsByDate
    val weeks = generateSequence(range.start) { it.plus(1, DateTimeUnit.WEEK) }.takeWhile { it <= range.endInclusive }.toList()
    val selectedItems = byDate[uiState.selectedDate].orEmpty()
    val currentMonth = uiState.selectedDate.month

    LazyColumn(contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Row(Modifier.fillMaxWidth()) {
                for (offset in 0..6) {
                    Text(
                        text = java.time.DayOfWeek.of(offset + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        items(weeks, key = { it.toString() }) { weekStart ->
            Row(Modifier.fillMaxWidth()) {
                for (offset in 0..6) {
                    val day = weekStart.plus(offset, DateTimeUnit.DAY)
                    MonthCell(
                        day = day,
                        dayItems = byDate[day].orEmpty(),
                        isSelected = day == uiState.selectedDate,
                        isToday = day == uiState.today,
                        inMonth = day.month == currentMonth,
                        onClick = { onDateSelect(day) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(
                uiState.selectedDate.format(FormatStyle.FULL),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        if (selectedItems.isEmpty()) {
            item { Text("Nothing scheduled.", Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(selectedItems, key = { "sel-${it.eventId}-${it.programId}-${it.type}-${it.title}-${it.startMinutes}" }) {
                CalendarItemCard(it, onClick = { onItemClick(it) }, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@Composable
private fun MonthCell(
    day: LocalDate,
    dayItems: List<CalendarItem>,
    isSelected: Boolean,
    isToday: Boolean,
    inMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .background(
                color = if (isSelected) colors.primaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isToday -> colors.primary
                inMonth -> colors.onSurface
                else -> colors.onSurfaceVariant.copy(alpha = 0.5f)
            },
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dayItems.take(3).forEach { item ->
                Box(Modifier.size(6.dp).background(item.status.color(), CircleShape))
            }
        }
        if (dayItems.size > 3) {
            Text("+${dayItems.size - 3}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarItemStatus.color(): Color = when (this) {
    CalendarItemStatus.DONE -> MaterialTheme.colorScheme.primary
    CalendarItemStatus.MISSED -> MaterialTheme.colorScheme.error
    CalendarItemStatus.UPCOMING -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun CalendarItemCard(item: CalendarItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(40.dp).background(item.status.color(), MaterialTheme.shapes.extraSmall))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.startMinutes?.let {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatMinutes(it), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.isRecurring) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.Repeat, contentDescription = "Repeats", modifier = Modifier.size(14.dp))
                    }
                }
            }
            when (item.status) {
                CalendarItemStatus.DONE -> Icon(Icons.Filled.CheckCircle, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                CalendarItemStatus.MISSED -> Icon(Icons.Filled.Error, contentDescription = "Missed", tint = MaterialTheme.colorScheme.error)
                CalendarItemStatus.UPCOMING -> Unit
            }
        }
    }
}

@Composable
private fun EmptyDay(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarItemSheet(
    item: CalendarItem,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onSetDone: (Boolean) -> Unit,
    onMove: (LocalDate) -> Unit,
    onEdit: () -> Unit,
    onRemoveOccurrence: () -> Unit,
    onDeleteSeries: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    var confirmDeleteSeries by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            ListItem(
                headlineContent = { Text(item.title, style = MaterialTheme.typography.titleLarge) },
                supportingContent = {
                    Column {
                        Text(
                            buildString {
                                append(item.date.format(FormatStyle.MEDIUM))
                                item.startMinutes?.let { append(" · ${formatMinutes(it)}") }
                            },
                        )
                        if (item.notes.isNotBlank()) Text(item.notes)
                    }
                },
            )
            val canStart = !item.isDone && when (item.type) {
                CalendarItemType.WORKOUT -> item.workoutId != null
                CalendarItemType.PROGRAM_SESSION, CalendarItemType.PROGRAM_TEST -> item.programId != null
                CalendarItemType.CUSTOM -> false
            }
            if (canStart) {
                SheetAction(
                    when (item.type) {
                        CalendarItemType.PROGRAM_TEST -> "Record test"
                        else -> "Start now"
                    },
                    onStart,
                )
            }
            if (item.eventId != null) {
                SheetAction(if (item.isDone) "Mark as not done" else "Mark as done") { onSetDone(!item.isDone) }
            }
            if (!item.isDone) {
                SheetAction(if (item.status == CalendarItemStatus.MISSED) "Reschedule missed session" else "Move to another day") {
                    showDatePicker = true
                }
            }
            if (item.eventId != null) SheetAction("Edit", onEdit)
            SheetAction(if (item.isRecurring) "Remove this occurrence" else "Remove") { confirmRemove = true }
            if (item.isRecurring) SheetAction("Delete entire series") { confirmDeleteSeries = true }
        }
    }

    if (showDatePicker) {
        MoveDatePickerDialog(
            initial = item.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                onMove(it)
            },
        )
    }
    if (confirmRemove) {
        ConfirmDialog(
            title = "Remove from calendar?",
            text = if (item.eventId == null) "This program day will be hidden from the calendar. Your program isn't changed." else "This can't be undone.",
            confirmLabel = "Remove",
            onConfirm = {
                confirmRemove = false
                onRemoveOccurrence()
            },
            onDismiss = { confirmRemove = false },
        )
    }
    if (confirmDeleteSeries) {
        ConfirmDialog(
            title = "Delete series?",
            text = "Every occurrence of this repeating event will be removed.",
            confirmLabel = "Delete",
            onConfirm = {
                confirmDeleteSeries = false
                onDeleteSeries()
            },
            onDismiss = { confirmDeleteSeries = false },
        )
    }
}

@Composable
private fun SheetAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Text(label, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveDatePickerDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onConfirm(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date) } },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) { DatePicker(state = state) }
}

internal fun formatMinutes(minutes: Int): String {
    val hour = minutes / 60
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(displayHour, minutes % 60, if (hour < 12) "AM" else "PM")
}

private fun LocalDate.format(style: FormatStyle): String =
    toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(style).withLocale(Locale.getDefault()))

private fun LocalDate.formatShort(): String =
    toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
