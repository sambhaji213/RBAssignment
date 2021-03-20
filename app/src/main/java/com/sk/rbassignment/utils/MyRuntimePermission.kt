package com.sk.rbassignment.utils

import android.Manifest.permission
import android.app.Activity
import androidx.core.app.ActivityCompat

class MyRuntimePermission(private val context: Activity) {

    fun checkPermissionForExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission.WRITE_EXTERNAL_STORAGE)
        ) {
            ActivityCompat
                .requestPermissions(
                    context, arrayOf(permission.WRITE_EXTERNAL_STORAGE, permission.CAMERA),
                    PERMISSION_EXTERNAL_STORAGE
                )
        } else {
            ActivityCompat
                .requestPermissions(
                    context, arrayOf(permission.WRITE_EXTERNAL_STORAGE, permission.CAMERA),
                    PERMISSION_EXTERNAL_STORAGE
                )
        }
    }

    companion object {

        private const val PERMISSION_EXTERNAL_STORAGE = 1
    }
}