package com.expenseregulator.app

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data class representing an SMS message.
 */
data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long
)

/**
 * Reads SMS messages from the device inbox and filters for transactions.
 */
class SmsReader(private val context: Context) {
    private val TAG = "ExpenseRegulator/SmsReader"

    /**
     * Reads all SMS messages from the inbox and filters for transaction alerts.
     *
     * @return List of transaction SMS messages, sorted by date (newest first)
     */
    suspend fun readTransactionSms(): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting SMS inbox scan...")

            val transactionMessages = mutableListOf<SmsMessage>()
            var totalMessages = 0
            var filteredMessages = 0

            // Query the SMS inbox
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null, // No selection (read all)
                null, // No selection args
                "${Telephony.Sms.DATE} DESC" // Sort by date, newest first
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                Log.d(TAG, "Cursor created, scanning messages...")

                while (it.moveToNext()) {
                    totalMessages++

                    val id = it.getLong(idIndex)
                    val sender = it.getString(addressIndex) ?: "Unknown"
                    val body = it.getString(bodyIndex) ?: ""
                    val timestamp = it.getLong(dateIndex)

                    // Apply filter
                    if (SmsFilter.isTransactionSms(body, sender)) {
                        transactionMessages.add(
                            SmsMessage(
                                id = id,
                                sender = sender,
                                body = body,
                                timestamp = timestamp
                            )
                        )
                        filteredMessages++
                    }
                }
            }

            Log.d(TAG, "SMS scan complete:")
            Log.d(TAG, "  - Total messages scanned: $totalMessages")
            Log.d(TAG, "  - Transaction messages found: $filteredMessages")
            Log.d(TAG, "  - Messages filtered out: ${totalMessages - filteredMessages}")

            Result.success(transactionMessages)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception - SMS permission not granted", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS messages", e)
            Result.failure(e)
        }
    }

    /**
     * Reads a single SMS message by ID.
     */
    suspend fun readSmsById(id: Long): Result<SmsMessage?> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id.toString())
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                null
            )

            val message = cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                    val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                    SmsMessage(
                        id = it.getLong(idIndex),
                        sender = it.getString(addressIndex) ?: "Unknown",
                        body = it.getString(bodyIndex) ?: "",
                        timestamp = it.getLong(dateIndex)
                    )
                } else null
            }

            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS by ID: $id", e)
            Result.failure(e)
        }
    }
}
