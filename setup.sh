#!/bin/bash

# Expense Regulator - Setup and Installation Script

set -e

echo "=========================================="
echo "Expense Regulator - Phase 1 Setup"
echo "=========================================="
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ Error: adb not found. Please install Android SDK Platform Tools."
    exit 1
fi

echo "✓ Android SDK Platform Tools found"

# Check if device is connected
echo ""
echo "Checking for connected Android device..."
DEVICES=$(adb devices | grep -w "device" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No Android device found. Please:"
    echo "  1. Connect your device via USB"
    echo "  2. Enable USB Debugging in Developer Options"
    echo "  3. Accept the USB debugging authorization prompt"
    echo ""
    echo "Run 'adb devices' to verify connection"
    exit 1
fi

echo "✓ Device(s) connected:"
adb devices | grep -w "device"

# Check if local.properties exists
if [ ! -f "local.properties" ]; then
    echo ""
    echo "Creating local.properties from template..."
    if [ -f "local.properties.template" ]; then
        cp local.properties.template local.properties
        echo "✓ Created local.properties"
        echo "⚠️  Please edit local.properties and set sdk.dir to your Android SDK path"
        echo "   Example: sdk.dir=/Users/$(whoami)/Library/Android/sdk"
        echo ""
        read -p "Press Enter to continue after setting sdk.dir..."
    fi
fi

# Make gradlew executable
if [ -f "gradlew" ]; then
    chmod +x gradlew
    echo "✓ Made gradlew executable"
fi

# Build and install
echo ""
echo "=========================================="
echo "Building and installing Expense Regulator..."
echo "=========================================="
echo ""

# Clean build
echo "Step 1: Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "Step 2: Building debug APK..."
./gradlew assembleDebug

# Install on device
echo ""
echo "Step 3: Installing on device..."
./gradlew installDebug

echo ""
echo "=========================================="
echo "✓ Installation complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Launch 'Expense Regulator' on your device"
echo "2. Review the permission explanation screen"
echo "3. Grant SMS permissions when prompted"
echo "4. Wait for SMS scan to complete"
echo ""
echo "To monitor logs:"
echo "  adb logcat | grep ExpenseRegulator"
echo ""
echo "For troubleshooting, see README.md"
