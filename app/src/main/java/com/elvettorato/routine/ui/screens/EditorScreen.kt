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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.R
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.RoutineAction
import com.elvettorato.routine.ui.components.ActionEditDialog
import com.elvettorato.routine.ui.components.DayPicker
import com.elvettorato.routine.ui.components.MapLocationPicker
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
    var showMapPicker by remember { mutableStateOf(false) }
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

    if (showMapPicker) {
        MapLocationPicker(
            initialLat = lat,
            initialLng = lng,
            initialRadius = radius,
            onLocationSelected = { newLat, newLng, newRadius ->
                viewModel.updateLat(newLat)
                viewModel.updateLng(newLng)
                viewModel.updateRadius(newRadius)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
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
                title = { Text(if (routineId != null) stringResource(R.string.edit_routine) else stringResource(R.string.new_routine)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                label = { Text(stringResource(R.string.routine_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.trigger),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.time),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TimeTriggerSection(
                hour = hour,
                minute = minute,
                days = days,
                onTimeClick = { showTimePicker = true },
                onDaysChanged = viewModel::updateTriggerDays
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.location),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                onExitChange = viewModel::updateOnExit,
                onShowMap = { showMapPicker = true }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.actions),
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
                Text(stringResource(R.string.add_action))
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = LineagePrimary)
            ) {
                Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_routine))
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
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
    }
    val timeText = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(cal.time)

    FilledTonalButton(
        onClick = onTimeClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(timeText)
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
    onExitChange: (Boolean) -> Unit,
    onShowMap: () -> Unit
) {
    Card(
        onClick = onShowMap,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (lat != 0.0 && lng != 0.0) "%.5f, %.5f".format(lat, lng)
                    else stringResource(R.string.location),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.radius_format, radius.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Checkbox(checked = onEnter, onCheckedChange = onEnterChange)
        Text(stringResource(R.string.on_enter), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        androidx.compose.material3.Checkbox(checked = onExit, onCheckedChange = onExitChange)
        Text(stringResource(R.string.on_exit), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionCard(
    action: RoutineAction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val type = ActionType.fromValue(action.type)
    val icon: ImageVector
    val color: Color
    val summary: String
    when (type) {
        ActionType.DND -> {
            icon = Icons.Default.DoNotDisturbAlt; color = ActionDndColor
            summary = stringResource(R.string.summary_dnd, action.dndMode ?: "OFF")
        }
        ActionType.VOLUME -> {
            icon = Icons.Default.VolumeUp; color = ActionVolumeColor
            summary = stringResource(R.string.summary_volume, action.mediaVolume ?: 0, action.ringVolume ?: 0, action.alarmVolume ?: 0, action.notificationVolume ?: 0)
        }
        ActionType.BRIGHTNESS -> {
            icon = Icons.Default.BrightnessMedium; color = ActionBrightnessColor
            val brightText = if (action.brightnessAuto == true) stringResource(R.string.auto) else "${action.brightnessLevel}"
            summary = stringResource(R.string.summary_brightness, brightText)
        }
        ActionType.WIFI -> {
            icon = Icons.Default.Wifi; color = ActionWifiColor
            summary = stringResource(R.string.summary_wifi, if (action.wifiEnabled == true) stringResource(R.string.on) else stringResource(R.string.off))
        }
        ActionType.BLUETOOTH -> {
            icon = Icons.Default.Bluetooth; color = ActionBluetoothColor
            summary = stringResource(R.string.summary_bluetooth, if (action.bluetoothEnabled == true) stringResource(R.string.on) else stringResource(R.string.off))
        }
        ActionType.NOTIFICATION -> {
            icon = Icons.Default.Notifications; color = ActionNotificationColor
            summary = stringResource(R.string.summary_notification, action.notificationTitle ?: "")
        }
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        title = { Text(stringResource(R.string.select_time), style = MaterialTheme.typography.headlineSmall) },
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
                    Text(if (isPM) stringResource(R.string.pm) else stringResource(R.string.am))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = if (isPM) (hour % 12) + 12 else hour % 12
                onConfirm(h, minute)
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
