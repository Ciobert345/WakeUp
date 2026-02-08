package com.gemini.wol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.wol.data.repository.AppTheme
import com.gemini.wol.ui.component.SettingsItem
import com.gemini.wol.ui.viewmodel.SettingsViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import com.gemini.wol.ui.component.ColorPickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.OutputStreamWriter
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = accentColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { 
                viewModel.setAccentColor(it)
                showColorPicker = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { 
                viewModel.setTheme(it)
                showThemeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Face,
                            title = "App Theme",
                            subtitle = when(currentTheme) {
                                AppTheme.SYSTEM -> "System default"
                                AppTheme.DARK -> "Dark mode"
                                AppTheme.AMOLED -> "AMOLED pure black"
                            },
                            onClick = { showThemeDialog = true }
                        )
                        SettingsItem(
                            icon = Icons.Default.Palette,
                            title = "Accent Color",
                            subtitle = "Primary brand color",
                            onClick = { showColorPicker = true },
                            trailingContent = {
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = Color(accentColor),
                                    border = BorderStroke(2.dp, Color.White)
                                ) {}
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            kotlinx.coroutines.GlobalScope.launch { // Using GlobalScope for quick fix, consider proper scope in real app
                try {
                    val json = viewModel.exportDevices()
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        OutputStreamWriter(out).use { writer ->
                            writer.write(json)
                        }
                    }
                    snackbarHostState.showSnackbar("Export successful")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Export failed")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val json = inputStream?.bufferedReader().use { reader -> reader?.readText() }
                    if (json != null) {
                        val success = viewModel.importDevices(json)
                        if (success) {
                            snackbarHostState.showSnackbar("Import successful")
                        } else {
                            snackbarHostState.showSnackbar("Invalid file format")
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Import failed")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Data & Backup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Download,
                            title = "Export Devices",
                            subtitle = "Backup your data to JSON",
                            onClick = { exportLauncher.launch("wakeup_backup.json") }
                        )
                        SettingsItem(
                            icon = Icons.Default.Upload,
                            title = "Import Devices",
                            subtitle = "Restore devices from file",
                            onClick = { importLauncher.launch("application/json") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StabilitySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val accentColor by viewModel.accentColor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Stability") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Perché l'app viene chiusa?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Android e i produttori (Xiaomi, Samsung, etc.) chiudono forzatamente le app in background per risparmiare batteria. Se \"pulisci\" le app recenti, lo scheduling potrebbe interrompersi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    "SOLUZIONI CONSIGLIATE",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BulletPoint("Attiva l'esenzione batteria dagli avvisi nella Home.")
                        BulletPoint("Blocca l'app nel menu 'Recent Apps' (icona lucchetto).")
                        BulletPoint("Abilita 'Avvio Automatico' nelle impostazioni di sistema se richiesto.")
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        
                        Text(
                            "ATTENZIONE: Se usi 'Interruzione Forzata' dalle impostazioni di Android, il sistema bloccherà ogni operazione futura fino a quando non riapri l'app manualmente.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                ThemeRadioItem("System Default", AppTheme.SYSTEM, currentTheme, onThemeSelected)
                ThemeRadioItem("Dark Mode", AppTheme.DARK, currentTheme, onThemeSelected)
                ThemeRadioItem("AMOLED Black", AppTheme.AMOLED, currentTheme, onThemeSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeRadioItem(
    label: String,
    theme: AppTheme,
    currentTheme: AppTheme,
    onSelect: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(theme) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = theme == currentTheme,
            onClick = null // Handled by Row click
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
