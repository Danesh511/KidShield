# KidShield 🛡️

**KidShield** is a modern, highly secure Android parental control application designed to empower parents to monitor and restrict device usage seamlessly. Built entirely with **Jetpack Compose** and modern Android architecture, KidShield provides a robust suite of tools to keep children safe in the digital world.

## ✨ Key Features

- **App Management & Blocking:** Parents can whitelist safe applications. If a child attempts to open an unauthorized app, KidShield instantly intercepts it and overlays a secure, un-bypassable lock screen.
- **Anti-Shoulder Surfing Security (Scrambled Keypad):** Children are incredibly perceptive. KidShield uses a dynamic, randomized keypad for its PIN entry. Every time the lock screen appears, the numbers `0-9` are shuffled into different positions, making it physically impossible for a child to memorize the PIN by watching finger movements.
- **Quick Settings Tile Integration:** Parents can instantly toggle the protection ON or OFF directly from the Android Quick Settings notification shade. No need to open the app!
- **Unstoppable Background Service:** KidShield runs a resilient foreground service utilizing `UsageStatsManager` and `SYSTEM_ALERT_WINDOW` (Overlay) permissions to ensure absolute monitoring accuracy.
- **Daily Usage Dashboard:** A clean, intuitive dashboard that reports exactly how many minutes the child has spent on allowed apps today.

## 🛠️ Tech Stack

- **UI:** 100% Jetpack Compose (Material 3)
- **Architecture:** MVVM, Kotlin Coroutines, and Flows
- **Local Storage:** Jetpack DataStore (Preferences)
- **Background Tasks:** Android Services, Quick Settings `TileService`, `LifecycleEventObserver`
- **Security:** Dynamic PIN validation logic

## 🔒 Permissions Required

To operate securely and reliably, KidShield requires the following permissions:
1. **Usage Access:** To monitor which app is currently in the foreground.
2. **Display over other apps (Overlay):** To draw the security lock screen on top of unauthorized applications.
3. **Foreground Service:** To keep the monitoring engine alive in the background.

## 🚀 Getting Started

1. Clone the repository.
2. Open the project in **Android Studio**.
3. Sync the project with Gradle files.
4. Run the app on an Android device or emulator (Android API 24+).
5. On the first launch, you will be prompted to create your 4-digit Master PIN and grant the necessary permissions.

---
*Built with ❤️ for digital safety.*
