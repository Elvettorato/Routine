package com.elvettorato.routine.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elvettorato.routine.R
import com.elvettorato.routine.RoutineApp
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.repository.RoutineRepository
import com.elvettorato.routine.data.settings.SettingsManager
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RoutineForegroundService : Service() {

    private var monitoringLocation = false
    private var locationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val insideZones = mutableSetOf<Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: monitorLocation=${intent?.getBooleanExtra(EXTRA_MONITOR_LOCATION, false)}")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        val monitorLocation = intent?.getBooleanExtra(EXTRA_MONITOR_LOCATION, false) ?: false

        if (monitorLocation && !monitoringLocation) {
            startLocationMonitoring()
        } else if (!monitorLocation && monitoringLocation) {
            stopLocationMonitoring()
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        stopLocationMonitoring()
        scope.cancel()
        super.onDestroy()
    }

    private fun startLocationMonitoring() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Cannot monitor location — missing permission")
            return
        }
        monitoringLocation = true
        Log.i(TAG, "Starting location polling")
        locationJob = scope.launch {
            while (isActive) {
                try {
                    val client = LocationServices.getFusedLocationProviderClient(this@RoutineForegroundService)
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build()
                    val intervalMs = SettingsManager.locationIntervalMs.value
                    val location = Tasks.await(
                        client.getCurrentLocation(request, CancellationTokenSource().token),
                        intervalMs,
                        TimeUnit.MILLISECONDS
                    )
                    if (location != null) {
                        onLocationUpdate(location)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "getCurrentLocation failed: ${e.message}")
                }
                delay(SettingsManager.locationIntervalMs.value)
            }
        }
    }

    private fun stopLocationMonitoring() {
        if (!monitoringLocation) return
        monitoringLocation = false
        locationJob?.cancel()
        locationJob = null
        insideZones.clear()
        Log.i(TAG, "Location polling stopped")
    }

    private suspend fun onLocationUpdate(location: Location) {
        Log.i(TAG, "Location: ${location.latitude}, ${location.longitude} (acc: ${location.accuracy})")
        try {
            val db = RoutineDatabase.getDatabase(this)
            val repository = RoutineRepository(db.routineDao())
            val routines = repository.getAllRoutinesOnce().filter { it.isEnabled && it.hasLocationTrigger }
            val results = FloatArray(1)

            for (routine in routines) {
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    routine.triggerLatitude!!, routine.triggerLongitude!!,
                    results
                )
                val isInside = results[0] <= (routine.triggerRadius ?: 100f)
                val wasInside = insideZones.contains(routine.id)
                Log.d(TAG, "Routine ${routine.id}: dist=${results[0]}m, inside=$isInside, wasInside=$wasInside")

                if (isInside && !wasInside && routine.triggerOnEnter) {
                    Log.i(TAG, "Enter zone → triggering routine ${routine.id}")
                    ActionExecutor.execute(this, routine.actions, routine.name)
                } else if (!isInside && wasInside && routine.triggerOnExit) {
                    Log.i(TAG, "Exit zone → triggering routine ${routine.id}")
                    ActionExecutor.execute(this, routine.actions, routine.name)
                }

                if (isInside) insideZones.add(routine.id) else insideZones.remove(routine.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing location", e)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, RoutineApp.CHANNEL_LOCATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.foreground_service_active))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val TAG = "RoutineFgService"
        const val NOTIFICATION_ID = 1
        const val EXTRA_MONITOR_LOCATION = "monitor_location"
    }
}
