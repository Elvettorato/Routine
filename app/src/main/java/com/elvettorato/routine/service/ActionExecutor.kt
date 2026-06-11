package com.elvettorato.routine.service

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elvettorato.routine.R
import com.elvettorato.routine.RoutineApp
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.elvettorato.routine.data.model.ActionType
import com.elvettorato.routine.data.model.DndMode
import com.elvettorato.routine.data.model.NotificationPriority
import com.elvettorato.routine.data.model.RoutineAction

object ActionExecutor {

    fun execute(context: Context, actions: List<RoutineAction>, routineName: String) {
        actions.forEach { action -> executeSingle(context, action) }
        val hasNotificationAction = actions.any { ActionType.fromValue(it.type) == ActionType.NOTIFICATION }
        if (!hasNotificationAction) {
            sendExecutionNotification(context, routineName, actions)
        }
    }

    private fun executeSingle(context: Context, action: RoutineAction) {
        try {
            when (ActionType.fromValue(action.type)) {
                ActionType.DND -> setDndMode(context, action.dndMode)
                ActionType.VOLUME -> setVolume(context, action)
                ActionType.BRIGHTNESS -> setBrightness(context, action)
                ActionType.WIFI -> setWifi(context, action.wifiEnabled)
                ActionType.BLUETOOTH -> setBluetooth(context, action.bluetoothEnabled)
                ActionType.NOTIFICATION -> sendActionNotification(context, action)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied for ${action.type}: ${e.message}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to execute ${action.type}: ${e.message}")
        }
    }

    private fun setDndMode(context: Context, mode: String?) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "DND not granted - requires notification policy access")
            return
        }
        val dnd = DndMode.valueOf(mode ?: "OFF")
        val filter = when (dnd) {
            DndMode.OFF -> NotificationManager.INTERRUPTION_FILTER_ALL
            DndMode.PRIORITY_ONLY -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
            DndMode.TOTAL_SILENCE -> NotificationManager.INTERRUPTION_FILTER_NONE
            DndMode.ALARMS_ONLY -> NotificationManager.INTERRUPTION_FILTER_ALARMS
        }
        nm.setInterruptionFilter(filter)
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
        val cr = context.contentResolver
        if (auto) {
            Settings.System.putInt(
                cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
        } else {
            try {
                Settings.System.putInt(
                    cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    cr, Settings.System.SCREEN_BRIGHTNESS,
                    brightness.coerceIn(0, 255)
                )
            } catch (e: SecurityException) {
                Log.w(TAG, "Brightness write blocked on this Android version")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setWifi(context: Context, enabled: Boolean?) {
        if (enabled == null) return
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        try {
            wifiManager.isWifiEnabled = enabled
        } catch (e: SecurityException) {
            Log.w(TAG, "WiFi toggle requires CHANGE_WIFI_STATE and location permission")
        } catch (e: Exception) {
            Log.w(TAG, "WiFi toggle not supported on this Android version")
        }
    }

    @Suppress("DEPRECATION")
    private fun setBluetooth(context: Context, enabled: Boolean?) {
        if (enabled == null) return
        val btAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        if (enabled) btAdapter.enable() else btAdapter.disable()
    }

    private const val TAG = "ActionExecutor"

    private fun sendActionNotification(context: Context, action: RoutineAction) {
        val title = action.notificationTitle ?: "Routine"
        val text = action.notificationText ?: ""
        val priority = when (action.notificationPriority) {
            NotificationPriority.HIGH.value -> PRIORITY_HIGH
            NotificationPriority.DEFAULT.value -> PRIORITY_DEFAULT
            NotificationPriority.LOW.value -> PRIORITY_LOW
            NotificationPriority.MIN.value -> PRIORITY_MIN
            else -> PRIORITY_HIGH
        }
        NotificationCompat.Builder(context, RoutineApp.CHANNEL_ROUTINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(priority)
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
            .setPriority(PRIORITY_HIGH)
            .build()
            .let { NotificationManagerCompat.from(context).notify(Int.MIN_VALUE, it) }
    }
}
