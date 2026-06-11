package com.elvettorato.routine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.model.TriggerType
import com.elvettorato.routine.ui.theme.ActionBluetoothColor
import com.elvettorato.routine.ui.theme.ActionBrightnessColor
import com.elvettorato.routine.ui.theme.ActionDndColor
import com.elvettorato.routine.ui.theme.ActionNotificationColor
import com.elvettorato.routine.ui.theme.ActionVolumeColor
import com.elvettorato.routine.ui.theme.ActionWifiColor
import com.elvettorato.routine.ui.theme.LineagePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Routine") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRoutine,
                containerColor = LineagePrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Routine")
            }
        }
    ) { padding ->
        if (routines.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutine(routine.id, it) },
                        onDelete = { viewModel.deleteRoutine(routine.id) },
                        onClick = { onEditRoutine(routine.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No routines yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap + to create your first routine",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    TriggerSummary(routine)
                }
                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = LineagePrimary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIconsRow(routine.actions.map { ActionType.fromValue(it.type) })
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerSummary(routine: Routine) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (routine.triggerType == TriggerType.TIME) Icons.Default.Schedule else Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (routine.triggerType == TriggerType.TIME) {
                val hour = routine.triggerHour ?: 0
                val minute = routine.triggerMinute ?: 0
                val amPm = if (hour < 12) "AM" else "PM"
                val h = if (hour % 12 == 0) 12 else hour % 12
                val m = minute.toString().padStart(2, '0')
                val days = routine.triggerDaysOfWeek
                if (days.isNullOrEmpty()) "$h:$m $amPm"
                else {
                    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val dayStr = days.mapNotNull { dayLabels.getOrNull(it - 1) }
                    "$h:$m $amPm \u00B7 ${dayStr.joinToString(",")}"
                }
            } else {
                "Location: ${routine.triggerLatitude?.let { "%.2f".format(it) } ?: "?"}, ${routine.triggerLongitude?.let { "%.2f".format(it) } ?: "?"}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionIconsRow(types: List<ActionType>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        types.take(5).forEach { type ->
            val (icon, color) = when (type) {
                ActionType.DND -> Icons.Default.DoNotDisturbAlt to ActionDndColor
                ActionType.VOLUME -> Icons.Default.VolumeUp to ActionVolumeColor
                ActionType.BRIGHTNESS -> Icons.Default.BrightnessMedium to ActionBrightnessColor
                ActionType.WIFI -> Icons.Default.Wifi to ActionWifiColor
                ActionType.BLUETOOTH -> Icons.Default.Bluetooth to ActionBluetoothColor
                ActionType.NOTIFICATION -> Icons.Default.Notifications to ActionNotificationColor
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }
        if (types.size > 5) {
            Text(
                "+${types.size - 5}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
