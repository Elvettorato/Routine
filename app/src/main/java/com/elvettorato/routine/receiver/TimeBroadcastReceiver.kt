package com.elvettorato.routine.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.service.ActionExecutor
import com.elvettorato.routine.service.RoutineScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getLongExtra("routine_id", -1L)
        if (routineId == -1L) return

        val dayOfWeek = intent.getIntExtra("day_of_week", -1)

        CoroutineScope(Dispatchers.IO).launch {
            val db = RoutineDatabase.getDatabase(context)
            val repository = RoutineRepository(db.routineDao())
            val routine = repository.getRoutineByIdOnce(routineId)

            if (routine != null && routine.isEnabled) {
                ActionExecutor.execute(context, routine.actions, routine.name)

                val days = routine.triggerDaysOfWeek
                if (days.isNullOrEmpty()) {
                    RoutineScheduler.schedule(context, routine)
                } else if (dayOfWeek != -1) {
                    scheduleNextWeekly(context, routine, dayOfWeek)
                }

                RoutineScheduler.restartServiceIfNeeded(context)
            }
        }
    }

    private fun scheduleNextWeekly(context: Context, routine: Routine, completedDay: Int) {
        val days = routine.triggerDaysOfWeek ?: return
        val hour = routine.triggerHour ?: return
        val minute = routine.triggerMinute ?: return

        val nextDay = days.firstOrNull { it > completedDay }
            ?: days.firstOrNull() ?: return

        RoutineScheduler.schedule(context, routine.copy(
            triggerDaysOfWeek = listOf(nextDay)
        ))
    }
}
