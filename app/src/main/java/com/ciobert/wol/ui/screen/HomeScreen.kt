package com.ciobert.wol.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciobert.wol.ui.viewmodel.HomeViewModel

import com.ciobert.wol.ui.component.getContrastColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDevices: () -> Unit,
    onAddPcClick: () -> Unit,
    onShowAllSchedules: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: com.ciobert.wol.ui.viewmodel.SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentColor by settingsViewModel.accentColor.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    // Launcher for Notification Permission
    val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { _ -> viewModel.refreshPermissions() }
    )

    // Refresh permissions when app comes to foreground
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val hubColor = Color(accentColor)
    val hubContentColor = getContrastColor(hubColor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Superior Elite Header
        com.ciobert.wol.ui.component.GradientHeader(
            title = "Superior Hub",
            subtitle = "Network Interface \u2022 Remote Console Active",
            accentColor = hubColor,
            isDark = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Permission Warnings
            PermissionWarningSection(
                missingPermissions = uiState.missingPermissions,
                onPermissionAction = { id, action ->
                    if (id == "notifications") {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        action()
                    }
                },
                accentColor = hubColor
            )

            // Horizontal Action Card - Elite Design
            Surface(
                onClick = onNavigateToDevices,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(24.dp),
                color = hubColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = hubContentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PowerSettingsNew, null, tint = hubContentColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "QUICK SEQUENCE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = hubContentColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Broadcast Wake-on-LAN",
                            style = MaterialTheme.typography.bodySmall,
                            color = hubContentColor.copy(alpha = 0.8f)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = hubContentColor.copy(alpha = 0.6f))
                }
            }

            // Information Grid Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "INTELLIGENCE MATRIX",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IntelligenceTile(
                        label = "Nodes",
                        count = uiState.totalPcs.toString(),
                        icon = Icons.Default.Dns,
                        accentColor = Color(accentColor),
                        modifier = Modifier.weight(1f)
                    )
                    IntelligenceTile(
                        label = "Schedules",
                        count = uiState.activeSchedules.toString(),
                        icon = Icons.Default.Schedule,
                        accentColor = Color(accentColor),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid Actions
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "TARGET CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EliteActionTile(
                        label = "New Node",
                        icon = Icons.Default.Add,
                        onClick = onAddPcClick,
                        modifier = Modifier.weight(1f)
                    )
                    EliteActionTile(
                        label = "Seq History",
                        icon = Icons.Default.Timeline,
                        onClick = onShowAllSchedules,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun IntelligenceTile(
    label: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = -(1).sp
                )
                Text(
                    text = label.toUpperCase(java.util.Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EliteActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionWarningSection(
    missingPermissions: List<com.ciobert.wol.util.PermissionsHelper.PermissionItem>,
    onPermissionAction: (String, () -> Unit) -> Unit,
    accentColor: Color
) {
    if (missingPermissions.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "SISTEMA ANALISI: CRITICIT\u00C0 RILEVATE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )

        missingPermissions.forEach { permission ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Warning, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = permission.title.toUpperCase(java.util.Locale.ROOT),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = { onPermissionAction(permission.id, permission.action) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("RISOLVI ORA", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
