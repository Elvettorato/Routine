package com.elvettorato.routine.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.R
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.DndAllowFrom
import com.elvettorato.routine.data.model.DndMode
import com.elvettorato.routine.data.model.NotificationPriority
import com.elvettorato.routine.data.model.RingerMode
import com.elvettorato.routine.data.model.RoutineAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionEditDialog(
    initialAction: RoutineAction? = null,
    onDismiss: () -> Unit,
    onSave: (RoutineAction) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialAction?.let { ActionType.fromValue(it.type) } ?: ActionType.DND) }
    var typeExpanded by remember { mutableStateOf(false) }

    var dndMode by remember { mutableStateOf(initialAction?.dndMode?.let { DndMode.valueOf(it) } ?: DndMode.PRIORITY_ONLY) }
    var dndAllowCallsFrom by remember { mutableStateOf(
        initialAction?.dndAllowCallsFrom?.let { DndAllowFrom.valueOf(it) } ?: DndAllowFrom.NONE
    ) }
    var dndAllowMessagesFrom by remember { mutableStateOf(
        initialAction?.dndAllowMessagesFrom?.let { DndAllowFrom.valueOf(it) } ?: DndAllowFrom.NONE
    ) }
    var dndAllowAlarms by remember { mutableStateOf(initialAction?.dndAllowAlarms ?: true) }
    var dndAllowMedia by remember { mutableStateOf(initialAction?.dndAllowMedia ?: false) }
    var dndAllowSystem by remember { mutableStateOf(initialAction?.dndAllowSystem ?: false) }
    var dndCallsExpanded by remember { mutableStateOf(false) }
    var dndMsgsExpanded by remember { mutableStateOf(false) }

    var mediaVolume by remember { mutableIntStateOf(initialAction?.mediaVolume ?: 7) }
    var ringVolume by remember { mutableIntStateOf(initialAction?.ringVolume ?: 5) }
    var alarmVolume by remember { mutableIntStateOf(initialAction?.alarmVolume ?: 10) }
    var notificationVolume by remember { mutableIntStateOf(initialAction?.notificationVolume ?: 5) }

    var brightnessLevel by remember { mutableIntStateOf(initialAction?.brightnessLevel ?: 128) }
    var brightnessAuto by remember { mutableStateOf(initialAction?.brightnessAuto ?: false) }

    var ringerMode by remember { mutableStateOf(initialAction?.ringerMode?.let { RingerMode.valueOf(it) } ?: RingerMode.NORMAL) }

    var notificationTitle by remember { mutableStateOf(initialAction?.notificationTitle ?: "") }
    var notificationText by remember { mutableStateOf(initialAction?.notificationText ?: "") }
    var notificationPriority by remember { mutableStateOf(
        initialAction?.notificationPriority?.let { NotificationPriority.valueOf(it) } ?: NotificationPriority.HIGH
    ) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialAction != null) stringResource(R.string.edit_action) else stringResource(R.string.add_action_dialog),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.action_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ActionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (selectedType) {
                    ActionType.DND -> {
                        Text(stringResource(R.string.dnd_mode), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        DndMode.entries.forEach { mode ->
                            val label = when (mode) {
                                DndMode.OFF -> stringResource(R.string.dnd_off)
                                DndMode.PRIORITY_ONLY -> stringResource(R.string.dnd_priority)
                                DndMode.TOTAL_SILENCE -> stringResource(R.string.dnd_total)
                                DndMode.ALARMS_ONLY -> stringResource(R.string.dnd_alarms)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.RadioButton(
                                    selected = dndMode == mode,
                                    onClick = { dndMode = mode }
                                )
                            }
                        }

                        if (dndMode == DndMode.PRIORITY_ONLY) {
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.dnd_customize), style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = dndCallsExpanded,
                                onExpandedChange = { dndCallsExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = dndAllowFromLabel(dndAllowCallsFrom),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.dnd_allow_calls)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dndCallsExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = dndCallsExpanded,
                                    onDismissRequest = { dndCallsExpanded = false }
                                ) {
                                    DndAllowFrom.entries.forEach { allow ->
                                        DropdownMenuItem(
                                            text = { Text(dndAllowFromLabel(allow)) },
                                            onClick = {
                                                dndAllowCallsFrom = allow
                                                dndCallsExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = dndMsgsExpanded,
                                onExpandedChange = { dndMsgsExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = dndAllowFromLabel(dndAllowMessagesFrom),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.dnd_allow_messages)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dndMsgsExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = dndMsgsExpanded,
                                    onDismissRequest = { dndMsgsExpanded = false }
                                ) {
                                    DndAllowFrom.entries.forEach { allow ->
                                        DropdownMenuItem(
                                            text = { Text(dndAllowFromLabel(allow)) },
                                            onClick = {
                                                dndAllowMessagesFrom = allow
                                                dndMsgsExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.dnd_allow_alarms), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Switch(checked = dndAllowAlarms, onCheckedChange = { dndAllowAlarms = it })
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.dnd_allow_media), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Switch(checked = dndAllowMedia, onCheckedChange = { dndAllowMedia = it })
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.dnd_allow_system), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Switch(checked = dndAllowSystem, onCheckedChange = { dndAllowSystem = it })
                            }
                        }
                    }

                    ActionType.VOLUME -> {
                        VolumeSlider(stringResource(R.string.media), mediaVolume, 0f..15f) { mediaVolume = it.toInt() }
                        VolumeSlider(stringResource(R.string.ring), ringVolume, 0f..15f) { ringVolume = it.toInt() }
                        VolumeSlider(stringResource(R.string.alarm), alarmVolume, 0f..15f) { alarmVolume = it.toInt() }
                        VolumeSlider(stringResource(R.string.notification), notificationVolume, 0f..15f) { notificationVolume = it.toInt() }
                    }

                    ActionType.BRIGHTNESS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.auto), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(checked = brightnessAuto, onCheckedChange = { brightnessAuto = it })
                        }
                        if (!brightnessAuto) {
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.level_format, (brightnessLevel * 100 / 255)), style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = brightnessLevel.toFloat(),
                                onValueChange = { brightnessLevel = it.toInt() },
                                valueRange = 0f..255f,
                                steps = 254
                            )
                        }
                    }

                    ActionType.RINGER_MODE -> {
                        Text(stringResource(R.string.ringer_mode), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        RingerMode.entries.forEach { mode ->
                            val label = when (mode) {
                                RingerMode.NORMAL -> stringResource(R.string.ringer_normal)
                                RingerMode.VIBRATE -> stringResource(R.string.ringer_vibrate)
                                RingerMode.SILENT -> stringResource(R.string.ringer_silent)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.RadioButton(
                                    selected = ringerMode == mode,
                                    onClick = { ringerMode = mode }
                                )
                            }
                        }
                    }

                    ActionType.NOTIFICATION -> {
                        OutlinedTextField(
                            value = notificationTitle,
                            onValueChange = { notificationTitle = it },
                            label = { Text(stringResource(R.string.notification_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notificationText,
                            onValueChange = { notificationText = it },
                            label = { Text(stringResource(R.string.notification_content)) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = priorityExpanded,
                            onExpandedChange = { priorityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = notificationPriority.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.priority)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = priorityExpanded,
                                onDismissRequest = { priorityExpanded = false }
                            ) {
                                NotificationPriority.entries.forEach { p ->
                                    val label = when (p) {
                                        NotificationPriority.HIGH -> stringResource(R.string.priority_high)
                                        NotificationPriority.DEFAULT -> stringResource(R.string.priority_default)
                                        NotificationPriority.LOW -> stringResource(R.string.priority_low)
                                        NotificationPriority.MIN -> stringResource(R.string.priority_min)
                                    }
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            notificationPriority = p
                                            priorityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                val action = when (selectedType) {
                    ActionType.DND -> RoutineAction.createDnd(
                        dndMode, dndAllowCallsFrom, dndAllowMessagesFrom,
                        dndAllowAlarms, dndAllowMedia, dndAllowSystem
                    )
                    ActionType.VOLUME -> RoutineAction.createVolume(mediaVolume, ringVolume, alarmVolume, notificationVolume)
                    ActionType.BRIGHTNESS -> RoutineAction.createBrightness(brightnessLevel, brightnessAuto)
                    ActionType.RINGER_MODE -> RoutineAction.createRingerMode(ringerMode)
                    ActionType.NOTIFICATION -> RoutineAction.createNotification(notificationTitle, notificationText, notificationPriority.value)
                }
                onSave(action)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun dndAllowFromLabel(allow: DndAllowFrom): String = when (allow) {
    DndAllowFrom.NONE -> stringResource(R.string.dnd_allow_none)
    DndAllowFrom.CONTACTS -> stringResource(R.string.dnd_allow_contacts)
    DndAllowFrom.STARRED -> stringResource(R.string.dnd_allow_starred)
    DndAllowFrom.EVERYONE -> stringResource(R.string.dnd_allow_everyone)
}

@Composable
private fun VolumeSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: $value", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            steps = 14
        )
    }
}
