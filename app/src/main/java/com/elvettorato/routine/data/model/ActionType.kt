package com.elvettorato.routine.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActionType(val value: String, val labelRes: String) {
    DND("DND", "action_dnd"),
    VOLUME("VOLUME", "action_volume"),
    BRIGHTNESS("BRIGHTNESS", "action_brightness"),
    WIFI("WIFI", "action_wifi"),
    BLUETOOTH("BLUETOOTH", "action_bluetooth"),
    NOTIFICATION("NOTIFICATION", "action_notification");

    companion object {
        fun fromValue(value: String): ActionType =
            entries.firstOrNull { it.value == value } ?: NOTIFICATION
    }
}

enum class DndMode(val value: String, val labelRes: String) {
    OFF("OFF", "dnd_off"),
    PRIORITY_ONLY("PRIORITY_ONLY", "dnd_priority"),
    TOTAL_SILENCE("TOTAL_SILENCE", "dnd_total"),
    ALARMS_ONLY("ALARMS_ONLY", "dnd_alarms")
}
