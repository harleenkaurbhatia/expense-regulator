# Expense Regulator - Phase 1

A native Android personal finance app that automatically reads bank/UPI transaction SMS messages.

## Phase 1 Features

✅ **SMS Permission Management**
- Clear in-app explanation screen before requesting system permissions
- Handles permission denial gracefully with option to open settings

✅ **SMS Detection & Filtering**
- Reads existing SMS inbox and filters for transaction messages
- Real-time detection of new transaction SMS via BroadcastReceiver
- Smart filtering that excludes OTPs, spam, and promotional messages

✅ **Transaction Indicators**
- Detects keywords: "debited", "spent", "paid", "purchase", "transaction"
- Identifies UPI transactions, bank debits, and card payments
- Recognizes account/card patterns: "XXXX XXXX", "**** ****", "ending with"

✅ **UI Features**
- Scrollable list of detected transaction SMS
- Swipe-to-refresh for manual re-scanning
- Real-time updates when new transaction SMS arrive
- Empty state message when no transactions found
- Displays sender, timestamp, and raw SMS body

## Prerequisites

- Android SDK (API 26+)
- Gradle 8.2+
- Java 17
- Physical Android device with USB debugging enabled

## Build & Install Instructions

### 1. Verify Device Connection

First, ensure your device is connected and USB debugging is enabled:

```bash
# Check if device is detected
adb devices
```

Expected output:
```
List of devices attached
<device_serial>    device
```

If you don't see your device:
- Enable **USB Debugging** in Developer Options on your phone
- Accept the USB debugging authorization prompt on your phone
- Try a different USB cable or port

### 2. Build the Project

```bash
# Navigate to project directory
cd /Users/harleen/expense-regulator

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug
```

### 3. Install on Device

```bash
# Install the debug APK
./gradlew installDebug

# OR manually install the APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Quick One-Command Install

```bash
# Clean, build, and install in one command
./gradlew clean installDebug
```

## Testing the App

### Initial Setup

1. **Launch the app** on your device
2. **Review the permission explanation** screen
3. **Tap "Grant Permission"** when ready
4. **Approve the system permission dialog** when it appears
5. **Wait for SMS scan** to complete

### What You Should See

- **Transaction SMS** will appear in a scrollable list
- Each card shows: sender, timestamp, and raw SMS body
- If an amount is detected, it's highlighted in green
- Newest messages appear at the top

### Real-time Detection

1. **Keep the app open** (or in recent apps)
2. **Receive a new transaction SMS** (bank debit, UPI payment, etc.)
3. **Watch for the Toast notification**: "New transaction SMS detected!"
4. **New message appears** at the top of the list automatically

### Manual Permission Grant (if needed)

If the in-app request doesn't trigger the system dialog:

1. Go to **Settings → Apps → Expense Regulator**
2. Tap **Permissions**
3. Find **SMS** and set to **Allow**
4. Go back to the app and **pull down to refresh**

## Debugging & Logs

### Monitor App Logs

```bash
# Filter for app-specific logs
adb logcat | grep ExpenseRegulator

# More verbose logging with timestamps
adb logcat -v time | grep ExpenseRegulator

# Log all SMS-related activity
adb logcat -v time | grep -E "ExpenseRegulator|SmsReceiver"
```

### Key Log Tags

- `ExpenseRegulator/SmsFilter` - SMS filtering decisions
- `ExpenseRegulator/SmsReader` - Inbox scan progress and results
- `ExpenseRegulator/SmsReceiver` - Real-time SMS detection
- `ExpenseRegulator/MainActivity` - UI updates and user interactions
- `ExpenseRegulator/PermissionHelper` - Permission state changes

### Example Log Output

```
D/ExpenseRegulator/MainActivity: MainActivity created
D/ExpenseRegulator/PermissionHelper: All SMS permissions granted
D/ExpenseRegulator/SmsReader: Starting SMS inbox scan...
D/ExpenseRegulator/SmsReader: SMS scan complete:
D/ExpenseRegulator/SmsReader:   - Total messages scanned: 1,247
D/ExpenseRegulator/SmsReader:   - Transaction messages found: 23
D/ExpenseRegulator/SmsReader:   - Messages filtered out: 1,224
D/ExpenseRegulator/SmsFilter: ✓ Transaction SMS detected: Your A/c XX1234 debited with Rs.1,250.00...
```

## What Gets Detected (Examples)

### ✅ Will Detect
- "Your A/c XX1234 debited with Rs.1,250.00 at AMAZON"
- "UPI transaction: Rs.500.00 paid to SWIGGY via GPay"
- "Amount spent Rs.2,000.00 on card ending 4567 at FLIPKART"
- "Transaction alert: Rs.750.00 debited from account ****7890"

### ❌ Will Filter Out
- "Your OTP is 123456 for transaction verification"
- "Get 50% cashback on your next purchase!"
- "Loan EMI of Rs.5,000.00 due on 5th"
- "Your verification code is 789012"

## Troubleshooting

### No SMS appearing

1. **Check permissions**: Settings → Apps → Expense Regulator → Permissions → SMS
2. **Review logs**: `adb logcat | grep ExpenseRegulator`
3. **Swipe to refresh**: Pull down on the main screen
4. **Verify SMS content**: Ensure you have actual transaction SMS in your inbox

### Real-time detection not working

1. **Keep app in memory**: Don't swipe it away from recent apps
2. **Check logs**: `adb logcat | grep SmsReceiver`
3. **Test with real SMS**: Send yourself a test transaction message

### Build errors

```bash
# Clean and rebuild
./gradlew clean build --stacktrace

# Check Gradle version
./gradlew --version

# Update Gradle wrapper if needed
./gradlew wrapper --gradle-version=8.2
```

## Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml          # Permissions & receiver registration
│   ├── java/com/expenseregulator/app/
│   │   ├── MainActivity.kt          # Main UI with RecyclerView
│   │   ├── PermissionExplanationActivity.kt  # Permission request screen
│   │   ├── SmsReceiver.kt           # BroadcastReceiver for new SMS
│   │   ├── SmsFilter.kt             # Transaction detection logic
│   │   ├── SmsReader.kt             # Inbox reading functionality
│   │   ├── PermissionHelper.kt      # Permission management
│   │   └── adapters/
│   │       └── SmsAdapter.kt        # RecyclerView adapter
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   ├── item_sms.xml
│       │   └── activity_permission_explanation.xml
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
└── build.gradle.kts                 # App dependencies
```

## Next Phase (Phase 2)

Once Phase 1 is tested and working, the next phase will include:
- Parse transaction SMS into structured fields (amount, bank, merchant, etc.)
- Store transactions in Room database
- Add transaction details view
- Basic transaction categorization

## Privacy Notice

- **All data stays on-device** - No cloud storage or external servers
- Only reads transaction SMS - OTPs and spam are filtered out
- SMS content is processed locally on your device
- No data collection or analytics

## License

Personal use project.
