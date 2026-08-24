// port-lint: source sentry/src/lib.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.sentry

import kotlin.time.Clock
import kotlin.time.Instant

public enum class SessionMode {
    Application,
    Request,
}

public enum class SessionStatus {
    Ok,
    Exited,
    Crashed,
    Abnormal,
}

public data class Session(
    public val distinctId: String,
    public val sessionId: Uuid = Uuid.random(),
    public var status: SessionStatus = SessionStatus.Ok,
    public val started: Instant = Clock.System.now(),
    public var timestamp: Instant = Clock.System.now(),
    public var seq: Long = 0,
    public var errors: Long = 0,
    public var user: User? = null,
    public var release: String? = null,
    public var environment: String? = null,
    public var userAgent: String? = null,
    public var ipAddress: String? = null,
) {
    public fun close(status: SessionStatus = SessionStatus.Exited) {
        this.status = status
        this.timestamp = Clock.System.now()
    }
}
