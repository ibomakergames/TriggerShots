# Trigger Shots

Android floating trigger prototype.

## Features
- Gray translucent floating HOLD button
- Draggable small tap-target marker
- Adjustable rate from 1 to 30 clicks/sec
- Hold the trigger to repeat; release to stop
- Uses Android AccessibilityService gesture dispatch

## Build
The repository's GitHub Actions workflow builds a debug APK with Android Gradle Plugin 8.9.1 and Android SDK 35.

## Install/setup
1. Install the APK from the Actions artifact.
2. Grant "Display over other apps".
3. Enable Trigger Shots under Android Accessibility settings.
4. Start the floating controls and drag the small marker to the desired tap position.

Android and individual apps may throttle or block automated gestures. The selected rate is a requested rate and is not guaranteed to be the effective rate.
