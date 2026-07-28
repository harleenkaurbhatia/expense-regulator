package com.expenseregulator.app

import android.util.Log
import java.util.regex.Pattern

/**
 * Filters SMS messages to identify transaction alerts from banks and UPI apps.
 */
object SmsFilter {
    private const val TAG = "ExpenseRegulator/SmsFilter"

    // Transaction indicators - must contain at least one of these
    private val TRANSACTION_KEYWORDS = listOf(
        "debited", "spent", "paid", "purchase", "transaction",
        "withdrawn", "charged", "deducted"
    )

    // Patterns that indicate a transaction
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s*\\d+(?:,\\d{3})*(?:\\.\\d{2})?",
        Pattern.CASE_INSENSITIVE
    )

    private val ACCOUNT_PATTERN = Pattern.compile(
        "(?:XXXX|\\*\\*\\*\\*)\\s*\\d{4}|ending with\\s*\\d{4}|a/c\\s*\\*?\\d{4}",
        Pattern.CASE_INSENSITIVE
    )

    private val UPI_PATTERN = Pattern.compile(
        "UPI\\s*(?:transaction|payment|transfer|debit|paid|sent)",
        Pattern.CASE_INSENSITIVE
    )

    // Patterns to exclude - NOT transaction messages
    private val EXCLUDE_PATTERNS = listOf(
        Pattern.compile("OTP|one.?time.?password|verification.?code|verify your", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:^|\\s)promo(?:tion)?|(?:^|\\s)offer|(?:^|\\s)discount|(?:^|\\s)cashback(?=\\s|$)", Pattern.CASE_INSENSITIVE)
    )

    private val LOAN_PATTERN = Pattern.compile(
        "loan|EMI|installment|credit.?due",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Determines if an SMS message is a transaction alert.
     *
     * @param message The SMS body text
     * @param sender The sender phone number or ID (optional)
     * @return true if this appears to be a transaction SMS
     */
    fun isTransactionSms(message: String, sender: String? = null): Boolean {
        if (message.isBlank()) {
            return false
        }

        // Filter out very short messages (likely OTPs)
        if (message.length < 30) {
            Log.d(TAG, "Filtered out short message (${message.length} chars): ${message.take(20)}...")
            return false
        }

        // Check exclude patterns first
        for (pattern in EXCLUDE_PATTERNS) {
            if (pattern.matcher(message).find()) {
                Log.d(TAG, "Filtered out exclude pattern match: ${message.take(30)}...")
                return false
            }
        }

        // Check for loan/EMI patterns (exclude unless also has transaction keywords)
        if (LOAN_PATTERN.matcher(message).find()) {
            val hasTransactionKeyword = TRANSACTION_KEYWORDS.any { keyword ->
                message.contains(keyword, ignoreCase = true)
            }
            if (!hasTransactionKeyword) {
                Log.d(TAG, "Filtered out loan/EMI message: ${message.take(30)}...")
                return false
            }
        }

        // Must have at least one transaction indicator
        val hasTransactionKeyword = TRANSACTION_KEYWORDS.any { keyword ->
            message.contains(keyword, ignoreCase = true)
        }

        val hasAmount = AMOUNT_PATTERN.matcher(message).find()
        val hasAccount = ACCOUNT_PATTERN.matcher(message).find()
        val hasUpi = UPI_PATTERN.matcher(message).find()

        // Decision logic: Need at least one strong indicator
        val isTransaction = hasTransactionKeyword || hasUpi ||
                (hasAmount && (hasAccount || hasTransactionKeyword))

        if (isTransaction) {
            Log.d(TAG, "✓ Transaction SMS detected: ${message.take(40)}...")
            Log.d(TAG, "  - Keywords: $hasTransactionKeyword, Amount: $hasAmount, Account: $hasAccount, UPI: $hasUpi")
        } else {
            Log.d(TAG, "✗ Not a transaction: ${message.take(30)}...")
        }

        return isTransaction
    }

    /**
     * Extracts the amount from a transaction SMS if present.
     * This is a simple extraction for display purposes.
     */
    fun extractAmount(message: String): String? {
        val matcher = AMOUNT_PATTERN.matcher(message)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }
}
