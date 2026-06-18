package com.elvettorato.routine.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.R
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.RoutineAction
import com.elvettorato.routine.service.GeofenceHelper
import com.elvettorato.routine.ui.components.ActionEditDialog
import com.elvettorato.routine.ui.components.DayPicker
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    routineId: Long?,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.routineName.collectAsState()
    val hasTimeTrigger by viewModel.hasTimeTrigger.collectAsState()
    val hour by viewModel.triggerHour.collectAsState()
    val minute by viewModel.triggerMinute.collectAsState()
    val days by viewModel.triggerDays.collectAsState()
    val hasLocationTrigger by viewModel.hasLocationTrigger.collectAsState()
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
    var editingActionIndex by remember { mutableIntStateOf(-1) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            val fusedLocationClient =
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            @Suppress("MissingPermission")
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build(),
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateLat(it.latitude)
                    viewModel.updateLng(it.longitude)
                }
            }
            if (!GeofenceHelper.hasBackgroundLocationPermission(context)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Per geofence anche in background, concedi 'Consenti sempre'",
                        "Impostazioni"
                    )
                    settingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                }
            }
        }
    }
    val writeSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (Settings.System.canWrite(context)) viewModel.save() }
    val notificationPolicyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (routineId != null) stringResource(R.string.edit_routine) else stringResource(R.string.new_routine)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.time),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = hasTimeTrigger,
                    onCheckedChange = viewModel::updateHasTimeTrigger
                )
            }
            AnimatedVisibility(
                visible = hasTimeTrigger,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
                TimeTriggerSection(
                    hour = hour,
                    minute = minute,
                    days = days,
                    onTimeClick = { showTimePicker = true },
                    onDaysChanged = viewModel::updateTriggerDays
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.location),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = hasLocationTrigger,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (!hasFine) {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            } else {
                                viewModel.updateHasLocationTrigger(true)
                                if (!GeofenceHelper.hasBackgroundLocationPermission(context)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Per geofence anche in background, concedi 'Consenti sempre'",
                                            "Impostazioni"
                                        )
                                        settingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        })
                                    }
                                }
                            }
                        } else {
                            viewModel.updateHasLocationTrigger(false)
                        }
                    }
                )
            }
            AnimatedVisibility(
                visible = hasLocationTrigger,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
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
                    onGetCurrentLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

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
                onClick = {
                    val hasBrightness = actions.any { ActionType.fromValue(it.type) == ActionType.BRIGHTNESS }
                    val hasDnd = actions.any { ActionType.fromValue(it.type) == ActionType.DND }

                    var needsIntent = false

                    if (hasBrightness && !Settings.System.canWrite(context)) {
                        needsIntent = true
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        writeSettingsLauncher.launch(intent)
                    }
                    if (hasDnd) {
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        if (!nm.isNotificationPolicyAccessGranted) {
                            needsIntent = true
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            notificationPolicyLauncher.launch(intent)
                        }
                    }
                    if (!needsIntent) viewModel.save()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
    onGetCurrentLocation: () -> Unit
) {
    var latText by remember(lat) { mutableStateOf(if (lat != 0.0) lat.toString() else "") }
    var lngText by remember(lng) { mutableStateOf(if (lng != 0.0) lng.toString() else "") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latText,
                    onValueChange = { v ->
                        latText = v
                        v.toDoubleOrNull()?.let { onLatChange(it) }
                    },
                    label = { Text("Lat") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { v ->
                        lngText = v
                        v.toDoubleOrNull()?.let { onLngChange(it) }
                    },
                    label = { Text("Lng") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(Modifier.height(8.dp))

            FilledTonalButton(
                onClick = onGetCurrentLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.get_current_location))
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        stringResource(R.string.radius_format, radius.toInt()),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Slider(
        value = radius,
        onValueChange = onRadiusChange,
        valueRange = 10f..1000f
    )

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
            icon = Icons.Default.DoNotDisturbAlt; color = MaterialTheme.colorScheme.error
            val mode = action.dndMode ?: "OFF"
            val dndCustom = buildString {
                append(mode)
                val calls = action.dndAllowCallsFrom
                val msgs = action.dndAllowMessagesFrom
                if (calls != null && calls != "NONE") append(" · Calls:$calls")
                if (msgs != null && msgs != "NONE") append(" · Msgs:$msgs")
            }
            summary = stringResource(R.string.summary_dnd, dndCustom)
        }
        ActionType.VOLUME -> {
            icon = Icons.AutoMirrored.Filled.VolumeUp; color = MaterialTheme.colorScheme.primary
            summary = stringResource(R.string.summary_volume, action.mediaVolume ?: 0, action.ringVolume ?: 0, action.alarmVolume ?: 0, action.notificationVolume ?: 0)
        }
        ActionType.BRIGHTNESS -> {
            icon = Icons.Default.BrightnessMedium; color = MaterialTheme.colorScheme.tertiary
            val brightText = if (action.brightnessAuto == true) stringResource(R.string.auto) else "${action.brightnessLevel}"
            summary = stringResource(R.string.summary_brightness, brightText)
        }
        ActionType.RINGER_MODE -> {
            icon = Icons.Default.NotificationsActive; color = MaterialTheme.colorScheme.secondary
            val mode = action.ringerMode?.let {
                when (it) {
                    "NORMAL" -> stringResource(R.string.ringer_normal)
                    "VIBRATE" -> stringResource(R.string.ringer_vibrate)
                    "SILENT" -> stringResource(R.string.ringer_silent)
                    else -> it
                }
            } ?: stringResource(R.string.ringer_normal)
            summary = stringResource(R.string.summary_ringer_mode, mode)
        }
        ActionType.NOTIFICATION -> {
            icon = Icons.Default.Notifications; color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time), style = MaterialTheme.typography.headlineSmall) },
        text = {
            TimePicker(
                state = state
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(state.hour, state.minute)
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
