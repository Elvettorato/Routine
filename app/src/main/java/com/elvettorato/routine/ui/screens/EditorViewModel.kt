package com.elvettorato.routine.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.model.Routine
import com.elvettorato.routine.data.model.RoutineAction
import com.elvettorato.routine.data.model.TriggerType
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.service.RoutineScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository

    private val _routineName = MutableStateFlow("")
    val routineName: StateFlow<String> = _routineName.asStateFlow()

    private val _triggerType = MutableStateFlow(TriggerType.TIME)
    val triggerType: StateFlow<TriggerType> = _triggerType.asStateFlow()

    private val _triggerHour = MutableStateFlow(8)
    val triggerHour: StateFlow<Int> = _triggerHour.asStateFlow()

    private val _triggerMinute = MutableStateFlow(0)
    val triggerMinute: StateFlow<Int> = _triggerMinute.asStateFlow()

    private val _triggerDays = MutableStateFlow(listOf<Int>())
    val triggerDays: StateFlow<List<Int>> = _triggerDays.asStateFlow()

    private val _triggerLat = MutableStateFlow(0.0)
    val triggerLat: StateFlow<Double> = _triggerLat.asStateFlow()

    private val _triggerLng = MutableStateFlow(0.0)
    val triggerLng: StateFlow<Double> = _triggerLng.asStateFlow()

    private val _triggerRadius = MutableStateFlow(100f)
    val triggerRadius: StateFlow<Float> = _triggerRadius.asStateFlow()

    private val _triggerOnEnter = MutableStateFlow(true)
    val triggerOnEnter: StateFlow<Boolean> = _triggerOnEnter.asStateFlow()

    private val _triggerOnExit = MutableStateFlow(false)
    val triggerOnExit: StateFlow<Boolean> = _triggerOnExit.asStateFlow()

    private val _actions = MutableStateFlow<List<RoutineAction>>(emptyList())
    val actions: StateFlow<List<RoutineAction>> = _actions.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    private var editingId: Long? = null

    init {
        val db = RoutineDatabase.getDatabase(application)
        repository = RoutineRepository(db.routineDao())
    }

    fun loadRoutine(id: Long) {
        viewModelScope.launch {
            val routine = repository.getRoutineByIdOnce(id)
            if (routine != null) {
                editingId = routine.id
                _routineName.value = routine.name
                _triggerType.value = routine.triggerType
                _triggerHour.value = routine.triggerHour ?: 8
                _triggerMinute.value = routine.triggerMinute ?: 0
                _triggerDays.value = routine.triggerDaysOfWeek ?: emptyList()
                _triggerLat.value = routine.triggerLatitude ?: 0.0
                _triggerLng.value = routine.triggerLongitude ?: 0.0
                _triggerRadius.value = routine.triggerRadius ?: 100f
                _triggerOnEnter.value = routine.triggerOnEnter
                _triggerOnExit.value = routine.triggerOnExit
                _actions.value = routine.actions
            }
        }
    }

    fun updateName(name: String) { _routineName.value = name }
    fun updateTriggerType(type: TriggerType) { _triggerType.value = type }
    fun updateTriggerHour(hour: Int) { _triggerHour.value = hour }
    fun updateTriggerMinute(minute: Int) { _triggerMinute.value = minute }
    fun updateTriggerDays(days: List<Int>) { _triggerDays.value = days }
    fun updateLat(lat: Double) { _triggerLat.value = lat }
    fun updateLng(lng: Double) { _triggerLng.value = lng }
    fun updateRadius(radius: Float) { _triggerRadius.value = radius }
    fun updateOnEnter(onEnter: Boolean) { _triggerOnEnter.value = onEnter }
    fun updateOnExit(onExit: Boolean) { _triggerOnExit.value = onExit }

    fun addAction(action: RoutineAction) {
        _actions.value = _actions.value + action
    }

    fun removeAction(index: Int) {
        _actions.value = _actions.value.toMutableList().also { it.removeAt(index) }
    }

    fun save() {
        viewModelScope.launch {
            _isSaving.value = true
            val routine = Routine(
                id = editingId ?: 0,
                name = _routineName.value.ifBlank { "Unnamed" },
                isEnabled = true,
                triggerType = _triggerType.value,
                triggerHour = if (_triggerType.value == TriggerType.TIME) _triggerHour.value else null,
                triggerMinute = if (_triggerType.value == TriggerType.TIME) _triggerMinute.value else null,
                triggerDaysOfWeek = if (_triggerType.value == TriggerType.TIME) _triggerDays.value.ifEmpty { null } else null,
                triggerLatitude = if (_triggerType.value == TriggerType.LOCATION) _triggerLat.value else null,
                triggerLongitude = if (_triggerType.value == TriggerType.LOCATION) _triggerLng.value else null,
                triggerRadius = if (_triggerType.value == TriggerType.LOCATION) _triggerRadius.value else null,
                triggerOnEnter = _triggerOnEnter.value,
                triggerOnExit = _triggerOnExit.value,
                actions = _actions.value,
                createdAt = if (editingId != null) 0L else System.currentTimeMillis()
            )

            val ctx = getApplication<Application>()
            if (editingId != null) {
                RoutineScheduler.cancel(ctx, routine)
                repository.update(routine)
            } else {
                val newId = repository.insert(routine)
                routine.copy(id = newId).let { RoutineScheduler.schedule(ctx, it) }
            }
            _isSaving.value = false
            _saveComplete.value = true
        }
    }
}
