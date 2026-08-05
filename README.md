# Math Puzzle Master

Math Puzzle Master is an educational Android application developed using Kotlin and Jetpack Compose. It challenges users with algebraic character puzzles where they must find the value of a target character based on a set of equations.

## Core Features

- **Landing Page**: Entry point with navigation to Game, Statistics, and Settings.
- **Puzzle Activity**: The main game screen where puzzles are displayed and solved.
- **User Statistics**: Tracks and displays user progress, scores, and levels reached using a Room database.
- **Settings**: Allows users to adjust game preferences like difficulty and sound.
- **Networking**: Fetches puzzle data from an external API (simulated via Retrofit).
- **Architecture**: Follows modern Android development practices including ViewModels, Repository pattern, and clean architecture.
- **Unit Testing**: Includes comprehensive unit tests for business logic and data handling.

## Technical Implementation

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Persistence Library
- **Networking**: Retrofit with Kotlinx Serialization
- **Navigation**: Jetpack Navigation Component
- **State Management**: StateFlow and ViewModel
- **Testing**: JUnit 4

## Getting Started

1. Clone the repository.
2. Open in Android Studio (Ladybug or later).
3. Build and run the `:app` module on an emulator or physical device.

## Unit Tests

Run the unit tests using:
```bash
./gradlew test
```
Tests can be found in `app/src/test/java/com/example/eduapp/`.
