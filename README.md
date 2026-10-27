# Teledentistry-Project

## Project Overview
This project is a Teledentistry mobile application

The purpose of this project is to provide a digital environment where patients and dentists can connect remotely to facilitate oral health consultations, triaging, and treatement monitoring without requiring an in-person clinic visit. 

## Tech Stack
1. Frontend/Languages: Java
2. Backend: PocketBase & Matrix
3. UI Framework: XML layout
4. IDE: Android Studio
5. Database: PocketBase

## Features
1. Virtual Consultations
2. Digital Intake & E-Signatures
3. Store-and-Forward Imaging
4. Electronic Prescriptions (e-Rx)
5. Remote Patient Monitoring
6. Treatment Planning & Billing
7. Patient Education Resource

# How to Run the Teledentistry App

Step-by-step instructions for getting the **Teledentistry Project** running locally, including setting up the backend (PocketBase & Matrix Synapse), building the Android application, and running it on an emulator or physical device.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your development machine:
1. **Android Studio** (Hedgehog or newer recommended) with Android SDK (API 34+).
2. **Java Development Kit (JDK) 17**.
3. **Docker & Docker Compose** (required for running PocketBase, Matrix Synapse, and Coturn STUN/TURN servers).
4. **Git**.

---

## 🚀 Step 1: Start Backend Services (PocketBase & Matrix)

The app relies on a backend for authentication, database records, and Matrix for real-time chat & WebRTC video calls.

1. Open your terminal in the project directory (`TeledentistryProject-chat-video-fix`).
2. Start the Docker containers (PocketBase, Matrix Synapse, PostgreSQL, Coturn, Element Web):
   ```bash
   docker compose up -d
   ```
3. Verify containers are running successfully:
   ```bash
   docker ps
   ```
   You should see:
   - `smile-pocketbase` (Port `8090`)
   - `matrix-synapse` (Port `8008`)
   - `matrix-postgres` (Internal DB)
   - `matrix-coturn` (Port `3478`)
   - `matrix-element-web` (Port `8080`)

*(Optional) If running on a physical Android device connected via USB, set up ADB reverse port forwarding so the device can access localhost:*
```bash
adb reverse tcp:8090 tcp:8090
adb reverse tcp:8008 tcp:8008
adb reverse tcp:3478 tcp:3478
```

---

## 📱 Step 2: Open and Build the Project in Android Studio

1. Open **Android Studio**.
2. Click **Open** and select the folder:
   `C:/Users/wwwne/Desktop/Smil-E Final App/TeledentistryProject-chat-video-fix`
3. Allow Gradle to sync all dependencies automatically. If prompted or if needed, click **File > Sync Project with Gradle Files**.
4. Verify network URLs in your configuration (by default, the Android Emulator uses `10.0.2.2` to access host services):
   - **PocketBase URL**: `http://10.0.2.2:8090` (or `http://127.0.0.1:8090` via ADB reverse)
   - **Matrix Homeserver URL**: `http://10.0.2.2:8008` (or `http://127.0.0.1:8008` via ADB reverse)

---

## ▶️ Step 3: Run the App on an Emulator or Device

### Using Android Emulator:
1. Open the **Device Manager** in Android Studio.
2. Start an Android Virtual Device (AVD) running **API 34 (Android 14)** or newer.
3. Click the **Run 'app'** button (green play icon) in the Android Studio toolbar, or run via terminal:
   ```bash
   ./gradlew assembleDebug
   ```
4. The application will build and launch automatically on your emulator/device.

---

## 🔑 Step 4: Default Test Accounts

You can log into the app using the pre-seeded mock accounts:

### 1. Patient Account
- **Email**: `patient@example.com`
- **Password**: `password123`
- **Role**: Patient (Can book appointments, view treatment plans, chat with dentists, join video consultations).

### 2. Practitioner / Dentist Account
- **Email**: `practitioner@example.com`
- **Password**: `password123`
- **Role**: Practitioner (Can manage patient records, billings, dental charts, and toggle real-time teeth overlays during video calls).

### 3. Admin Account
- **Email**: `admin@example.com`
- **Password**: `password123`
- **Role**: Admin (Can oversee platform activities, user enquiries, and practitioner approvals).

---

## 🩺 Step 5: Testing Key Features

