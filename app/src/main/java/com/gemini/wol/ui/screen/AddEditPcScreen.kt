package com.gemini.wol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.wol.ui.viewmodel.AddEditPcEvent
import com.gemini.wol.ui.viewmodel.AddEditPcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPcScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditPcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.name.isBlank()) "Add PC" else "Edit PC", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AddEditPcEvent.Save) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onEvent(AddEditPcEvent.NameChanged(it)) },
                        label = { Text("Device Name") },
                        isError = uiState.nameError != null,
                        supportingText = { uiState.nameError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.mac,
                        onValueChange = { viewModel.onEvent(AddEditPcEvent.MacChanged(it)) },
                        label = { Text("MAC Address") },
                        placeholder = { Text("00:11:22:33:44:55") },
                        isError = uiState.macError != null,
                        supportingText = { uiState.macError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.broadcast,
                        onValueChange = { viewModel.onEvent(AddEditPcEvent.BroadcastChanged(it)) },
                        label = { Text("IP Address / Hostname") },
                        supportingText = { Text("Used for Status Check & Unicast WoL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.port,
                        onValueChange = { viewModel.onEvent(AddEditPcEvent.PortChanged(it)) },
                        label = { Text("WoL Port") },
                        isError = uiState.portError != null,
                        supportingText = { uiState.portError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = uiState.isRelay,
                            onCheckedChange = { viewModel.onEvent(AddEditPcEvent.RelayChanged(it)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Use Secure Relay", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Route packet through WakeOnLan Relay Server", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
