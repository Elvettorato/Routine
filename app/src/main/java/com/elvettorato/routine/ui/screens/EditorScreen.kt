package com.elvettorato.routine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.RoutineAction
import com.elvettorato.routine.data.model.TriggerType
import com.elvettorato.routine.ui.components.ActionEditDialog
import com.elvettorato.routine.ui.components.DayPicker
import com.elvettorato.routine.ui.theme.ActionBluetoothColor
import com.elvettorato.routine.ui.theme.ActionBrightnessColor
import com.elvettorato.routine.ui.theme.ActionDndColor
import com.elvettorato.routine.ui.theme.ActionNotificationColor
import com.elvettorato.routine.ui.theme.ActionVolumeColor
import com.elvettorato.routine.ui.theme.ActionWifiColor
import com.elvettorato.routine.ui.theme.LineagePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    routineId: Long?,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.routineName.collectAsState()
    val triggerType by viewModel.triggerType.collectAsState()
    val hour by viewModel.triggerHour.collectAsState()
    val minute by viewModel.triggerMinute.collectAsState()
    val days by viewModel.triggerDays.collectAsState()
    val lat by viewModel.triggerLat.collectAsState()
    val lng by viewModel.triggerLng.collectAsState()
    val radius by viewModel.triggerRadius.collectAsState()
    val onEnter by viewModel.triggerOnEnter.collectAsState()
    val onExit by viewModel.triggerOnExit.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()

    LaunchedEffect(routineId) {
        if (routineId != null && routineId > 0) {
            viewModel.loadRoutine(routineId)
        }
    }

    LaunchedEffect(saveComplete) {
        if (saveComplete) onNavigateBack()
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var editingActionIndex by remember { mutableStateOf(-1) }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                viewModel.updateTriggerHour(h)
                viewModel.updateTriggerMinute(m)
                showTimePicker = false
            }
        )
    }

    if (showActionDialog) {
        val initialAction = if (editingActionIndex >= 0 && editingActionIndex < actions.size) {
            actions[editingActionIndex]
        } else null
        ActionEditDialog(
            initialAction = initialAction,
            onDismiss = {
                showActionDialog = false
                editingActionIndex = -1
            },
            onSave = { action ->
                if (editingActionIndex >= 0) {
                    val updated = actions.toMutableList().also { it[editingActionIndex] = action }
                    actions.forEachIndexed { i, a -> if (i == editingActionIndex) viewModel.removeAction(i) }
                    viewModel.addAction(action)
                } else {
                    viewModel.addAction(action)
                }
                showActionDialog = false
                editingActionIndex = -1
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (routineId != null) "Edit Routine" else "New Routine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Routine Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Trigger",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = triggerType == TriggerType.TIME,
                    onClick = { viewModel.updateTriggerType(TriggerType.TIME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Time")
                }
                SegmentedButton(
                    selected = triggerType == TriggerType.LOCATION,
                    onClick = { viewModel.updateTriggerType(TriggerType.LOCATION) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Location")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (triggerType == TriggerType.TIME) {
                TimeTriggerSection(
                    hour = hour,
                    minute = minute,
                    days = days,
                    onTimeClick = { showTimePicker = true },
                    onDaysChanged = viewModel::updateTriggerDays
                )
            } else {
                LocationTriggerSection(
                    lat = lat,
                    lng = lng,
                    radius = radius,
                    onEnter = onEnter,
                    onExit = onExit,
                    onLatChange = viewModel::updateLat,
                    onLngChange = viewModel::updateLng,
                    onRadiusChange = viewModel::updateRadius,
                    onEnterChange = viewModel::updateOnEnter,
                    onExitChange = viewModel::updateOnExit
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Actions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            actions.forEachIndexed { index, action ->
                ActionCard(
                    action = action,
                    onEdit = {
                        editingActionIndex = index
                        showActionDialog = true
                    },
                    onDelete = { viewModel.removeAction(index) }
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = { showActionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Action")
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = LineagePrimary)
            ) {
                Text(if (isSaving) "Saving..." else "Save Routine")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimeTriggerSection(
    hour: Int,
    minute: Int,
    days: List<Int>,
    onTimeClick: () -> Unit,
    onDaysChanged: (List<Int>) -> Unit
) {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    val m = minute.toString().padStart(2, '0')

    FilledTonalButton(
        onClick = onTimeClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("$h:$m $amPm")
    }

    Spacer(Modifier.height(16.dp))
    DayPicker(selectedDays = days, onDaysChanged = onDaysChanged)
}

@Composable
private fun LocationTriggerSection(
    lat: Double,
    lng: Double,
    radius: Float,
    onEnter: Boolean,
    onExit: Boolean,
    onLatChange: (Double) -> Unit,
    onLngChange: (Double) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onEnterChange: (Boolean) -> Unit,
    onExitChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = if (lat != 0.0) lat.toString() else "",
        onValueChange = { it.toDoubleOrNull()?.let(onLatChange) },
        label = { Text("Latitude") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = if (lng != 0.0) lng.toString() else "",
        onValueChange = { it.toDoubleOrNull()?.let(onLngChange) },
        label = { Text("Longitude") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(Modifier.height(8.dp))
    Text("Radius: ${radius.toInt()}m", style = MaterialTheme.typography.bodySmall)
    Slider(
        value = radius,
        onValueChange = onRadiusChange,
        valueRange = 10f..1000f
    )
    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Checkbox(checked = onEnter, onCheckedChange = onEnterChange)
        Text("On enter", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        androidx.compose.material3.Checkbox(checked = onExit, onCheckedChange = onExitChange)
        Text("On exit", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionCard(
    action: RoutineAction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val type = ActionType.fromValue(action.type)
    val (icon, color, summary) = when (type) {
        ActionType.DND -> Icons.Default.DoNotDisturbAlt to ActionDndColor to
            "DND: ${action.dndMode ?: "OFF"}"
        ActionType.VOLUME -> Icons.Default.VolumeUp to ActionVolumeColor to
            "Vol: M${action.mediaVolume} R${action.ringVolume} A${action.alarmVolume} N${action.notificationVolume}"
        ActionType.BRIGHTNESS -> Icons.Default.BrightnessMedium to ActionBrightnessColor to
            "Brightness: ${action.brightnessAuto?.let { if (it) "Auto" else "${action.brightnessLevel}" } ?: "?"}"
        ActionType.WIFI -> Icons.Default.Wifi to ActionWifiColor to
            "WiFi: ${if (action.wifiEnabled == true) "ON" else "OFF"}"
        ActionType.BLUETOOTH -> Icons.Default.Bluetooth to ActionBluetoothColor to
            "Bluetooth: ${if (action.bluetoothEnabled == true) "ON" else "OFF"}"
        ActionType.NOTIFICATION -> Icons.Default.Notifications to ActionNotificationColor to
            "Notify: ${action.notificationTitle ?: ""}"
    }

    Card(
        onClick = onEdit,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(type.name, style = MaterialTheme.typography.labelLarge)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    var isPM by remember { mutableStateOf(initialHour >= 12) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("+", style = MaterialTheme.typography.headlineMedium) }
                        Text(
                            text = String.format("%02d", if (isPM) (if (hour % 12 == 0) 12 else hour % 12) else hour),
                            style = MaterialTheme.typography.displaySmall
                        )
                        TextButton(onClick = { hour = (hour - 1 + 24) % 24 }) { Text("-", style = MaterialTheme.typography.headlineMedium) }
                    }
                    Text(":", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { minute = (minute + 1) % 60 }) { Text("+", style = MaterialTheme.typography.headlineMedium) }
                        Text(
                            text = String.format("%02d", minute),
                            style = MaterialTheme.typography.displaySmall
                        )
                        TextButton(onClick = { minute = (minute - 1 + 60) % 60 }) { Text("-", style = MaterialTheme.typography.headlineMedium) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(
                    onClick = { isPM = !isPM }
                ) {
                    Text(if (isPM) "PM" else "AM")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = if (isPM) (hour % 12) + 12 else hour % 12
                onConfirm(h, minute)
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
