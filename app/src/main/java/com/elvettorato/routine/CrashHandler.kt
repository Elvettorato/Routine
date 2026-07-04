package com.elvettorato.routine

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val app: RoutineApp) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashLog(thread, throwable)
            showCrashNotification(throwable)
        } catch (_: Exception) {
        }
        defaultHandler?.uncaughtException(thread, throwable) ?: Process.killProcess(Process.myPid())
    }

    private fun saveCrashLog(thread: Thread, throwable: Throwable) {
        val dir = File(app.filesDir, "crash_logs")
        dir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "crash_$timestamp.txt")
        val sb = StringBuilder()
        sb.appendLine("=== CRASH REPORT ===")
        sb.appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        sb.appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("Thread: ${thread.name} (${thread.id})")
        sb.appendLine()
        sb.appendLine("${throwable.javaClass.name}: ${throwable.message}")
        for (element in throwable.stackTrace) {
            sb.appendLine("\tat $element")
        }
        throwable.cause?.let { cause ->
            sb.appendLine("Caused by: ${cause.javaClass.name}: ${cause.message}")
            for (element in cause.stackTrace) {
                sb.appendLine("\tat $element")
            }
        }
        file.writeText(sb.toString())
    }

    private fun showCrashNotification(throwable: Throwable) {
        val intent = Intent(app, CrashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            app, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(app, RoutineApp.CHANNEL_CRASH)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(app.getString(R.string.crash_notification_title))
            .setContentText(throwable.message ?: throwable.javaClass.simpleName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(CRASH_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CRASH_NOTIFICATION_ID = 9001
    }
}
