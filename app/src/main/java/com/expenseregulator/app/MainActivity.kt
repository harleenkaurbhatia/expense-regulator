package com.expenseregulator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.expenseregulator.app.adapters.SmsAdapter
import kotlinx.coroutines.launch

/**
 * Main activity that displays transaction SMS messages.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsReader: SmsReader

    private val smsMessages = mutableListOf<SmsMessage>()
    private var isLoading = false

    private val TAG = "ExpenseRegulator/MainActivity"

    // BroadcastReceiver for real-time SMS detection
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SmsReceiver.ACTION_TRANSACTION_DETECTED) {
                val smsId = intent.getLongExtra(SmsReceiver.EXTRA_SMS_ID, 0)
                val sender = intent.getStringExtra(SmsReceiver.EXTRA_SMS_SENDER) ?: ""
                val body = intent.getStringExtra(SmsReceiver.EXTRA_SMS_BODY) ?: ""
                val timestamp = intent.getLongExtra(SmsReceiver.EXTRA_SMS_TIMESTAMP, 0)

                Log.d(TAG, "Real-time transaction SMS received!")
                Log.d(TAG, "Sender: $sender")
                Log.d(TAG, "Body: ${body.take(40)}...")

                // Add to the list
                val newSms = SmsMessage(smsId, sender, body, timestamp)
                addNewTransactionSms(newSms)

                // Show notification
                Toast.makeText(
                    this@MainActivity,
                    "New transaction SMS detected!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created")

        setupUI()
        checkPermissionsAndLoadData()
    }

    private fun setupUI() {
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        // Set up RecyclerView
        smsAdapter = SmsAdapter(smsMessages)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = smsAdapter
        }

        // Set up swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadTransactionSms()
        }

        // Initialize SMS reader
        smsReader = SmsReader(this)
    }

    private fun checkPermissionsAndLoadData() {
        if (PermissionHelper.areSmsPermissionsGranted(this)) {
            Log.d(TAG, "Permissions granted, loading SMS...")
            loadTransactionSms()
        } else {
            Log.d(TAG, "Permissions not granted, showing explanation screen")
            val intent = Intent(this, PermissionExplanationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadTransactionSms() {
        if (isLoading) {
            Log.d(TAG, "Already loading, skipping...")
            return
        }

        isLoading = true
        swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            Log.d(TAG, "Loading transaction SMS from inbox...")

            val result = smsReader.readTransactionSms()

            result.onSuccess { messages ->
                Log.d(TAG, "Successfully loaded ${messages.size} transaction SMS")

                smsMessages.clear()
                smsMessages.addAll(messages)
                smsAdapter.notifyDataSetChanged()

                updateEmptyState()
            }.onFailure { error ->
                Log.e(TAG, "Failed to load transaction SMS", error)
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load SMS: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            isLoading = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun addNewTransactionSms(smsMessage: SmsMessage) {
        Log.d(TAG, "Adding new transaction SMS to list")

        // Add to the beginning of the list (newest first)
        smsMessages.add(0, smsMessage)
        smsAdapter.notifyItemInserted(0)

        // Scroll to top
        recyclerView.smoothScrollToPosition(0)

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (smsMessages.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyStateTextView.text = "No transaction SMS found yet.\n\nSwipe down to refresh or wait for new transaction messages."
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Register receiver for real-time SMS detection
        val filter = IntentFilter(SmsReceiver.ACTION_TRANSACTION_DETECTED)
        registerReceiver(smsReceiver, filter)
        Log.d(TAG, "SMS receiver registered")
    }

    override fun onPause() {
        super.onPause()
        // Unregister receiver
        try {
            unregisterReceiver(smsReceiver)
            Log.d(TAG, "SMS receiver unregistered")
        } catch (e: IllegalArgumentException) {
            // Receiver wasn't registered, ignore
            Log.w(TAG, "Receiver was not registered")
        }
    }
}
