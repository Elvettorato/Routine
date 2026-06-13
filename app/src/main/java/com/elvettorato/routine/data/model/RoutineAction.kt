package com.elvettorato.routine.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RoutineAction(
    val type: String,
    val dndMode: String? = null,
    val dndAllowCallsFrom: String? = null,
    val dndAllowMessagesFrom: String? = null,
    val dndAllowAlarms: Boolean? = null,
    val dndAllowMedia: Boolean? = null,
    val dndAllowSystem: Boolean? = null,
    val ringerMode: String? = null,
    val mediaVolume: Int? = null,
    val ringVolume: Int? = null,
    val alarmVolume: Int? = null,
    val notificationVolume: Int? = null,
    val brightnessLevel: Int? = null,
    val brightnessAuto: Boolean? = null,
    val notificationTitle: String? = null,
    val notificationText: String? = null,
    val notificationPriority: String? = null
) {
    companion object {
        private val gson = Gson()

        fun fromJson(json: String): List<RoutineAction> {
            val type = object : TypeToken<List<RoutineAction>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        }

        fun toJson(actions: List<RoutineAction>): String {
            return gson.toJson(actions)
        }

        fun createDnd(
            mode: DndMode,
            allowCallsFrom: DndAllowFrom = DndAllowFrom.NONE,
            allowMessagesFrom: DndAllowFrom = DndAllowFrom.NONE,
            allowAlarms: Boolean = true,
            allowMedia: Boolean = false,
            allowSystem: Boolean = false
        ) = RoutineAction(
            type = ActionType.DND.value,
            dndMode = mode.value,
            dndAllowCallsFrom = allowCallsFrom.value,
            dndAllowMessagesFrom = allowMessagesFrom.value,
            dndAllowAlarms = allowAlarms,
            dndAllowMedia = allowMedia,
            dndAllowSystem = allowSystem
        )

        fun createVolume(media: Int? = null, ring: Int? = null, alarm: Int? = null, notification: Int? = null) = RoutineAction(
            type = ActionType.VOLUME.value,
            mediaVolume = media,
            ringVolume = ring,
            alarmVolume = alarm,
            notificationVolume = notification
        )

        fun createBrightness(level: Int, auto: Boolean) = RoutineAction(
            type = ActionType.BRIGHTNESS.value,
            brightnessLevel = level,
            brightnessAuto = auto
        )

        fun createRingerMode(mode: RingerMode) = RoutineAction(
            type = ActionType.RINGER_MODE.value,
            ringerMode = mode.value
        )

        fun createNotification(title: String, text: String, priority: String = "HIGH") = RoutineAction(
            type = ActionType.NOTIFICATION.value,
            notificationTitle = title,
            notificationText = text,
            notificationPriority = priority
        )
    }
}
