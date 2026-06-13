package com.elvettorato.routine.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActionType(val value: String, val labelRes: String) {
    DND("DND", "action_dnd"),
    VOLUME("VOLUME", "action_volume"),
    BRIGHTNESS("BRIGHTNESS", "action_brightness"),
    RINGER_MODE("RINGER_MODE", "action_ringer_mode"),
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

enum class DndAllowFrom(val value: String, val labelRes: String) {
    NONE("NONE", "dnd_allow_none"),
    CONTACTS("CONTACTS", "dnd_allow_contacts"),
    STARRED("STARRED", "dnd_allow_starred"),
    EVERYONE("EVERYONE", "dnd_allow_everyone")
}

enum class RingerMode(val value: String, val labelRes: String) {
    NORMAL("NORMAL", "ringer_normal"),
    VIBRATE("VIBRATE", "ringer_vibrate"),
    SILENT("SILENT", "ringer_silent")
}

enum class NotificationPriority(val value: String, val labelRes: String) {
    HIGH("HIGH", "priority_high"),
    DEFAULT("DEFAULT", "priority_default"),
    LOW("LOW", "priority_low"),
    MIN("MIN", "priority_min")
}
