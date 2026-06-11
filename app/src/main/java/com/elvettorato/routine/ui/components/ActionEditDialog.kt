package com.elvettorato.routine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.DndMode
import com.elvettorato.routine.data.model.RoutineAction
import com.elvettorato.routine.ui.theme.ActionBluetoothColor
import com.elvettorato.routine.ui.theme.ActionBrightnessColor
import com.elvettorato.routine.ui.theme.ActionDndColor
import com.elvettorato.routine.ui.theme.ActionNotificationColor
import com.elvettorato.routine.ui.theme.ActionVolumeColor
import com.elvettorato.routine.ui.theme.ActionWifiColor

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

    var mediaVolume by remember { mutableIntStateOf(initialAction?.mediaVolume ?: 7) }
    var ringVolume by remember { mutableIntStateOf(initialAction?.ringVolume ?: 5) }
    var alarmVolume by remember { mutableIntStateOf(initialAction?.alarmVolume ?: 10) }
    var notificationVolume by remember { mutableIntStateOf(initialAction?.notificationVolume ?: 5) }

    var brightnessLevel by remember { mutableIntStateOf(initialAction?.brightnessLevel ?: 128) }
    var brightnessAuto by remember { mutableStateOf(initialAction?.brightnessAuto ?: false) }

    var wifiEnabled by remember { mutableStateOf(initialAction?.wifiEnabled ?: true) }
    var bluetoothEnabled by remember { mutableStateOf(initialAction?.bluetoothEnabled ?: true) }

    var notificationTitle by remember { mutableStateOf(initialAction?.notificationTitle ?: "") }
    var notificationText by remember { mutableStateOf(initialAction?.notificationText ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialAction != null) "Edit Action" else "Add Action",
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
                        label = { Text("Action Type") },
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
                        Text("Do Not Disturb Mode", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        DndMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    mode.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.RadioButton(
                                    selected = dndMode == mode,
                                    onClick = { dndMode = mode }
                                )
                            }
                        }
                    }

                    ActionType.VOLUME -> {
                        VolumeSlider("Media", mediaVolume, 0f..15f) { mediaVolume = it.toInt() }
                        VolumeSlider("Ring", ringVolume, 0f..15f) { ringVolume = it.toInt() }
                        VolumeSlider("Alarm", alarmVolume, 0f..15f) { alarmVolume = it.toInt() }
                        VolumeSlider("Notification", notificationVolume, 0f..15f) { notificationVolume = it.toInt() }
                    }

                    ActionType.BRIGHTNESS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Auto", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(checked = brightnessAuto, onCheckedChange = { brightnessAuto = it })
                        }
                        if (!brightnessAuto) {
                            Spacer(Modifier.height(8.dp))
                            Text("Level: ${(brightnessLevel * 100 / 255)}%", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = brightnessLevel.toFloat(),
                                onValueChange = { brightnessLevel = it.toInt() },
                                valueRange = 0f..255f,
                                steps = 254
                            )
                        }
                    }

                    ActionType.WIFI -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable WiFi", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(checked = wifiEnabled, onCheckedChange = { wifiEnabled = it })
                        }
                    }

                    ActionType.BLUETOOTH -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable Bluetooth", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(checked = bluetoothEnabled, onCheckedChange = { bluetoothEnabled = it })
                        }
                    }

                    ActionType.NOTIFICATION -> {
                        OutlinedTextField(
                            value = notificationTitle,
                            onValueChange = { notificationTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notificationText,
                            onValueChange = { notificationText = it },
                            label = { Text("Content") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                val action = when (selectedType) {
                    ActionType.DND -> RoutineAction.createDnd(dndMode)
                    ActionType.VOLUME -> RoutineAction.createVolume(mediaVolume, ringVolume, alarmVolume, notificationVolume)
                    ActionType.BRIGHTNESS -> RoutineAction.createBrightness(brightnessLevel, brightnessAuto)
                    ActionType.WIFI -> RoutineAction.createWifi(wifiEnabled)
                    ActionType.BLUETOOTH -> RoutineAction.createBluetooth(bluetoothEnabled)
                    ActionType.NOTIFICATION -> RoutineAction.createNotification(notificationTitle, notificationText)
                }
                onSave(action)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
