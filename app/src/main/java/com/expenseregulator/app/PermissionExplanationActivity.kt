package com.expenseregulator.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that explains why SMS permissions are needed before requesting them.
 */
class PermissionExplanationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_explanation)

        setupUI()
    }

    private fun setupUI() {
        // Set up the grant permission button
        val grantButton = findViewById<Button>(R.id.btnGrantPermission)
        grantButton.setOnClickListener {
            // Try sequential permission request for better compatibility
            PermissionHelper.requestPermissionsSequentially(this)
        }

        // Set up the skip button
        val skipButton = findViewById<Button>(R.id.btnSkip)
        skipButton.setOnClickListener {
            Toast.makeText(
                this,
                "SMS permissions are required for full functionality",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        // Set up the settings button (for permanently denied permissions)
        val settingsButton = findViewById<TextView>(R.id.tvOpenSettings)
        settingsButton.setOnClickListener {
            openAppSettings()
        }

        // Check if permissions are permanently denied and show settings button
        if (PermissionHelper.isAnyPermissionPermanentlyDenied(this)) {
            settingsButton.visibility = android.view.View.VISIBLE
            grantButton.isEnabled = false
            grantButton.text = "Permission Permanently Denied - Open Settings"
        } else {
            settingsButton.visibility = android.view.View.GONE
            grantButton.isEnabled = true
            grantButton.text = "Grant Permission"
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionHelper.SMS_PERMISSION_REQUEST_CODE, PermissionHelper.SMS_PERMISSION_REQUEST_CODE + 1 -> {
                // Check if all requested permissions were granted
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                if (allGranted) {
                    // Permissions granted, proceed to MainActivity
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()

                    // Check if all permissions are now granted
                    if (PermissionHelper.areSmsPermissionsGranted(this)) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Request the remaining permission
                        PermissionHelper.requestPermissionsSequentially(this)
                    }
                } else {
                    // Permissions denied
                    if (PermissionHelper.isAnyPermissionPermanentlyDenied(this)) {
                        // Show settings button
                        findViewById<TextView>(R.id.tvOpenSettings).visibility = android.view.View.VISIBLE
                        findViewById<Button>(R.id.btnGrantPermission).isEnabled = false
                        findViewById<Button>(R.id.btnGrantPermission).text = "Permission Permanently Denied - Open Settings"

                        Toast.makeText(
                            this,
                            "Permissions are permanently denied. Please enable them in Settings → Apps → Expense Regulator → Permissions → SMS",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // User denied but can ask again
                        Toast.makeText(
                            this,
                            "Permissions are required for this app to work. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permission status when activity resumes (in case user granted in settings)
        if (PermissionHelper.areSmsPermissionsGranted(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Update UI based on current permission status
            setupUI()
        }
    }
}
