// Shared helpers for the Matrix integration hooks. Loaded via require() from
// inside route handlers (each PocketBase JSVM handler runs in its own isolated
// context, so top-level state of matrix.pb.js is not visible to handlers).

function env(name, def) {
    const v = $os.getenv(name)
    return v ? v : def
}

function cfg() {
    return {
        baseUrl: env("MATRIX_INTERNAL_URL", "http://127.0.0.1:8008").replace(/\/+$/, ""),
        serverName: env("MATRIX_SERVER_NAME", "localhost"),
        // must equal registration_shared_secret in matrix/homeserver.yaml
        regSecret: env("MATRIX_REGISTRATION_SHARED_SECRET", "teledentistry_secret_registration_key_2026"),
        // server-only; derives each user's Matrix password. CHANGE IN PRODUCTION.
        pwSecret: env("MATRIX_PASSWORD_SECRET", "dev-only-teledentistry-matrix-password-secret"),
    }
}

// ---------- pure JS SHA-1 / HMAC-SHA1 (JSVM $security has no sha1) ----------
function utf8Bytes(s) {
    const out = []
    for (let i = 0; i < s.length; i++) {
        let c = s.charCodeAt(i)
        if (c >= 0xd800 && c < 0xdc00 && i + 1 < s.length) {
            c = 0x10000 + ((c - 0xd800) << 10) + (s.charCodeAt(++i) - 0xdc00)
        }
        if (c < 0x80) out.push(c)
        else if (c < 0x800) out.push(0xc0 | (c >> 6), 0x80 | (c & 63))
        else if (c < 0x10000) out.push(0xe0 | (c >> 12), 0x80 | ((c >> 6) & 63), 0x80 | (c & 63))
        else out.push(0xf0 | (c >> 18), 0x80 | ((c >> 12) & 63), 0x80 | ((c >> 6) & 63), 0x80 | (c & 63))
    }
    return out
}

function sha1Bytes(bytes) {
    const rol = (n, b) => ((n << b) | (n >>> (32 - b))) >>> 0
    const msg = bytes.slice()
    const bitLen = msg.length * 8
    msg.push(0x80)
    while (msg.length % 64 !== 56) msg.push(0)
    const hi = Math.floor(bitLen / 0x100000000), lo = bitLen >>> 0
    msg.push((hi >>> 24) & 255, (hi >>> 16) & 255, (hi >>> 8) & 255, hi & 255,
        (lo >>> 24) & 255, (lo >>> 16) & 255, (lo >>> 8) & 255, lo & 255)
    let h0 = 0x67452301, h1 = 0xefcdab89, h2 = 0x98badcfe, h3 = 0x10325476, h4 = 0xc3d2e1f0
    const w = new Array(80)
    for (let off = 0; off < msg.length; off += 64) {
        for (let i = 0; i < 16; i++) {
            const j = off + i * 4
            w[i] = ((msg[j] << 24) | (msg[j + 1] << 16) | (msg[j + 2] << 8) | msg[j + 3]) >>> 0
        }
        for (let i = 16; i < 80; i++) w[i] = rol(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1)
        let a = h0, b = h1, c = h2, d = h3, e = h4
        for (let i = 0; i < 80; i++) {
            let f, k
            if (i < 20) { f = (b & c) | (~b & d); k = 0x5a827999 }
            else if (i < 40) { f = b ^ c ^ d; k = 0x6ed9eba1 }
            else if (i < 60) { f = (b & c) | (b & d) | (c & d); k = 0x8f1bbcdc }
            else { f = b ^ c ^ d; k = 0xca62c1d6 }
            const t = (rol(a, 5) + (f >>> 0) + e + k + w[i]) >>> 0
            e = d; d = c; c = rol(b, 30); b = a; a = t
        }
        h0 = (h0 + a) >>> 0; h1 = (h1 + b) >>> 0; h2 = (h2 + c) >>> 0; h3 = (h3 + d) >>> 0; h4 = (h4 + e) >>> 0
    }
    const out = []
    for (const h of [h0, h1, h2, h3, h4]) out.push((h >>> 24) & 255, (h >>> 16) & 255, (h >>> 8) & 255, h & 255)
    return out
}

function hmacSha1Hex(key, message) {
    let k = utf8Bytes(key)
    if (k.length > 64) k = sha1Bytes(k)
    while (k.length < 64) k.push(0)
    const ipad = k.map((b) => b ^ 0x36), opad = k.map((b) => b ^ 0x5c)
    const inner = sha1Bytes(ipad.concat(utf8Bytes(message)))
    return sha1Bytes(opad.concat(inner)).map((b) => (b < 16 ? "0" : "") + b.toString(16)).join("")
}

// ---------- Matrix helpers ----------
function mx(method, path, token, body) {
    const c = cfg()
    const headers = {"Content-Type": "application/json"}
    if (token) headers["Authorization"] = "Bearer " + token
    const res = $http.send({
        url: c.baseUrl + path,
        method: method,
        headers: headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        timeout: 20,
    })
    return {status: res.statusCode, json: res.json || {}}
}

function localpart(pbUserId) {
    // PocketBase ids are [a-z0-9]; valid Matrix localpart characters.
    return "pb_" + pbUserId.toLowerCase()
}

function mxUserId(pbUserId) {
    return "@" + localpart(pbUserId) + ":" + cfg().serverName
}

function mxPassword(pbUserId) {
    return $security.hs256("matrix-pw:" + pbUserId, cfg().pwSecret)
}

function registerUser(pbUserId) {
    const c = cfg()
    const nonceRes = mx("GET", "/_synapse/admin/v1/register")
    if (nonceRes.status !== 200 || !nonceRes.json.nonce) {
        throw new ApiError(502, "Matrix server unavailable (nonce)", null)
    }
    const nonce = nonceRes.json.nonce
    const username = localpart(pbUserId), password = mxPassword(pbUserId)
    const mac = hmacSha1Hex(c.regSecret, nonce + "\u0000" + username + "\u0000" + password + "\u0000" + "notadmin")
    const r = mx("POST", "/_synapse/admin/v1/register", null, {
        nonce: nonce, username: username, password: password, admin: false, mac: mac,
    })
    // M_USER_IN_USE means a concurrent request created it first: fine.
    if (r.status !== 200 && r.json.errcode !== "M_USER_IN_USE") {
        console.log("matrix register failed", r.status, JSON.stringify(r.json))
        throw new ApiError(502, "Matrix account creation failed", null)
    }
}

// Logs in as the PB user, creating the Matrix account on first use.
// deviceId optional (reuse => no device growth, but invalidates that device's old token).
function login(pbUserId, deviceId) {
    const attempt = () => {
        const body = {
            type: "m.login.password",
            identifier: {type: "m.id.user", user: localpart(pbUserId)},
            password: mxPassword(pbUserId),
            initial_device_display_name: "Teledentistry",
        }
        if (deviceId) body.device_id = deviceId
        return mx("POST", "/_matrix/client/v3/login", null, body)
    }
    let r = attempt()
    if (r.status === 403) { // unknown user: register then retry
        registerUser(pbUserId)
        r = attempt()
    }
    if (r.status !== 200 || !r.json.access_token) {
        console.log("matrix login failed", r.status, JSON.stringify(r.json))
        throw new ApiError(502, "Matrix login failed", null)
    }
    return {user_id: r.json.user_id, access_token: r.json.access_token, device_id: r.json.device_id}
}

function logout(token) {
    try { mx("POST", "/_matrix/client/v3/logout", token, {}) } catch (e) {}
}

// Run fn(session) with a throwaway server-side login (own device, logged out afterwards)
// so it never invalidates the token the phone is using.
function withSession(pbUserId, fn) {
    const s = login(pbUserId)
    try { return fn(s) } finally { logout(s.access_token) }
}

function displayNameFor(user) {
    const uid = user.id, role = user.getString("role")
    let name = ""
    try {
        const coll = role === "practitioner" ? "practitioners" : "patients"
        const p = $app.findFirstRecordByData(coll, "user", uid)
        name = p.getString("fullName") || p.getString("FullName") || p.getString("name") || ""
    } catch (e) {}
    if (!name) name = user.getString("name")
    if (!name) name = (user.getString("email") || "user").split("@")[0]
    name = name.trim()
    if (role === "practitioner" && !/^dr\.?\s/i.test(name)) name = "Dr. " + name
    return name
}

function setDisplayName(session, name) {
    mx("PUT", "/_matrix/client/v3/profile/" + encodeURIComponent(session.user_id) + "/displayname",
        session.access_token, {displayname: name})
}

module.exports = {cfg, mx, mxUserId, login, logout, withSession, displayNameFor, setDisplayName, hmacSha1Hex}
