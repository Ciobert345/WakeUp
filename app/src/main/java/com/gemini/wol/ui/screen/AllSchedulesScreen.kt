package com.gemini.wol.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.gemini.wol.data.local.entity.ScheduleEntity
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.wol.ui.viewmodel.AllSchedulesViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AllSchedulesScreen(
    onNavigateUp: () -> Unit,
    viewModel: AllSchedulesViewModel = hiltViewModel()
) {
    val schedules by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ScheduleEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Active Jobs", 
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
                        text = "No active schedules found",
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
                items(schedules) { item ->
                    com.gemini.wol.ui.component.ScheduleItem(
                        schedule = item.schedule,
                        pcName = item.pcName,
                        onToggle = { viewModel.toggleSchedule(item.schedule) },
                        onDelete = { viewModel.deleteSchedule(item.schedule) },
                        onEdit = {
                            scheduleToEdit = item.schedule
                            showTimePicker = true
                        }
                    )
                }
            }
        }
        
        if (showTimePicker && scheduleToEdit != null) {
            val initialBitmap = scheduleToEdit!!.daysBitmap
            val days = remember(scheduleToEdit) { 
                val list = androidx.compose.runtime.mutableStateListOf<Boolean>()
                for (i in 0..6) {
                    list.add((initialBitmap and (1 shl i)) != 0)
                }
                list
            }
            val timePickerState = rememberTimePickerState(
                initialHour = scheduleToEdit!!.timeHour,
                initialMinute = scheduleToEdit!!.timeMinute
            )
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        var bitmask = 0
                        days.forEachIndexed { index, selected ->
                            if (selected) bitmask = bitmask or (1 shl index)
                        }
                        if (bitmask == 0) bitmask = 1
                        viewModel.updateSchedule(scheduleToEdit!!, timePickerState.hour, timePickerState.minute, bitmask)
                        showTimePicker = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                title = { Text("Edit Schedule for " + scheduleToEdit?.pcId ) }, // Could fetch name but complex refactor
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
    }
}
