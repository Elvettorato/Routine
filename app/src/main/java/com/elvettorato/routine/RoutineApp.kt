package com.elvettorato.routine

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat

import com.elvettorato.routine.data.settings.SettingsManager

class RoutineApp : Application() {
    companion object {
        const val CHANNEL_ROUTINE = "routine_actions"
        const val CHANNEL_LOCATION = "routine_location"
        const val CHANNEL_CRASH = "routine_crash"
    }

    override fun onCreate() {
        super.onCreate()
        SettingsManager.init(this)
        createNotificationChannels()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_ROUTINE,
                "Routine Actions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from routine triggers"
            },
            NotificationChannel(
                CHANNEL_LOCATION,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service for location monitoring"
            },
            NotificationChannel(
                CHANNEL_CRASH,
                "Crash Reports",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "App crash notifications"
            }
        )
        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
