package com.elvettorato.routine.ui.screens

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.service.ActionExecutor
import com.elvettorato.routine.service.RoutineForegroundService
import com.elvettorato.routine.service.RoutineScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository

    val routines: StateFlow<List<Routine>>

    init {
        val ctx = application
        val db = RoutineDatabase.getDatabase(ctx)
        repository = RoutineRepository(db.routineDao())
        routines = repository.allRoutines.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        viewModelScope.launch {
            repository.allRoutines.collect { list ->
                val hasEnabled = list.any { it.isEnabled }
                val hasLocation = list.any { it.isEnabled && it.hasLocationTrigger }
                val intent = Intent(ctx, RoutineForegroundService::class.java).apply {
                    putExtra(RoutineForegroundService.EXTRA_MONITOR_LOCATION, hasLocation)
                }
                if (hasEnabled) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.stopService(intent)
                }
            }
        }
    }

    fun toggleRoutine(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(id, enabled)
            val routine = repository.getRoutineByIdOnce(id)
            if (routine != null) {
                if (enabled) {
                    RoutineScheduler.schedule(getApplication(), routine)
                } else {
                    RoutineScheduler.cancel(getApplication(), routine)
                }
            }
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            val routine = repository.getRoutineByIdOnce(id)
            if (routine != null) {
                RoutineScheduler.cancel(getApplication(), routine)
                repository.deleteById(id)
            }
        }
    }

    fun runNow(id: Long) {
        viewModelScope.launch {
            val routine = repository.getRoutineByIdOnce(id) ?: return@launch
            if (routine.actions.isEmpty()) return@launch
            ActionExecutor.execute(getApplication(), routine.actions, routine.name)
        }
    }
}
