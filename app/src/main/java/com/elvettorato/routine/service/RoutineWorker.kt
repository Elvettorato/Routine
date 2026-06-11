package com.elvettorato.routine.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.repository.RoutineRepository

class RoutineWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val routineId = inputData.getLong("routine_id", -1L)
        if (routineId == -1L) return Result.failure()

        val db = RoutineDatabase.getDatabase(applicationContext)
        val repository = RoutineRepository(db.routineDao())
        val routine = repository.getRoutineByIdOnce(routineId) ?: return Result.failure()

        if (!routine.isEnabled) return Result.success()

        ActionExecutor.execute(applicationContext, routine.actions, routine.name)
        reschedule(applicationContext, routine)
        return Result.success()
    }

    companion object {
        fun reschedule(context: Context, routine: Routine) {
            val workName = "routine_${routine.id}"
            androidx.work.WorkManager.getInstance(context).cancelUniqueWork(workName)
            RoutineScheduler.schedule(context, routine)
        }
    }
}
