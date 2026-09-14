# Personal Finance

A local-first Android application for tracking personal income, expenses, and budgets. The app is written in Kotlin and stores its data on-device with Room.

## Features

- Dashboard with current-month balance, income, expenses, and recent transactions
- Income and expense transactions with categories, notes, and dates
- Transaction history and transaction details
- Category-based spending budgets
- Expense breakdown and income/expense charts
- Predefined income and expense categories on first launch
- Fully local persistence; the app does not require a network connection

## Tech stack

- Kotlin
- Android SDK 35, min SDK 24
- Android Views with View Binding and Data Binding
- Room
- ViewModel and LiveData
- Navigation Component and Safe Args
- Kotlin Coroutines
- MPAndroidChart
- Gradle Kotlin DSL

## Project structure

```text
PersonalFinance/
├── app/src/main/java/io/github/vuvannamsec/personalfinance/
│   ├── data/
│   │   ├── database/      # Room database and DAOs
│   │   ├── model/         # Transaction, category, and budget entities
│   │   └── repository/    # Data access layer
│   ├── domain/            # Pure finance calculations
│   ├── ui/                # Screens and RecyclerView adapters
│   ├── viewmodel/         # UI state and data coordination
│   ├── FinanceApplication.kt
│   └── MainActivity.kt
├── gradle/
└── settings.gradle.kts
```

## Build and run

### Requirements

- Android Studio with Android SDK 35 installed
- JDK 17

Open the `PersonalFinance` directory in Android Studio and run the `app` configuration, or build from the command line:

```bash
cd PersonalFinance
./gradlew assembleDebug
```

The debug APK is generated under `PersonalFinance/app/build/outputs/apk/debug/`.

## Verification

Run the same checks used by CI:

```bash
cd PersonalFinance
./gradlew --no-daemon \
  testDebugUnitTest \
  lintDebug \
  assembleDebug \
  assembleDebugAndroidTest \
  assembleRelease
```

Pull requests are checked automatically with GitHub Actions. Instrumentation test sources are compiled in CI; executing them requires an Android device or emulator.

## Data and privacy

Financial data is stored in the app's local Room database. The application does not request network access, cleartext traffic is disabled, and Android backup is disabled for app data. The project does not contain backend or cloud-sync integration.

## Current scope

This is a single-device finance tracker. Account authentication, cloud synchronization, multi-currency support, and backup/restore workflows are outside the current scope. Amounts are currently displayed using a dollar-style currency format.
