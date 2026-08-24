// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

import kotlin.random.Random

public enum class Scheme(public val protocol: String) {
    Http("http"),
    Https("https");

    public companion object {
        public fun fromString(value: String): Scheme = when (value.lowercase()) {
            "http" -> Http
            "https" -> Https
            else -> throw IllegalArgumentException("Unsupported scheme: $value")
        }
    }
}

public data class ProjectId(public val value: String) {
    override fun toString(): String = value
}

public enum class Level(public val value: String) {
    Debug("debug"),
    Info("info"),
    Warning("warning"),
    Error("error"),
    Fatal("fatal");

    public companion object {
        public fun fromString(value: String): Level = when (value.lowercase()) {
            "debug" -> Debug
            "info" -> Info
            "warning", "warn" -> Warning
            "error" -> Error
            "fatal" -> Fatal
            else -> Info
        }
    }
}

public data class Uuid(public val value: String) {
    public fun toSimple(): String = value.replace("-", "")

    override fun toString(): String = value

    public companion object {
        public fun nil(): Uuid = Uuid("00000000-0000-0000-0000-000000000000")

        public fun random(): Uuid {
            val randomBytes = Random.nextBytes(16)
            randomBytes[6] = ((randomBytes[6].toInt() and 0x0f) or 0x40).toByte() // version 4
            randomBytes[8] = ((randomBytes[8].toInt() and 0x3f) or 0x80).toByte() // variant RFC 4122
            val hex = randomBytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
            val formatted = "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20, 32)}"
            return Uuid(formatted)
        }

        public fun from(value: String): Uuid {
            val clean = value.trim()
            return if (clean.length == 32) {
                Uuid("${clean.substring(0, 8)}-${clean.substring(8, 12)}-${clean.substring(12, 16)}-${clean.substring(16, 20)}-${clean.substring(20, 32)}")
            } else {
                Uuid(clean)
            }
        }
    }
}

public data class TraceId(public val value: String) {
    override fun toString(): String = value

    public companion object {
        public fun random(): TraceId {
            val randomBytes = Random.nextBytes(16)
            return TraceId(randomBytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') })
        }

        public fun from(value: String): TraceId = TraceId(value.replace("-", "").lowercase())
    }
}

public data class SpanId(public val value: String) {
    override fun toString(): String = value

    public companion object {
        public fun random(): SpanId {
            val randomBytes = Random.nextBytes(8)
            return SpanId(randomBytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') })
        }

        public fun from(value: String): SpanId = SpanId(value.replace("-", "").lowercase())
    }
}

public data class PropagationContext(
    public val traceId: TraceId = TraceId.random(),
    public val spanId: SpanId = SpanId.random(),
    public val parentSpanId: SpanId? = null,
) {
    public companion object {
        public fun new(): PropagationContext = PropagationContext()
    }
}

public data class Dsn(
    public val scheme: Scheme,
    public val publicKey: String,
    public val secretKey: String? = null,
    public val host: String,
    public val port: Int? = null,
    public val path: String = "",
    public val projectId: ProjectId,
) {
    public fun envelopeApiUrl(): String {
        val portPart = if (port != null) ":$port" else ""
        val pathPart = if (path.isNotEmpty()) {
            if (path.startsWith("/")) path else "/$path"
        } else ""
        val normalizedPath = if (pathPart.endsWith("/")) pathPart else "$pathPart/"
        return "${scheme.protocol}://$host$portPart${normalizedPath}api/${projectId.value}/envelope/"
    }

    public fun toAuth(userAgent: String? = null): String {
        val authClient = userAgent ?: "sentry.kotlin/0.1.0"
        val secretPart = if (secretKey != null) ", sentry_secret=$secretKey" else ""
        return "Sentry sentry_version=7, sentry_client=$authClient, sentry_key=$publicKey$secretPart"
    }

    override fun toString(): String {
        val auth = if (secretKey != null) "$publicKey:$secretKey" else publicKey
        val portPart = if (port != null) ":$port" else ""
        val pathPart = if (path.isNotEmpty()) {
            if (path.startsWith("/")) path else "/$path"
        } else ""
        val normalizedPath = if (pathPart.endsWith("/")) pathPart else if (pathPart.isNotEmpty()) "$pathPart/" else "/"
        return "${scheme.protocol}://$auth@$host$portPart$normalizedPath${projectId.value}"
    }

    public companion object {
        public fun parse(dsnString: String): Dsn {
            val trimmed = dsnString.trim()
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("DSN string cannot be empty")
            }

            val schemeEnd = trimmed.indexOf("://")
            if (schemeEnd == -1) {
                throw IllegalArgumentException("Invalid DSN scheme: $trimmed")
            }
            val schemeStr = trimmed.substring(0, schemeEnd)
            val scheme = Scheme.fromString(schemeStr)

            val rest = trimmed.substring(schemeEnd + 3)
            val atIndex = rest.indexOf('@')
            if (atIndex == -1) {
                throw IllegalArgumentException("Invalid DSN auth part: missing @ in $trimmed")
            }
            val authPart = rest.substring(0, atIndex)
            val afterAuth = rest.substring(atIndex + 1)

            val (publicKey, secretKey) = if (authPart.contains(':')) {
                val parts = authPart.split(':', limit = 2)
                Pair(parts[0], parts[1])
            } else {
                Pair(authPart, null)
            }

            val slashIndex = afterAuth.indexOf('/')
            if (slashIndex == -1) {
                throw IllegalArgumentException("Invalid DSN path: missing project ID in $trimmed")
            }
            val hostPort = afterAuth.substring(0, slashIndex)
            val pathAndProject = afterAuth.substring(slashIndex + 1)

            val (host, port) = if (hostPort.contains(':')) {
                val parts = hostPort.split(':', limit = 2)
                Pair(parts[0], parts[1].toIntOrNull())
            } else {
                Pair(hostPort, null)
            }

            val lastSlash = pathAndProject.lastIndexOf('/')
            val (path, projectIdStr) = if (lastSlash != -1) {
                Pair(pathAndProject.substring(0, lastSlash), pathAndProject.substring(lastSlash + 1))
            } else {
                Pair("", pathAndProject)
            }

            if (projectIdStr.isEmpty()) {
                throw IllegalArgumentException("Project ID cannot be empty in DSN: $trimmed")
            }

            return Dsn(
                scheme = scheme,
                publicKey = publicKey,
                secretKey = secretKey,
                host = host,
                port = port,
                path = path,
                projectId = ProjectId(projectIdStr),
            )
        }
    }
}
