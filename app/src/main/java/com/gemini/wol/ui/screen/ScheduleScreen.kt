package com.gemini.wol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.wol.data.local.entity.ScheduleEntity
import com.gemini.wol.ui.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateUp: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val pc by viewModel.pc.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ScheduleEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        pc?.name ?: "Schedules", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    scheduleToEdit = null
                    showTimePicker = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No schedules set",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(schedules) { schedule ->
                    com.gemini.wol.ui.component.ScheduleItem(
                        schedule = schedule,
                        onDelete = { viewModel.deleteSchedule(schedule) },
                        onToggle = { viewModel.toggleSchedule(schedule) },
                        onEdit = {
                            scheduleToEdit = schedule
                            showTimePicker = true
                        }
                    )
                }
            }
        }

        if (showTimePicker) {
            AddTimeDialog(
                initialHour = scheduleToEdit?.timeHour,
                initialMinute = scheduleToEdit?.timeMinute,
                initialDaysBitmap = scheduleToEdit?.daysBitmap,
                onDismiss = { showTimePicker = false },
                onConfirm = { h, m, d -> 
                    if (scheduleToEdit != null) {
                        viewModel.updateSchedule(scheduleToEdit!!, h, m, d)
                    } else {
                        viewModel.addSchedule(h, m, d)
                    }
                    showTimePicker = false
                }
            )
        }
    }
}

// ScheduleItem is now in components


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTimeDialog(
    initialHour: Int? = null,
    initialMinute: Int? = null,
    initialDaysBitmap: Int? = null,
    onDismiss: () -> Unit, 
    onConfirm: (Int, Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour ?: 0,
        initialMinute = initialMinute ?: 0
    )
    
    // Default to Mon-Fri (31) if no initial or 0 provided
    val initialBitmap = initialDaysBitmap ?: 31
    val days = remember { 
        val list = androidx.compose.runtime.mutableStateListOf<Boolean>()
        for (i in 0..6) {
            list.add((initialBitmap and (1 shl i)) != 0)
        }
        list
    }
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                var bitmask = 0
                days.forEachIndexed { index, selected ->
                    if (selected) bitmask = bitmask or (1 shl index)
                }
                if (bitmask == 0) bitmask = 1 // Default to Mon if empty
                onConfirm(timePickerState.hour, timePickerState.minute, bitmask)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(if (initialHour != null) "Edit Schedule" else "New Schedule") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Repeat Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 7
                ) {
                    days.forEachIndexed { index, selected ->
                        FilterChip(
                            selected = selected,
                            onClick = { days[index] = !selected },
                            label = { Text(dayLabels[index]) },
                            modifier = Modifier.padding(2.dp),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    )
}
