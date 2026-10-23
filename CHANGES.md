# Messaging & Video Fix — What Changed

Chat and video calls now both run over the self-hosted Matrix stack (Synapse + coturn) that was
already in this repo. Tested end to end between a real Android phone (patient) and the emulator
(practitioner): chat both ways, history, retry after a failed send, video calls in both directions,
hang-up from either side, and the live teeth overlay.

See `MATRIX_SETUP.md` for running it (including the `adb reverse` device setup).

## What was broken

- **Chat:** messages were stored in the PocketBase consultation record, but the app read them back
  through `getDiagnosisNotes()`, which strips the chat log, so the other side never received
  anything. Patients were also not allowed to write that record, the field was capped at 5000
  chars, and each send overwrote the other person's messages.
- **Video:** there was no video code — the screen only showed a timer. The Matrix client was a stub
  (PocketBase token reused as a Matrix token, made-up room ids), so nothing reached Synapse.

## How it works now

1. **Accounts** — `POST /api/matrix/session` (PocketBase hook) creates the user's Matrix account on
   first use (`@pb_<userId>:localhost`) via Synapse shared-secret registration and returns a
   Matrix token. Matrix passwords are derived server-side and never reach the app.
   Open registration on Synapse is now disabled.
2. **Rooms** — `POST /api/matrix/appointment-room/{id}` creates/joins one private room per
   appointment (only that appointment's patient and practitioner; others get 403) and stores it in
   the new hidden `appointments.matrixRoomId` field.
3. **Chat** — `ChatActivity` sends/receives `m.room.message` over Matrix with a long-poll sync.
4. **Video** — `MatrixCallSession` does 1:1 WebRTC calls (`io.getstream:stream-webrtc-android`)
   with Matrix VoIP signalling (`m.call.invite/answer/candidates/hangup`), media relayed via coturn.
5. **Teeth overlay** — sent as the `org.teledentistry.teeth_overlay` room state event; only the
   practitioner can set it.

## Files

New:
- `pocketbase/pb_hooks/matrix.pb.js`, `pocketbase/pb_hooks/matrix_lib.js`
- `pocketbase/pb_migrations/1792000000_updated_consultations_notes_size.js`
- `pocketbase/pb_migrations/1792100000_updated_appointments_matrix_room.js`
- `data/remote/matrix/MatrixCallSession.java`, `MatrixSessionApi.java`, `MatrixMessagesResponse.java`

Changed:
- App: `MatrixClient`, `MatrixApi`, `MatrixSyncResponse`, `ChatActivity`, `VideoCallActivity`,
  `ConsultationActivity`, `Consultation`, `activity_chat.xml`, `activity_video_call.xml`,
  `AndroidManifest.xml`, `app/build.gradle.kts` (WebRTC dependency)
- Backend: `docker-compose.yml` (hooks volume + env vars, coturn command override),
  `pocketbase/Dockerfile` (PocketBase 0.40.4 — `pb_data` was already migrated by 0.40.x — and
  `--hooksDir`), `matrix/homeserver.yaml` (registration off, rate limits, TURN uris),
  `matrix/turnserver.conf`, `matrix/docker-compose.yml`, `MATRIX_SETUP.md`

## Known limitations

- Audio is negotiated but was not verified by ear (the emulator has no audio).
- Turning the camera off freezes the other side's view on the last frame instead of going black.
- The overlay label shows "Dr. Practitioner" rather than the dentist's name.
- Old chat messages stored in PocketBase consultations are not migrated to Matrix.

## Before production

- Pick the real `server_name` (e.g. `matrix.yourclinic.com`) **before** real users exist — it is
  part of every Matrix user/room id and cannot be changed later.
- Move secrets out of the repo: `matrix/localhost.signing.key`, the registration/TURN/macaroon/form
  secrets in `homeserver.yaml`, and set `MATRIX_PASSWORD_SECRET` / `MATRIX_REGISTRATION_SHARED_SECRET`
  for PocketBase. Don't commit the signing key.
- HTTPS via a reverse proxy, and remove `usesCleartextTraffic="true"` from the manifest.
- Postgres instead of SQLite for Synapse.
- coturn: set `external-ip` to the server's public IP and open the relay port range.
  `MatrixCallSession.FORCE_RELAY` is `true` for the USB/adb-reverse test setup; consider `false`
  in production so direct connections are tried first.
- Tighten the relaxed `rc_login` / `rc_joins` limits in `homeserver.yaml` (relaxed for testing).
- Consider disabling federation for a private clinic server.
