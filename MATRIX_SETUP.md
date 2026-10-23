# Matrix Synapse Docker Setup & Developer Guide

This document explains how to set up, launch, and verify the Matrix Synapse stack for **Teledentistry Project** using Docker Compose. The setup includes:
- **Matrix Synapse Homeserver** (`http://localhost:8008`)
- **PostgreSQL Database** (`matrix-postgres`)
- **Coturn STUN/TURN Server** (Port `3478` for WebRTC video/audio NAT traversal)
- **Element Web App** (`http://localhost:8080`)

---

## 🚀 Quick Start Instructions

### 1. Start the Docker Services

From the project root directory, run:
```bash
docker compose up -d
```
Or to run only the Matrix stack:
```bash
docker compose -f matrix/docker-compose.yml up -d
```

### 2. Verify Services are Running

Run:
```bash
docker ps
```
You should see the following running containers:
- `smile-pocketbase` (Port 8090)
- `matrix-synapse` (Port 8008)
- `matrix-postgres` (Port 5432 / internal)
- `matrix-coturn` (Port 3478)
- `matrix-element-web` (Port 8080)

You can check Matrix server health by opening in your browser or running `curl`:
```bash
curl http://localhost:8008/_matrix/client/versions
```
Expected output: JSON containing supported Matrix API versions (`v1.1`, `v1.2`, `v1.3`, `v1.4`, `v1.5`, `v1.6`, etc.).

---

## 👤 Creating Test Users on Matrix

To test chat and video call signaling between a practitioner and a patient, register two users:

*(Optional now: PocketBase creates accounts automatically; manual users are only for Element experiments. Open registration is off, so Option B no longer works.)*

### Option A: Via Command Line
Exec into the running `matrix-synapse` container:

1. **Register Practitioner user**:
   ```bash
   docker exec -it matrix-synapse register_new_matrix_user -c /data/homeserver.yaml http://localhost:8008 -u doctor1 -p password123 -a
   ```
2. **Register Patient user**:
   ```bash
   docker exec -it matrix-synapse register_new_matrix_user -c /data/homeserver.yaml http://localhost:8008 -u patient1 -p password123 --no-admin
   ```

### Option B: Via Element Web Client
1. Open `http://localhost:8080` in your web browser.
2. Click **Create Account**.
3. Select Home Server: `http://localhost:8008`.
4. Choose username and password.

---

## Server-managed Matrix accounts and rooms (PocketBase hooks)

Nobody registers Matrix accounts by hand any more. PocketBase (`pocketbase/pb_hooks/matrix.pb.js`) creates and logs in a Matrix user `@pb_<pbUserId>:localhost` for every PocketBase user (password = HMAC of the PB user id with a server-only secret; it never leaves the server). Open registration is **disabled** in `matrix/homeserver.yaml`; accounts are created via Synapse's shared-secret admin API.

All endpoints need `Authorization: <PocketBase token>`.

- `POST /api/matrix/session` (optional body `{"device_id":"..."}`, default `TELEDENT_APP`) -> `{"user_id","access_token","device_id"}`. Safe to call repeatedly. Also sets the Matrix display name.
- `POST /api/matrix/appointment-room/{appointmentId}` -> `{"room_id"}`. 403 unless the caller is the appointment's patient or practitioner; 404 unknown appointment; creates the private room once (both users joined, practitioner = PL100, patient = PL0 but may send messages and `m.call.*`; only the practitioner may set `org.teledentistry.teeth_overlay`) and stores it in `appointments.matrixRoomId` (hidden field, not visible/writable through the records API).

Environment variables for PocketBase (defaults match the repo config): `MATRIX_INTERNAL_URL` (default `http://127.0.0.1:8008`; compose sets `http://matrix-synapse:8008`), `MATRIX_SERVER_NAME` (`localhost`), `MATRIX_REGISTRATION_SHARED_SECRET` (must equal `registration_shared_secret` in homeserver.yaml), `MATRIX_PASSWORD_SECRET` (change in production). PocketBase must be 0.40.x and started with `--hooksDir=pocketbase/pb_hooks` (the Docker image does this).

### Testing on a USB phone / emulator with `adb reverse`

The device reaches the PC only through TCP tunnels:
```bash
adb reverse tcp:8090 tcp:8090   # PocketBase
adb reverse tcp:8008 tcp:8008   # Synapse
adb reverse tcp:3478 tcp:3478   # TURN (TCP only)
```
The app then uses `http://127.0.0.1:8090` / `http://127.0.0.1:8008`, and Synapse hands out `turn:127.0.0.1:3478?transport=tcp` (see `turn_uris`). Start the stack with `docker compose up -d matrix-synapse matrix-coturn` and run PocketBase (docker or `pocketbase.exe serve --hooksDir=pb_hooks`; for the exe set `MATRIX_INTERNAL_URL` only if Synapse is not on 127.0.0.1:8008).

---

## 📱 Android Client Connectivity

When running the Android App on the Emulator:
- **Matrix Homeserver URL**: `http://10.0.2.2:8008`
- **PocketBase URL**: `http://10.0.2.2:8090`

*(Note: `10.0.2.2` is the special alias for `localhost` inside the Android Emulator).*

---

## 🩺 Features Included in Android App

1. **Matrix REST Chat (`ChatActivity`)**:
   - Real-time messaging using Matrix Client API v3 (`/_matrix/client/v3/rooms/{roomId}/send/m.room.message`).
   - Seamless polling/syncing for real-time chat updates between patient and practitioner.

2. **Matrix Video Call & Real-time Dental Overlay (`VideoCallActivity`)**:
   - WebRTC video consultation view.
   - **Practitioner Dental Overlay Toggle**: Practitioner can toggle **"Teeth Outline Overlay"** on or off during live calls.
   - **Interactive Teeth Map**: Practitioner can select individual teeth (e.g. Tooth 14, 19, 30) to mark conditions (Healthy, Decay/Carious, Treated/Restored, Active Focus).
   - **Real-Time Patient Sync**: The dental overlay toggle and marked tooth conditions are broadcasted via Matrix state event (`org.teledentistry.teeth_overlay`) in real time, so the patient sees the live overlay and marked teeth on their screen instantly!
