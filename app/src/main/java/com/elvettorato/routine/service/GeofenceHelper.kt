package com.elvettorato.routine.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.elvettorato.routine.data.model.Routine
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {
    private const val TAG = "GeofenceHelper"

    fun addGeofence(context: Context, routine: Routine) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Missing location permission for routine ${routine.id}")
            return
        }

        val lat = routine.triggerLatitude ?: return
        val lng = routine.triggerLongitude ?: return
        val radius = routine.triggerRadius ?: 100f

        val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

        val geofence = Geofence.Builder()
            .setRequestId(routine.id.toString())
            .setCircularRegion(lat, lng, radius)
            .setTransitionTypes(
                (if (routine.triggerOnEnter) Geofence.GEOFENCE_TRANSITION_ENTER else 0) or
                        (if (routine.triggerOnExit) Geofence.GEOFENCE_TRANSITION_EXIT else 0)
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val pendingIntent = getGeofencePendingIntent(context, routine.id)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added for routine ${routine.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add geofence for routine ${routine.id}: ${e.message}")
            }
    }

    fun removeGeofence(context: Context, routine: Routine) {
        val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
        geofencingClient.removeGeofences(listOf(routine.id.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence removed for routine ${routine.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence for routine ${routine.id}: ${e.message}")
            }
    }

    private fun getGeofencePendingIntent(context: Context, routineId: Long): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            routineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
