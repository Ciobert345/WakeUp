package com.ciobert.wol.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddPcClick: () -> Unit,
    onPcClick: (String) -> Unit,
    onScheduleClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val pcs by viewModel.pcs.collectAsState()
    val wakeResult by viewModel.wakeResult.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    
    var pcToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<PcEntity?>(null) }

    // Show snackbar if wakeResult is not null
    if (wakeResult != null) {
        androidx.compose.runtime.LaunchedEffect(wakeResult) {
            snackbarHostState.showSnackbar(wakeResult!!)
            viewModel.clearMessage()
        }
    }
    
    // Delete Confirmation Dialog
    if (pcToDelete != null) {
        AlertDialog(
            onDismissRequest = { pcToDelete = null },
            title = { Text("Eliminare dispositivo") },
            text = { Text("Sei sicuro di voler eliminare ${pcToDelete?.name}? Questa azione non può essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pcToDelete?.let { viewModel.deletePc(it) }
                        pcToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { pcToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "My Devices", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshPcs() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
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
                onClick = onAddPcClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add PC")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (pcs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No devices found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add your first PC",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = pcs,
                    key = { it.pc.id }
                ) { uiModel ->
                    com.ciobert.wol.ui.component.PcItem(
                        pc = uiModel.pc,
                        isOnline = uiModel.isOnline,
                        onWakeClick = { viewModel.wakePc(uiModel.pc) },
                        onClick = { onPcClick(uiModel.pc.id) },
                        onScheduleClick = { onScheduleClick(uiModel.pc.id) },
                        onDeleteClick = { pcToDelete = uiModel.pc }
                    )
                }
            }
        }
    }
}

