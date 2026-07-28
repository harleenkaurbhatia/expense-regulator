package com.expenseregulator.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing SMS permissions.
 */
object PermissionHelper {
    private const val TAG = "ExpenseRegulator/PermissionHelper"

    // Permission request codes
    const val SMS_PERMISSION_REQUEST_CODE = 1001

    // Required permissions (requested separately for better compatibility)
    private val READ_SMS_PERMISSION = Manifest.permission.READ_SMS
    private val RECEIVE_SMS_PERMISSION = Manifest.permission.RECEIVE_SMS

    /**
     * Checks if all SMS permissions are granted.
     */
    fun areSmsPermissionsGranted(context: Context): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(context, READ_SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED
        val receiveGranted = ContextCompat.checkSelfPermission(context, RECEIVE_SMS_PERMISSION) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "READ_SMS granted: $readGranted, RECEIVE_SMS granted: $receiveGranted")
        return readGranted && receiveGranted
    }

    /**
     * Requests SMS permissions from the user.
     * For better compatibility, we'll request them one at a time.
     */
    fun requestSmsPermissions(activity: Activity) {
        Log.d(TAG, "Requesting SMS permissions...")

        // Check which permissions are missing
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(activity, READ_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(READ_SMS_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(activity, RECEIVE_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(RECEIVE_SMS_PERMISSION)
        }

        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All permissions already granted")
            return
        }

        Log.d(TAG, "Requesting permissions: $permissionsToRequest")

        // Request permissions (handle them together for now, but with better error handling)
        ActivityCompat.requestPermissions(
            activity,
            permissionsToRequest.toTypedArray(),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Alternative method: Request permissions one at a time for better compatibility
     */
    fun requestPermissionsSequentially(activity: Activity) {
        Log.d(TAG, "Requesting SMS permissions sequentially...")

        // Check and request READ_SMS first
        if (ContextCompat.checkSelfPermission(activity, READ_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(READ_SMS_PERMISSION),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else if (ContextCompat.checkSelfPermission(activity, RECEIVE_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            // Only request RECEIVE_SMS if READ_SMS is already granted
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(RECEIVE_SMS_PERMISSION),
                SMS_PERMISSION_REQUEST_CODE + 1
            )
        } else {
            Log.d(TAG, "All permissions already granted")
        }
    }

    /**
     * Checks if the user should see a rationale for why the permission is needed.
     */
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Checks if any permission rationale should be shown.
     */
    fun shouldShowAnyPermissionRationale(activity: Activity): Boolean {
        return shouldShowPermissionRationale(activity, READ_SMS_PERMISSION) ||
               shouldShowPermissionRationale(activity, RECEIVE_SMS_PERMISSION)
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
    fun isPermissionPermanentlyDenied(activity: Activity, permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        val isPermanentlyDenied = !isGranted && !shouldShowRationale

        if (isPermanentlyDenied) {
            Log.d(TAG, "Permission permanently denied: $permission")
        }

        return isPermanentlyDenied
    }

    /**
     * Checks if any permission is permanently denied.
     */
    fun isAnyPermissionPermanentlyDenied(activity: Activity): Boolean {
        return isPermissionPermanentlyDenied(activity, READ_SMS_PERMISSION) ||
               isPermissionPermanentlyDenied(activity, RECEIVE_SMS_PERMISSION)
    }

    /**
     * Gets the missing permissions (not yet granted).
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, READ_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            missing.add(READ_SMS_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(context, RECEIVE_SMS_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            missing.add(RECEIVE_SMS_PERMISSION)
        }

        return missing
    }

    /**
     * Gets a specific permission description.
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            READ_SMS_PERMISSION -> "Read SMS messages to detect transaction alerts"
            RECEIVE_SMS_PERMISSION -> "Receive SMS messages for real-time transaction detection"
            else -> "Unknown permission"
        }
    }
}
