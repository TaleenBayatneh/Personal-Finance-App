# Personal Finance App

A simple Android application for managing personal finances with features like income tracking, expense management, budgeting, and user profiles.

## Features

- **User Authentication** - Login and signup with email validation
- **Income Tracking** - Record and track income transactions
- **Expense Management** - Log and categorize expenses
- **Budget Planning** - Set and monitor budgets
- **Dashboard** - View financial overview with customizable periods (daily, weekly, monthly, yearly)
- **User Profile** - Manage personal information
- **Settings** - Adjust app preferences including theme (light/dark mode)
- **Session Management** - Remember user credentials

## Tech Stack

- **Language** - Kotlin & Java
- **Framework** - Android SDK with AndroidX
- **Architecture** - Navigation Component with Fragment-based UI
- **Database** - SQLite
- **Build Tool** - Gradle

## Requirements

- Android SDK 21 or higher
- Android Studio

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Build and run on an emulator or physical device

```bash
./gradlew build
./gradlew installDebug
```

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/project/Course_project/
│       │       ├── activities/
│       │       ├── fragments/
│       │       ├── database/
│       │       └── utils/
│       └── res/
│           ├── layout/
│           ├── drawable/
│           └── values/
```
