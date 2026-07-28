package com.expenseregulator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that listens for incoming SMS messages and detects transaction alerts.
 */
class SmsReceiver : BroadcastReceiver() {
    private val TAG = "ExpenseRegulator/SmsReceiver"
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SMS received")

        // Check if this is an SMS received intent
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle.get("pdus") as? Array<ByteArray>
                    if (pdus != null) {
                        for (pdu in pdus) {
                            val smsMessage = SmsMessage.createFromPdu(pdu)
                            if (smsMessage != null) {
                                handleIncomingSms(context, smsMessage)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing incoming SMS", e)
                }
            }
        }
    }

    private fun handleIncomingSms(context: Context, smsMessage: SmsMessage) {
        val sender = smsMessage.originatingAddress ?: "Unknown"
        val body = smsMessage.messageBody
        val timestamp = smsMessage.timestampMillis

        Log.d(TAG, "Incoming SMS from: $sender")
        Log.d(TAG, "Message body: ${body.take(50)}...")

        // Check if this is a transaction SMS
        if (SmsFilter.isTransactionSms(body, sender)) {
            Log.d(TAG, "✓ Transaction SMS detected in real-time!")

            // Create a new transaction message
            val transactionSms = SmsMessage(
                id = System.currentTimeMillis(), // Use timestamp as temporary ID
                sender = sender,
                body = body,
                timestamp = timestamp
            )

            // Notify MainActivity via a local broadcast
            notifyTransactionDetected(context, transactionSms)
        } else {
            Log.d(TAG, "✗ Not a transaction SMS, ignoring")
        }
    }

    private fun notifyTransactionDetected(context: Context, smsMessage: SmsMessage) {
        scope.launch {
            try {
                // Send a local broadcast to notify the MainActivity
                val intent = Intent(ACTION_TRANSACTION_DETECTED).apply {
                    putExtra(EXTRA_SMS_ID, smsMessage.id)
                    putExtra(EXTRA_SMS_SENDER, smsMessage.sender)
                    putExtra(EXTRA_SMS_BODY, smsMessage.body)
                    putExtra(EXTRA_SMS_TIMESTAMP, smsMessage.timestamp)
                }
                context.sendBroadcast(intent)
                Log.d(TAG, "Transaction broadcast sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending transaction broadcast", e)
            }
        }
    }

    companion object {
        const val ACTION_TRANSACTION_DETECTED = "com.expenseregulator.app.TRANSACTION_DETECTED"
        const val EXTRA_SMS_ID = "sms_id"
        const val EXTRA_SMS_SENDER = "sms_sender"
        const val EXTRA_SMS_BODY = "sms_body"
        const val EXTRA_SMS_TIMESTAMP = "sms_timestamp"
    }
}
