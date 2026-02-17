package com.ciobert.wol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ciobert.wol.ui.viewmodel.AddEditPcEvent
import com.ciobert.wol.ui.viewmodel.AddEditPcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPcScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditPcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (uiState.name.isBlank()) "Add PC" else "Edit PC", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Basic Info
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

                    Divider()

                    // Internal Connection
                    Text("Internal Connection (LAN)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.internalIp,
                            onValueChange = { viewModel.onEvent(AddEditPcEvent.InternalIpChanged(it)) },
                            label = { Text("Internal IP") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.internalPort,
                            onValueChange = { viewModel.onEvent(AddEditPcEvent.InternalPortChanged(it)) },
                            label = { Text("Port") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }

                    // External Connection
                    Text("External Connection (WAN)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.externalIp,
                            onValueChange = { viewModel.onEvent(AddEditPcEvent.ExternalIpChanged(it)) },
                            label = { Text("External IP / Host") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.externalPort,
                            onValueChange = { viewModel.onEvent(AddEditPcEvent.ExternalPortChanged(it)) },
                            label = { Text("Port") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }

                    Divider()

                    // Advanced / Status
                    Text("Advanced Settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = uiState.statusPort,
                        onValueChange = { viewModel.onEvent(AddEditPcEvent.StatusPortChanged(it)) },
                        label = { Text("Status Check Port") },
                        supportingText = { Text("TCP Port to check if device is online (e.g. 80, 3389)") },
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
                                "Route packet through WakeOnLan Relay Server (Optional)", 
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
