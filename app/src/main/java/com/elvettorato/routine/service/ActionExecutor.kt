package com.elvettorato.routine.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.bluetooth.BluetoothAdapter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elvettorato.routine.R
import com.elvettorato.routine.RoutineApp
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.DndMode
import com.elvettorato.routine.data.model.RoutineAction

object ActionExecutor {

    fun execute(context: Context, actions: List<RoutineAction>, routineName: String) {
        actions.forEach { action -> executeSingle(context, action) }
        sendExecutionNotification(context, routineName, actions)
    }

    private fun executeSingle(context: Context, action: RoutineAction) {
        when (ActionType.fromValue(action.type)) {
            ActionType.DND -> setDndMode(context, action.dndMode)
            ActionType.VOLUME -> setVolume(context, action)
            ActionType.BRIGHTNESS -> setBrightness(context, action)
            ActionType.WIFI -> setWifi(context, action.wifiEnabled)
            ActionType.BLUETOOTH -> setBluetooth(context, action.bluetoothEnabled)
            ActionType.NOTIFICATION -> sendActionNotification(context, action)
        }
    }

    private fun setDndMode(context: Context, mode: String?) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val policy = when (DndMode.valueOf(mode ?: "OFF")) {
            DndMode.OFF -> NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_NONE,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET
            )
            DndMode.PRIORITY_ONLY -> NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS or
                        NotificationManager.Policy.PRIORITY_CATEGORY_MEDIA or
                        NotificationManager.Policy.PRIORITY_CATEGORY_SYSTEM,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET
            )
            DndMode.TOTAL_SILENCE -> NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_NONE,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_SCREEN_OFF or
                        NotificationManager.Policy.SUPPRESSED_EFFECTS_SCREEN_ON,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_SCREEN_OFF or
                        NotificationManager.Policy.SUPPRESSED_EFFECTS_SCREEN_ON
            )
            DndMode.ALARMS_ONLY -> NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET,
                NotificationManager.Policy.SUPPRESSED_EFFECTS_UNSET
            )
        }
        nm.setInterruptionFilter(
            when (DndMode.valueOf(mode ?: "OFF")) {
                DndMode.OFF -> NotificationManager.INTERRUPTION_FILTER_ALL
                DndMode.PRIORITY_ONLY -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                DndMode.TOTAL_SILENCE -> NotificationManager.INTERRUPTION_FILTER_NONE
                DndMode.ALARMS_ONLY -> NotificationManager.INTERRUPTION_FILTER_ALARMS
            }
        )
    }

    private fun setVolume(context: Context, action: RoutineAction) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        action.mediaVolume?.let {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, it, 0)
        }
        action.ringVolume?.let {
            am.setStreamVolume(AudioManager.STREAM_RING, it, 0)
        }
        action.alarmVolume?.let {
            am.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
        }
        action.notificationVolume?.let {
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, it, 0)
        }
    }

    private fun setBrightness(context: Context, action: RoutineAction) {
        val brightness = action.brightnessLevel ?: 128
        val auto = action.brightnessAuto ?: false
        if (auto) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
        } else {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness.coerceIn(0, 255)
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun setWifi(context: Context, enabled: Boolean?) {
        if (enabled == null) return
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }

    @Suppress("DEPRECATION")
    private fun setBluetooth(context: Context, enabled: Boolean?) {
        if (enabled == null) return
        val btAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        if (enabled) btAdapter.enable() else btAdapter.disable()
    }

    private fun sendActionNotification(context: Context, action: RoutineAction) {
        val title = action.notificationTitle ?: "Routine"
        val text = action.notificationText ?: ""
        NotificationCompat.Builder(context, RoutineApp.CHANNEL_ROUTINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
            .let { NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), it) }
    }

    private fun sendExecutionNotification(context: Context, routineName: String, actions: List<RoutineAction>) {
        val actionSummary = actions.joinToString(", ") {
            ActionType.fromValue(it.type).name
        }
        NotificationCompat.Builder(context, RoutineApp.CHANNEL_ROUTINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Routine: $routineName")
            .setContentText(actionSummary)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
            .let { NotificationManagerCompat.from(context).notify(Int.MIN_VALUE, it) }
    }
}
