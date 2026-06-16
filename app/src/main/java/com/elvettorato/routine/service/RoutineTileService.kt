package com.elvettorato.routine.service

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.elvettorato.routine.MainActivity
import com.elvettorato.routine.data.RoutineDatabase
import com.elvettorato.routine.data.repository.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: RoutineRepository

    override fun onCreate() {
        super.onCreate()
        val db = RoutineDatabase.getDatabase(this)
        repository = RoutineRepository(db.routineDao())
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile() {
        scope.launch {
            try {
                val count = repository.getEnabledRoutinesCount()
                val tile = qsTile ?: return@launch
                if (count > 0) {
                    tile.state = Tile.STATE_ACTIVE
                    tile.label = "$count active"
                } else {
                    tile.state = Tile.STATE_INACTIVE
                    tile.label = getString(com.elvettorato.routine.R.string.tile_label)
                }
                tile.updateTile()
            } catch (_: Exception) {
            }
        }
    }
}
