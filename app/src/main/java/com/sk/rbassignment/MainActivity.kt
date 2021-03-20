package com.sk.rbassignment

import android.Manifest.permission
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.sk.rbassignment.services.SyncService
import com.sk.rbassignment.utils.MyRuntimePermission
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

    private var mServiceIntent: Intent? = null
    private var mSyncService: SyncService? = null
    private var buttonStartStop: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        buttonStartStop = findViewById(R.id.buttonStartStop)
        setUpViewListener()
    }

    private fun setUpViewListener() {
        mSyncService = SyncService()
        mServiceIntent = Intent(this, mSyncService?.javaClass)
        if (!isMyServiceRunning(mSyncService?.javaClass!!)) {
            buttonStartStop?.text = getString(R.string.hint_start)
        } else {
            buttonStartStop?.text = getString(R.string.hint_stop)
        }

        buttonStartStop?.setOnClickListener {
            if (!isMyServiceRunning(mSyncService?.javaClass!!)) {
                if (ActivityCompat.checkSelfPermission(this,
                        permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startService(mServiceIntent)
                    buttonStartStop?.text = getString(R.string.hint_stop)
                } else {
                    MyRuntimePermission(this).checkPermissionForExternalStorage()
                }
            } else {
                stopService(mServiceIntent)
                buttonStartStop?.text = getString(R.string.hint_start)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(mServiceIntent)
                buttonStartStop?.text = getString(R.string.hint_stop)
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
