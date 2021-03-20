package com.sk.rbassignment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sk.rbassignment.services.SyncService
import com.sk.rbassignment.utils.Actions
import com.sk.rbassignment.utils.ServiceState
import com.sk.rbassignment.utils.getServiceState
import com.sk.rbassignment.utils.log

class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, SyncService::class.java).also {
                it.action = Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log("Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                log("Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }
}