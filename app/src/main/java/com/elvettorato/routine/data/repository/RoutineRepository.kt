package com.elvettorato.routine.data.repository

import com.elvettorato.routine.data.dao.RoutineDao
import com.elvettorato.routine.data.entity.RoutineEntity
import com.elvettorato.routine.data.model.Routine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepository(private val dao: RoutineDao) {

    val allRoutines: Flow<List<Routine>> = dao.getAllRoutines().map { entities ->
        entities.map { Routine.fromEntity(it) }
    }

    fun getRoutineById(id: Long): Flow<Routine?> =
        dao.getRoutineByIdFlow(id).map { it?.let { Routine.fromEntity(it) } }

    suspend fun getRoutineByIdOnce(id: Long): Routine? =
        dao.getRoutineById(id)?.let { Routine.fromEntity(it) }

    suspend fun getEnabledRoutines(): List<Routine> =
        dao.getEnabledRoutines().map { Routine.fromEntity(it) }

    suspend fun getEnabledRoutinesCount(): Int =
        dao.getEnabledRoutinesCount()

    suspend fun insert(routine: Routine): Long {
        val entity = Routine.toEntity(routine).copy(updatedAt = System.currentTimeMillis())
        return dao.insertRoutine(entity)
    }

    suspend fun update(routine: Routine) {
        val entity = Routine.toEntity(routine).copy(updatedAt = System.currentTimeMillis())
        dao.updateRoutine(entity)
    }

    suspend fun delete(routine: Routine) {
        dao.deleteRoutine(Routine.toEntity(routine))
    }

    suspend fun deleteById(id: Long) {
        dao.deleteRoutineById(id)
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.setRoutineEnabled(id, enabled)
    }
}
