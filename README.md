# WakeUp - Wake-on-LAN Scheduler

<p align="center">
  <img src="WakeUp-icon.png" alt="WakeUp Logo" width="120"/>
</p>

## Overview

**WakeUp** is an Android application that allows you to remotely wake up network devices using Wake-on-LAN (WOL) technology and schedule automatic wake-up times. Perfect for managing home servers, workstations, and other WOL-enabled devices directly from your smartphone.

## Features

### 🖥️ Device Management
- **Add Multiple Devices**: Manage all your WOL-enabled devices from a single interface
- **Real-time Status Monitoring**: Check if devices are online or offline
- **Device Details**: Store device name, MAC address, IP address, and port information
- **Quick Actions**: Wake, schedule, or delete devices with one tap

### ⏰ Smart Scheduling
- **Flexible Schedules**: Set wake-up times for any day of the week
- **Multiple Schedules**: Create multiple schedules per device
- **Enable/Disable Schedules**: Toggle schedules on or off without deleting them
- **Unified Schedule View**: See all scheduled wake-ups across all devices in one place

### 🎨 Modern Material Design
- **Material 3 UI**: Beautiful, modern interface following Google's Material Design guidelines
- **Theme Options**: Choose between Light, Dark, AMOLED Black, or System Default themes
- **Custom Accent Colors**: Personalize the app with your preferred color scheme
- **Smooth Animations**: Polished transitions and interactions

### 🔔 Notifications
- **Wake Notifications**: Get notified when a scheduled wake-up is executed
- **Background Execution**: Schedules work reliably even when the app is closed

## Requirements

- **Android Version**: 8.0 (API 26) or higher
- **Permissions**:
  - Internet access (for sending WOL packets)
  - Network state access
  - Exact alarm scheduling (for precise wake times)
  - Notifications (for wake confirmations)

## Installation

### Option 1: From Release APK
1. Download the latest `WakeUp.apk` from the releases section
2. Enable "Install from Unknown Sources" in your Android settings
3. Open the APK file and follow the installation prompts

### Option 2: Build from Source
```bash
# Clone the repository
git clone <repository-url>
cd Scheduler-mobile

# Build the release APK
./gradlew assembleRelease

# The APK will be located at:
# app/build/outputs/apk/release/app-release.apk
```

## Usage

### Adding a Device
1. Tap the **"+"** button on the home or devices screen
2. Enter device details:
   - **Name**: A friendly name for your device
   - **MAC Address**: The device's network adapter MAC address (required for WOL)
   - **IP Address**: The device's IP address or broadcast address
   - **Port**: WOL port (default: 9)
3. Tap **"Save"**

### Waking a Device Manually
- Navigate to the **Devices** screen
- Tap the **"WAKE"** button on the desired device
- A notification will confirm the magic packet was sent

### Creating a Schedule
1. Tap the **calendar icon** on a device
2. Tap the **"+"** button to add a new schedule
3. Set the wake-up time using the time picker
4. Select which days of the week the schedule should run
5. Tap **"Add Schedule"**

### Managing Schedules
- **Enable/Disable**: Use the toggle switch on each schedule
- **Edit**: Tap on a schedule to modify its time or days
- **Delete**: Tap the trash icon to remove a schedule
- **View All**: Access the "All Schedules" screen from the home page to see a unified view

## Technical Details

### Architecture
- **MVVM Pattern**: Clean separation of concerns using ViewModel and Repository layers
- **Dependency Injection**: Hilt for dependency management
- **Database**: Room for local data persistence
- **Background Work**: WorkManager and AlarmManager for reliable scheduled execution
- **UI**: Jetpack Compose for modern, declarative UI

### Key Technologies
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern UI toolkit
- **Room Database**: Local data storage
- **Hilt**: Dependency injection
- **WorkManager**: Background task scheduling
- **Coroutines & Flow**: Asynchronous programming
- **Material 3**: Latest Material Design components

### Wake-on-LAN Implementation
WakeUp sends magic packets over UDP to wake devices:
- Standard WOL magic packet format (6 bytes of FF followed by 16 repetitions of the target MAC address)
- Supports both unicast and broadcast addressing
- Configurable port (default: 9)

## Customization

### Themes
Navigate to **Settings** to customize:
- **App Theme**: Light, Dark, AMOLED Black, or System Default
- **Accent Color**: Choose your preferred app color scheme

## Troubleshooting

### Device Won't Wake Up
- Ensure Wake-on-LAN is enabled in the device's BIOS/UEFI
- Verify the MAC address is correct
- Check that the device's network adapter supports WOL
- Confirm your router allows broadcast packets
- Try using the device's IP address or your network's broadcast address (e.g., 192.168.1.255)

### Schedules Not Working
- Verify the app has permission to schedule exact alarms (Settings → Apps → WakeUp → Permissions)
- Check that battery optimization is disabled for WakeUp
- Ensure the schedules are enabled (toggle switch is on)

## Privacy & Permissions

WakeUp requires the following permissions:
- **Internet**: To send Wake-on-LAN packets over your network
- **Network State**: To check device connectivity status
- **Exact Alarms**: To trigger scheduled wake-ups at precise times
- **Notifications**: To inform you when a scheduled wake-up occurs
- **Boot Completed**: To restore schedules after device restart

**Data Privacy**: All data is stored locally on your device. WakeUp does not collect, transmit, or share any personal information.

## Author

**Robert Ciobanu**

## License

This project is provided as-is for personal use.

## Support

For issues, questions, or feature requests, please open an issue in the repository.

---

**Version**: 1.0.0  
**Last Updated**: December 2025
