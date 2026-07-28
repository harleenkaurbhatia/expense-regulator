package com.expenseregulator.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.expenseregulator.app.R
import com.expenseregulator.app.SmsMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for displaying SMS messages.
 */
class SmsAdapter(
    private val smsMessages: List<SmsMessage>
) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = smsMessages[position]
        holder.bind(sms)
    }

    override fun getItemCount(): Int = smsMessages.size

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.tvSender)
        private val timestampTextView: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val bodyTextView: TextView = itemView.findViewById(R.id.tvBody)
        private val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(sms: SmsMessage) {
            senderTextView.text = sms.sender
            timestampTextView.text = formatTimestamp(sms.timestamp)
            bodyTextView.text = sms.body

            // Try to extract and display amount
            val amount = com.expenseregulator.app.SmsFilter.extractAmount(sms.body)
            if (amount != null) {
                amountTextView.text = amount
                amountTextView.visibility = View.VISIBLE
            } else {
                amountTextView.visibility = View.GONE
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return try {
                val date = Date(timestamp)
                val formattedDate = dateFormat.format(date)
                // Add relative time
                val now = System.currentTimeMillis()
                val diff = now - timestamp
                val minutes = diff / (1000 * 60)
                val hours = diff / (1000 * 60 * 60)
                val days = diff / (1000 * 60 * 60 * 24)

                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "$minutes min ago"
                    hours < 24 -> "$hours hr ago"
                    days < 7 -> "$days day(s) ago"
                    else -> formattedDate
                }
            } catch (e: Exception) {
                "Unknown time"
            }
        }
    }
}
