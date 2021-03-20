package com.sk.rbassignment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sk.rbassignment.services.SyncService

class SyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(SyncReceiver::class.java.simpleName, "Service Stops!!")
        context.startService(Intent(context, SyncService::class.java))
    }
}