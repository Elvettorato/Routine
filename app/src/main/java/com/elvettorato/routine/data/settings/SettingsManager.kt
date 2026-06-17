package com.elvettorato.routine.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationInterval(
    val label: String,
    val millis: Long
)

object SettingsManager {

    private const val PREFS_NAME = "routine_settings"
    private const val KEY_LOCATION_INTERVAL = "location_interval_ms"

    val intervals = listOf(
        LocationInterval("1 min", 60_000L),
        LocationInterval("2 min", 120_000L),
        LocationInterval("5 min", 300_000L),
        LocationInterval("10 min", 600_000L),
        LocationInterval("15 min", 900_000L),
        LocationInterval("30 min", 1_800_000L),
    )

    private val _locationIntervalMs = MutableStateFlow(300_000L)
    val locationIntervalMs: StateFlow<Long> = _locationIntervalMs.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _locationIntervalMs.value = prefs.getLong(KEY_LOCATION_INTERVAL, 300_000L)
    }

    fun setLocationIntervalMs(context: Context, millis: Long) {
        _locationIntervalMs.value = millis
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LOCATION_INTERVAL, millis)
            .apply()
    }
}
