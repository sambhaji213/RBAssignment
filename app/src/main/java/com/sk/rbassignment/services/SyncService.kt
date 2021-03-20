package com.sk.rbassignment.services

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.sk.rbassignment.activity.MainActivity
import com.sk.rbassignment.R
import com.sk.rbassignment.model.FieldDetails
import com.sk.rbassignment.retrofit.ApiInterface
import com.sk.rbassignment.retrofit.ServiceGenerator
import com.sk.rbassignment.utils.Actions
import com.sk.rbassignment.utils.ServiceState
import com.sk.rbassignment.utils.log
import com.sk.rbassignment.utils.setServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.IOException

class SyncService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".toUpperCase())
        val notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
        Toast.makeText(this, "SyncService destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, SyncService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task")
        Toast.makeText(this, "SyncService starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SyncService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    fetchFieldDetails()
                }
                delay(2 * 60 * 1000)
            }
            log("End of the loop for the service")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun fetchFieldDetails() {
        val apiInterface = ServiceGenerator.callAPICreateService(ApiInterface::class.java)
        apiInterface.getFieldsDetails("61439")
            .enqueue(object : Callback<FieldDetails?> {
                override fun onResponse(call: Call<FieldDetails?>, response: Response<FieldDetails?>) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.i("**RESULT***", response.body()?.query ?: "")
                        val body = System.currentTimeMillis().toString() + ":" + response.body()?.query
                        val fileName = "rb_ip_file.txt"
                        try {
                            val root = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + File.separator + "RB_Folder"
                            )
                            if (!root.exists()) {
                                root.mkdirs()
                            }
                            val gpxfile = File(root, fileName)
                            val writer = FileWriter(gpxfile, true)
                            writer.append(body + "\n")
                            writer.flush()
                            writer.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<FieldDetails?>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "SYNCSERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "SyncService notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "SyncService channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("SyncService")
            .setContentText("This is your favorite sync service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}