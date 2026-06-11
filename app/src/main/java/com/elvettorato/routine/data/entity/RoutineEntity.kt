package com.elvettorato.routine.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val triggerHour: Int? = null,
    val triggerMinute: Int? = null,
    val triggerDaysOfWeek: String? = null,
    val triggerLatitude: Double? = null,
    val triggerLongitude: Double? = null,
    val triggerRadius: Float? = null,
    val triggerOnEnter: Boolean = true,
    val triggerOnExit: Boolean = false,
    val actionsJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
