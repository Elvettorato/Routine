package com.elvettorato.routine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.service.RoutineForegroundService
import com.elvettorato.routine.service.RoutineScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = RoutineDatabase.getDatabase(context)
            val repository = RoutineRepository(db.routineDao())
            val routines = repository.getEnabledRoutines()

            routines.forEach { routine ->
                RoutineScheduler.schedule(context, routine)
            }

            if (routines.isNotEmpty()) {
                val fgsIntent = Intent(context, RoutineForegroundService::class.java)
                context.startForegroundService(fgsIntent)
            }
        }
    }
}
