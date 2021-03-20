package com.sk.rbassignment.services

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.sk.rbassignment.model.FieldDetails
import com.sk.rbassignment.receivers.SyncReceiver
import com.sk.rbassignment.retrofit.ApiInterface
import com.sk.rbassignment.retrofit.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class SyncService : Service() {

    var counter = 0
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    init {
        Log.i("HERE", "here I am!")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("EXIT", "ondestroy!")
        val broadcastIntent = Intent(this, SyncReceiver::class.java)
        sendBroadcast(broadcastIntent)
        stopTimerTask()
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    private fun initializeTimerTask() {
        timerTask = object : TimerTask() {
            override fun run() {
                Log.i("in timer", "in timer ++++  " + counter++)
                fetchFieldDetails()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        //set a new Timer
        timer = Timer()

        //initialize the TimerTask's job
        initializeTimerTask()

        //schedule the timer, to wake up every 2 minute
        timer?.schedule(timerTask, (1000 * 60 * 1).toLong(), (1000 * 60 * 2).toLong())
    }

    /**
     * not needed
     */
    private fun stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
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
}