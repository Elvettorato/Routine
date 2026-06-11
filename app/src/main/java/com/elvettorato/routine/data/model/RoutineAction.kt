package com.elvettorato.routine.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RoutineAction(
    val type: String,
    val dndMode: String? = null,
    val mediaVolume: Int? = null,
    val ringVolume: Int? = null,
    val alarmVolume: Int? = null,
    val notificationVolume: Int? = null,
    val brightnessLevel: Int? = null,
    val brightnessAuto: Boolean? = null,
    val wifiEnabled: Boolean? = null,
    val bluetoothEnabled: Boolean? = null,
    val notificationTitle: String? = null,
    val notificationText: String? = null
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

        fun createDnd(mode: DndMode) = RoutineAction(
            type = ActionType.DND.value,
            dndMode = mode.value
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

        fun createWifi(enabled: Boolean) = RoutineAction(
            type = ActionType.WIFI.value,
            wifiEnabled = enabled
        )

        fun createBluetooth(enabled: Boolean) = RoutineAction(
            type = ActionType.BLUETOOTH.value,
            bluetoothEnabled = enabled
        )

        fun createNotification(title: String, text: String) = RoutineAction(
            type = ActionType.NOTIFICATION.value,
            notificationTitle = title,
            notificationText = text
        )
    }
}
