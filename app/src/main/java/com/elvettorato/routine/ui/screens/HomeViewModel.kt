package com.elvettorato.routine.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.service.RoutineScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository

    val routines: StateFlow<List<Routine>>

    init {
        val db = RoutineDatabase.getDatabase(application)
        repository = RoutineRepository(db.routineDao())
        routines = repository.allRoutines.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
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
}
