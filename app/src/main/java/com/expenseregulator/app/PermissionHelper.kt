package com.expenseregulator.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing SMS permissions.
 */
object PermissionHelper {
    private const val TAG = "ExpenseRegulator/PermissionHelper"

    // Permission request codes
    const val SMS_PERMISSION_REQUEST_CODE = 1001

    // Required permissions
    private val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    /**
     * Checks if all SMS permissions are granted.
     */
    fun areSmsPermissionsGranted(context: Context): Boolean {
        for (permission in SMS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted: $permission")
                return false
            }
        }
        Log.d(TAG, "All SMS permissions granted")
        return true
    }

    /**
     * Requests SMS permissions from the user.
     */
    fun requestSmsPermissions(activity: Activity) {
        Log.d(TAG, "Requesting SMS permissions...")
        ActivityCompat.requestPermissions(
            activity,
            SMS_PERMISSIONS,
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Checks if the user should see a rationale for why the permission is needed.
     */
    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        for (permission in SMS_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * Handles the permission request result.
     * @param grantResults The permission grant results
     * @return true if all permissions were granted, false otherwise
     */
    fun handlePermissionResult(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            Log.w(TAG, "Permission result was empty")
            return false
        }

        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission was denied")
                return false
            }
        }

        Log.d(TAG, "All permissions granted")
        return true
    }

    /**
     * Checks if the user has permanently denied the permission.
     */
    fun isPermissionPermanentlyDenied(activity: Activity): Boolean {
        for (permission in SMS_PERMISSIONS) {
            val isGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

            if (!isGranted && !shouldShowRationale) {
                Log.d(TAG, "Permission permanently denied: $permission")
                return true
            }
        }
        return false
    }

    /**
     * Gets the missing permissions (not yet granted).
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()
        for (permission in SMS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission)
            }
        }
        return missing
    }
}
