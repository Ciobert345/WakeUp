package com.ciobert.wol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ciobert.wol.ui.component.SettingsItem
import com.ciobert.wol.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToStability: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val accentColor by viewModel.accentColor.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Superior Midnight Header
        com.ciobert.wol.ui.component.GradientHeader(
            title = "App Settings",
            subtitle = "System Preferences & Data",
            accentColor = Color(accentColor),
            isDark = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Quick Links Hub
            Text(
                "QUICK ACCESS",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Appearance",
                        subtitle = "Theme, colors and style",
                        onClick = onNavigateToAppearance
                    )
                    Divider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Data Management",
                        subtitle = "Import, export and backups",
                        onClick = onNavigateToData
                    )
                    Divider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Background Stability",
                        subtitle = "Persistent wake-up settings",
                        onClick = onNavigateToStability
                    )
                }
            }

            // Compact About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(accentColor).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Info, null, tint = Color(accentColor), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WakeUp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("v2.2.0 \u2022 Stable", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }

                    val context = androidx.compose.ui.platform.LocalContext.current
                    FilledTonalButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/Ciobert345/WakeUp")
                            )
                            context.startActivity(intent)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("GITHUB", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
