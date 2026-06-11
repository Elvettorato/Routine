package com.elvettorato.routine.data.model

enum class TriggerType(val value: String) {
    TIME("TIME"),
    LOCATION("LOCATION");

    companion object {
        fun fromValue(value: String): TriggerType =
            entries.firstOrNull { it.value == value } ?: TIME
    }
}
