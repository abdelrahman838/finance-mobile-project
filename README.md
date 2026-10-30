# MobileFinance

An Android personal finance app built with Kotlin, Jetpack Compose and Room.

## Tech Stack

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose + Material 3
- **Persistence:** Room (annotation processing via KSP)
- **Min SDK:** 24 · **Compile/Target SDK:** 36
- **Build:** Android Gradle Plugin 8.11, Gradle 8.13

## Architecture

The app follows **MVVM with a Repository layer**, with packages split by layer:

```
Compose Screen  ──observes──▶  ViewModel  ──calls──▶  Repository  ──calls──▶  Room DAO  ──▶  SQLite
      ▲                            │
      └──── StateFlow<UiState> ────┘
```

- The **UI** only displays state and forwards user actions to its ViewModel.
- The **ViewModel** holds screen state and never touches Room directly.
- The **Repository** is the single source of truth that ViewModels talk to.
- **Room** (entities + DAOs) is hidden behind the repository.

## Project Structure

```text
app/src/main/java/com/example/mobilefinance/
├── MainActivity.kt          # Single activity hosting the Compose UI
│
├── data/                    # Data layer
│   ├── local/               # Room persistence
│   │   ├── dao/             # @Dao interfaces: SQL queries (Flow for reads, suspend for writes)
│   │   ├── entity/          # @Entity classes: one class per database table
│   │   └── AppDatabase.kt   # (to add) @Database class, singleton instance
│   └── repository/          # Repositories wrapping DAOs, exposed to ViewModels
│
├── ui/                      # Presentation layer (Jetpack Compose)
│   ├── navigation/          # Navigation graph and route definitions
│   ├── screens/             # One sub-package per screen: XScreen.kt + XViewModel.kt
│   ├── components/          # Reusable composables shared across screens
│   └── theme/               # Material 3 colors, typography and theme
│
└── util/                    # Helpers: formatters, date utilities, constants
```

### Conventions

- **Entities** are named `XEntity` (e.g. `TransactionEntity`), **DAOs** `XDao`, **repositories** `XRepository`.
- Each screen lives in its own folder under `ui/screens/`, e.g. `ui/screens/home/HomeScreen.kt` and `HomeViewModel.kt`.
- Screens collect ViewModel state with `collectAsStateWithLifecycle()`.
- Composables used by more than one screen go in `ui/components/`.
- Empty folders contain a `.gitkeep` file so they show up in the repository; delete it once the folder has real files.

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/abdelrahman838/finance-mobile-project.git
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Build and run on an emulator or device (API 24+):
   ```bash
   ./gradlew assembleDebug
   ```
