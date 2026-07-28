# Quick Start Guide - Expense Regulator Phase 1

## 1. Verify Device Connection
```bash
adb devices
```
You should see your device listed with "device" status.

## 2. Build and Install (One Command)
```bash
cd /Users/harleen/expense-regulator
./setup.sh
```

Or manually:
```bash
./gradlew clean installDebug
```

## 3. Test the App
1. Launch "Expense Regulator" on your phone
2. Read the permission explanation
3. Tap "Grant Permission"
4. Approve the system dialog
5. Wait for SMS scan to complete

## 4. Monitor Logs
```bash
adb logcat | grep ExpenseRegulator
```

## 5. Test Real-time Detection
1. Keep app open (or in recent apps)
2. Receive a new transaction SMS
3. Watch for "New transaction SMS detected!" notification

## Expected Results
- ✅ Transaction SMS appear in scrollable list
- ✅ Each shows sender, timestamp, and raw message
- ✅ Amounts are highlighted in green
- ✅ New SMS appear automatically when received
- ✅ OTPs and spam are filtered out

## Troubleshooting
**No SMS appearing?**
```bash
adb logcat | grep ExpenseRegulator
```
Check if permissions are granted in Settings → Apps → Expense Regulator → Permissions

**Real-time not working?**
- Keep app in memory (don't swipe away)
- Check logs: `adb logcat | grep SmsReceiver`

**Permission issues?**
- Settings → Apps → Expense Regulator → Permissions → SMS → Allow
- Then pull down to refresh in the app

## Key Log Tags
- `ExpenseRegulator/SmsFilter` - Filtering decisions
- `ExpenseRegulator/SmsReader` - Scan progress
- `ExpenseRegulator/SmsReceiver` - Real-time detection
- `ExpenseRegulator/MainActivity` - UI updates
