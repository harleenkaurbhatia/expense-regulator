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
            PermissionHelper.requestSmsPermissions(this)
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
        if (PermissionHelper.isPermissionPermanentlyDenied(this)) {
            settingsButton.visibility = android.view.View.VISIBLE
            grantButton.isEnabled = false
            grantButton.text = "Permission Permanently Denied"
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

        if (requestCode == PermissionHelper.SMS_PERMISSION_REQUEST_CODE) {
            if (PermissionHelper.handlePermissionResult(grantResults)) {
                // Permissions granted, proceed to MainActivity
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Permissions denied
                if (PermissionHelper.isPermissionPermanentlyDenied(this)) {
                    // Show settings button
                    findViewById<TextView>(R.id.tvOpenSettings).visibility = android.view.View.VISIBLE
                    findViewById<Button>(R.id.btnGrantPermission).isEnabled = false
                    findViewById<Button>(R.id.btnGrantPermission).text = "Permission Permanently Denied"

                    Toast.makeText(
                        this,
                        "Permissions are permanently denied. Please enable them in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Permissions are required for this app to work",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
