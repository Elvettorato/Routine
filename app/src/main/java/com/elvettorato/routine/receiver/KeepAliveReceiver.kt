package com.elvettorato.routine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvettorato.routine.service.RoutineScheduler

class KeepAliveReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        RoutineScheduler.restartServiceIfNeeded(context)
    }
}
