# Teledentistry Development Environment Guide

This guide provides step-by-step instructions for setting up, running, and testing the complete local development environment for the **Teledentistry Project**, including the Android mobile client, PocketBase backend, PostgreSQL database, and Supabase local services.

---

## Prerequisites

Ensure you have the following tools installed on your machine:
1. **Android Studio** (Hedgehog or newer recommended) with Android SDK (API 34+)
2. **Java Development Kit (JDK) 17**
3. **Docker & Docker Compose** (for running PocketBase and PostgreSQL containers)
4. **Supabase CLI** (for local Supabase instance management)
5. **Git**

---

## 1. Backend & Database Setup

### Option A: PocketBase Backend (Docker Compose)
PocketBase provides a lightweight realtime backend with built-in database, authentication, and file storage.

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Start PocketBase and PostgreSQL services using Docker Compose:
   ```bash
   docker compose up -d
   ```
3. Verify containers are running:
   ```bash
   docker ps
   ```
4. Access services:
   - **PocketBase Admin UI & API**: `http://localhost:8090/_/`
   - **PostgreSQL Dev Database**: `localhost:5432` (User: `admin`, Password: `admin`, DB: `smile_dev`)

---

### Option B: Supabase Local Development
Supabase is also included for advanced authentication, database migrations, and edge functions.

1. Navigate to the project root directory:
   ```bash
   cd Teledentistry-Project
   ```
2. Initialize / Start local Supabase services:
   ```bash
   supabase start
   ```
3. Once started, Supabase outputs local connection URLs and keys:
   - **API URL**: `http://127.0.0.1:54321`
   - **Studio Dashboard**: `http://127.0.0.1:54323`
   - **DB Connection**: `postgresql://postgres:postgres@127.0.0.1:54322/postgres`

4. Apply Database Migrations:
   The initial schema is located in `supabase/migrations/20260922190000_initial_schema.sql`. Supabase automatically applies migrations on `supabase start`. To reset or apply manually:
   ```bash
   supabase db reset
   ```

---

## 2. Android Mobile Application Setup

1. Open **Android Studio**.
2. Select **Open** and choose the `Teledentistry-Project` root folder (`C:\Users\wwwne\Desktop\TeledentistryProject`).
3. Allow Gradle to sync dependencies. If necessary, trigger a manual sync via **File > Sync Project with Gradle Files**.
4. Configure API Base URLs in your network client classes or `local.properties`:
   ```properties
   SUPABASE_URL=http://10.0.2.2:54321
   POCKETBASE_URL=http://10.0.2.2:8090
   ```
   *(Note: `10.0.2.2` is the Android Emulator alias for localhost)*

---

## 3. Running & Testing the App

### Running on Emulator / Device
1. Open the **Device Manager** in Android Studio and start an Android Virtual Device (AVD) running API 34+.
2. Click the **Run 'app'** (green play button) in Android Studio.
3. The app will build using Gradle (`./gradlew assembleDebug`) and launch on the emulator.

### Running Automated Tests
To run unit tests and instrumentation tests from the terminal:
```bash
# Run unit tests
./gradlew test

# Run connected Android instrumentation tests
./gradlew connectedAndroidTest
```

---

## 4. Continuous Integration (CI/CD)

The project includes GitHub Actions workflow configuration located at `.github/workflows/ci.yml` which automatically handles:
- Code checkout and JDK 17 setup
- Gradle unit test execution (`./gradlew test`)
- Debug APK building (`./gradlew assembleDebug`)
- Artifact upload (`debug-apk`)
