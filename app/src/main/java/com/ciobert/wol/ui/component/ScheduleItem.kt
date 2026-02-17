package com.ciobert.wol.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciobert.wol.data.local.entity.ScheduleEntity
import java.util.Locale

@Composable
fun ScheduleItem(
    schedule: ScheduleEntity,
    pcName: String? = null, // Optional, for global list
    onToggle: (ScheduleEntity) -> Unit,
    onDelete: (ScheduleEntity) -> Unit,
    onEdit: (ScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit(schedule) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time and Days
            Column(modifier = Modifier.weight(1f)) {
                // PC Name Label (if provided)
                if (!pcName.isNullOrBlank()) {
                    Text(
                        text = pcName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", schedule.timeHour, schedule.timeMinute),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (schedule.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Days Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    days.forEachIndexed { index, label ->
                        val isSet = (schedule.daysBitmap and (1 shl index)) != 0
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSet) 
                                        if (schedule.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSet) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = { onToggle(schedule) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = { onDelete(schedule) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Schedule",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
