package com.elvettorato.routine.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.repository.RoutineRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val db = RoutineDatabase.getDatabase(context)
            val repository = RoutineRepository(db.routineDao())

            triggeringGeofences.forEach { geofence ->
                val routineId = geofence.requestId.toLongOrNull() ?: return@forEach
                val routine = repository.getRoutineByIdOnce(routineId) ?: return@forEach
                if (!routine.isEnabled) return@forEach

                val shouldTrigger = when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> routine.triggerOnEnter
                    Geofence.GEOFENCE_TRANSITION_EXIT -> routine.triggerOnExit
                    else -> false
                }

                if (shouldTrigger) {
                    ActionExecutor.execute(context, routine.actions, routine.name)
                }
            }
        }
    }
}
