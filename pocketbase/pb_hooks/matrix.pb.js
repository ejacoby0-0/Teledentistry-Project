/// <reference path="../pb_data/types.d.ts" />
// Matrix integration endpoints. See MATRIX_SETUP.md.
//   POST /api/matrix/session                          -> {user_id, access_token, device_id}
//   POST /api/matrix/appointment-room/{appointmentId} -> {room_id}

routerAdd("POST", "/api/matrix/session", (e) => {
    const lib = require(`${__hooks}/matrix_lib.js`)
    const user = e.auth
    let body = {}
    try { body = e.requestInfo().body || {} } catch (_) {}
    let deviceId = typeof body.device_id === "string" ? body.device_id.replace(/[^A-Za-z0-9_\-]/g, "").slice(0, 32) : ""
    if (!deviceId) deviceId = "TELEDENT_APP"

    const s = lib.login(user.id, deviceId)
    try { lib.setDisplayName(s, lib.displayNameFor(user)) } catch (err) { console.log("displayname failed", err) }
    return e.json(200, s)
}, $apis.requireAuth())

routerAdd("POST", "/api/matrix/appointment-room/{appointmentId}", (e) => {
    const lib = require(`${__hooks}/matrix_lib.js`)
    const caller = e.auth
    const apptId = e.request.pathValue("appointmentId")

    let appt
    try { appt = $app.findRecordById("appointments", apptId) } catch (_) { throw new NotFoundError("Appointment not found") }

    const userIdOf = (coll, id) => {
        if (!id) return ""
        try { return $app.findRecordById(coll, id).getString("user") } catch (_) { return "" }
    }
    const patientUser = userIdOf("patients", appt.getString("patient"))
    const practUser = userIdOf("practitioners", appt.getString("practitioner"))
    if (caller.id !== patientUser && caller.id !== practUser) {
        throw new ForbiddenError("You are not a participant of this appointment")
    }
    if (!patientUser || !practUser) {
        throw new BadRequestError("Appointment needs both a patient and a practitioner")
    }

    const PENDING = "pending:"
    const readRoom = () => $app.findRecordById("appointments", apptId).getString("matrixRoomId") || ""
    const ensureJoined = (roomId, pbUserId) => {
        lib.withSession(pbUserId, (s) => {
            const j = lib.mx("POST", "/_matrix/client/v3/join/" + encodeURIComponent(roomId), s.access_token, {})
            if (j.status !== 200) {
                console.log("join failed", j.status, JSON.stringify(j.json))
                throw new ApiError(502, "Could not join Matrix room", null)
            }
        })
    }

    // Existing room
    let room = readRoom()
    if (room && room.indexOf(PENDING) !== 0) {
        ensureJoined(room, caller.id)
        return e.json(200, {room_id: room})
    }

    // Atomic claim (single SQL UPDATE) so only one request creates the room.
    const now = Math.floor(Date.now() / 1000)
    let won = $app.db().newQuery(
        "UPDATE appointments SET matrixRoomId = {:m} WHERE id = {:id} AND COALESCE(matrixRoomId,'') = ''"
    ).bind({m: PENDING + now, id: apptId}).execute().rowsAffected() > 0
    if (!won && room.indexOf(PENDING) === 0) {
        const ts = parseInt(room.slice(PENDING.length), 10) || 0
        if (now - ts > 60) { // stale claim from a crashed request: take over
            won = $app.db().newQuery("UPDATE appointments SET matrixRoomId = {:m} WHERE id = {:id} AND matrixRoomId = {:old}")
                .bind({m: PENDING + now, id: apptId, old: room}).execute().rowsAffected() > 0
        }
    }

    if (!won) {
        // Someone else is creating it: wait (up to ~20s) for the real id.
        for (let i = 0; i < 80; i++) {
            sleep(250)
            room = readRoom()
            if (room && room.indexOf(PENDING) !== 0) {
                ensureJoined(room, caller.id)
                return e.json(200, {room_id: room})
            }
        }
        throw new ApiError(409, "Room creation in progress, retry shortly", null)
    }

    // We own the claim: create the room as the practitioner, join the patient.
    const mxPract = lib.mxUserId(practUser), mxPatient = lib.mxUserId(patientUser)
    let roomId = ""
    try {
        // make sure the patient account exists (and has a name) before inviting
        lib.withSession(patientUser, (s) => {
            try { lib.setDisplayName(s, lib.displayNameFor($app.findRecordById("users", patientUser))) } catch (_) {}
        })
        roomId = lib.withSession(practUser, (s) => {
            try { lib.setDisplayName(s, lib.displayNameFor($app.findRecordById("users", practUser))) } catch (_) {}
            const callEvents = ["m.call.invite", "m.call.candidates", "m.call.answer", "m.call.hangup",
                "m.call.reject", "m.call.select_answer", "m.call.negotiate", "m.call.sdp_stream_metadata_changed"]
            const events = {"org.teledentistry.teeth_overlay": 100, "m.room.message": 0}
            callEvents.forEach((t) => { events[t] = 0 })
            const users = {}
            users[mxPract] = 100
            users[mxPatient] = 0
            const r = lib.mx("POST", "/_matrix/client/v3/createRoom", s.access_token, {
                name: "Smile Session " + apptId.slice(0, 6),
                preset: "private_chat",
                is_direct: true,
                invite: [mxPatient],
                power_level_content_override: {
                    users: users, users_default: 0, events_default: 0, state_default: 50,
                    invite: 100, kick: 100, ban: 100, redact: 50, events: events,
                },
            })
            if (r.status !== 200 || !r.json.room_id) {
                console.log("createRoom failed", r.status, JSON.stringify(r.json))
                throw new ApiError(502, "Could not create Matrix room", null)
            }
            return r.json.room_id
        })
        ensureJoined(roomId, patientUser)
    } catch (err) {
        // release the claim so a retry can work
        $app.db().newQuery("UPDATE appointments SET matrixRoomId = '' WHERE id = {:id} AND matrixRoomId = {:m}")
            .bind({id: apptId, m: PENDING + now}).execute()
        throw err
    }
    $app.db().newQuery("UPDATE appointments SET matrixRoomId = {:r} WHERE id = {:id}").bind({r: roomId, id: apptId}).execute()
    return e.json(200, {room_id: roomId})
}, $apis.requireAuth())
