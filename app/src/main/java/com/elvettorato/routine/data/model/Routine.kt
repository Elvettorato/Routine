package com.elvettorato.routine.data.model

import com.elvettorato.routine.data.entity.RoutineEntity

data class Routine(
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val triggerHour: Int? = null,
    val triggerMinute: Int? = null,
    val triggerDaysOfWeek: List<Int>? = null,
    val triggerLatitude: Double? = null,
    val triggerLongitude: Double? = null,
    val triggerRadius: Float? = null,
    val triggerOnEnter: Boolean = true,
    val triggerOnExit: Boolean = false,
    val actions: List<RoutineAction> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val hasTimeTrigger: Boolean get() = triggerHour != null && triggerMinute != null
    val hasLocationTrigger: Boolean get() = triggerLatitude != null && triggerLongitude != null
            && triggerLatitude != 0.0 && triggerLongitude != 0.0

    companion object {
        fun fromEntity(entity: RoutineEntity): Routine {
            val daysOfWeek = entity.triggerDaysOfWeek
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }

            return Routine(
                id = entity.id,
                name = entity.name,
                isEnabled = entity.isEnabled,
                triggerHour = entity.triggerHour,
                triggerMinute = entity.triggerMinute,
                triggerDaysOfWeek = daysOfWeek,
                triggerLatitude = entity.triggerLatitude,
                triggerLongitude = entity.triggerLongitude,
                triggerRadius = entity.triggerRadius,
                triggerOnEnter = entity.triggerOnEnter,
                triggerOnExit = entity.triggerOnExit,
                actions = RoutineAction.fromJson(entity.actionsJson),
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }

        fun toEntity(routine: Routine): RoutineEntity {
            return RoutineEntity(
                id = routine.id,
                name = routine.name,
                isEnabled = routine.isEnabled,
                triggerHour = routine.triggerHour,
                triggerMinute = routine.triggerMinute,
                triggerDaysOfWeek = routine.triggerDaysOfWeek?.joinToString(","),
                triggerLatitude = routine.triggerLatitude,
                triggerLongitude = routine.triggerLongitude,
                triggerRadius = routine.triggerRadius,
                triggerOnEnter = routine.triggerOnEnter,
                triggerOnExit = routine.triggerOnExit,
                actionsJson = RoutineAction.toJson(routine.actions),
                createdAt = routine.createdAt,
                updatedAt = routine.updatedAt
            )
        }
    }
}
