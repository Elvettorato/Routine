package com.elvettorato.routine.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.model.TriggerType
import com.elvettorato.routine.receiver.TimeBroadcastReceiver
import java.util.Calendar

object RoutineScheduler {

    fun schedule(context: Context, routine: Routine) {
        when (routine.triggerType) {
            TriggerType.TIME -> scheduleTimeBased(context, routine)
            TriggerType.LOCATION -> scheduleLocationBased(context, routine)
        }
    }

    fun cancel(context: Context, routine: Routine) {
        val workName = "routine_${routine.id}"
        WorkManager.getInstance(context).cancelUniqueWork(workName)

        val intent = Intent(context, TimeBroadcastReceiver::class.java).apply {
            putExtra("routine_id", routine.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        if (routine.triggerType == TriggerType.LOCATION) {
            GeofenceHelper.removeGeofence(context, routine)
        }
    }

    private fun scheduleTimeBased(context: Context, routine: Routine) {
        val hour = routine.triggerHour ?: return
        val minute = routine.triggerMinute ?: return
        val days = routine.triggerDaysOfWeek

        if (days.isNullOrEmpty()) {
            scheduleDaily(context, routine, hour, minute)
        } else {
            days.forEach { day ->
                scheduleWeekly(context, routine, hour, minute, day)
            }
        }
    }

    private fun scheduleDaily(context: Context, routine: Routine, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, TimeBroadcastReceiver::class.java).apply {
            putExtra("routine_id", routine.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun scheduleWeekly(context: Context, routine: Routine, hour: Int, minute: Int, dayOfWeek: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val targetDay = dayOfWeek % 7
            val currentDay = get(Calendar.DAY_OF_WEEK) - 1
            var diff = targetDay - currentDay
            if (diff <= 0 || (diff == 0 && before(Calendar.getInstance()))) {
                diff += 7
            }
            add(Calendar.DAY_OF_YEAR, diff)
        }

        val intent = Intent(context, TimeBroadcastReceiver::class.java).apply {
            putExtra("routine_id", routine.id)
            putExtra("day_of_week", dayOfWeek)
        }
        val requestCode = (routine.id * 10 + dayOfWeek).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun scheduleLocationBased(context: Context, routine: Routine) {
        GeofenceHelper.addGeofence(context, routine)
    }
}
