# Finance App

A modern Android application for personal finance management, built with Kotlin, Android Jetpack, and Room Database following MVVM Clean Architecture.

## Architecture & Project Structure

The project follows the standard MVVM (Model-View-ViewModel) pattern with a Repository layer:

```text
com.example.finance/
├── data/                       # Data layer
│   ├── local/                  # Room local persistence
│   │   ├── dao/                # Data Access Objects (SQL queries)
│   │   ├── entity/             # Room Database Entities (Tables)
│   │   └── AppDatabase.kt      # RoomDatabase singleton
│   └── repository/             # Repository pattern (single source of truth)
│
├── ui/                         # Presentation layer
│   ├── MainActivity.kt
│   └── ...
│
└── utils/                      # Utilities and helpers
```

## Tech Stack & Dependencies

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Compile SDK:** 36 (Android 15)
- **Architecture:** MVVM + Repository Pattern
- **Persistence:** Room Database (SQLite abstraction)
- **Asynchronous Work:** Kotlin Coroutines & Flow
- **UI Toolkit:** Android Jetpack / Material Components

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repo-url>
   ```
2. Open the project in **Android Studio Ladybug / Meerkat** (or later).
3. Let Gradle sync and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on an Android Emulator or physical device (API 24+).
