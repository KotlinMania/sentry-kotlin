// port-lint: source sentry/src/lib.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.sentry

import kotlin.time.Clock
import kotlin.time.Instant

public enum class LogLevel(public val value: String) {
    Trace("trace"),
    Debug("debug"),
    Info("info"),
    Warn("warn"),
    Error("error"),
    Fatal("fatal");
}

public data class LogAttribute(public val value: String) {
    public companion object {
        public fun from(value: Any): LogAttribute = LogAttribute(value.toString())
    }
}

public data class Log(
    public val body: String,
    public val level: LogLevel = LogLevel.Info,
    public val timestamp: Instant = Clock.System.now(),
    public val traceId: TraceId? = null,
    public val severityNumber: Int? = null,
    public val attributes: Map<String, LogAttribute> = emptyMap(),
)

public data class SdkInfo(
    public val name: String = "sentry.kotlin",
    public val version: String = "0.1.0",
    public val packages: List<String> = emptyList(),
)

public data class Breadcrumb(
    public val timestamp: Instant = Clock.System.now(),
    public val message: String? = null,
    public val level: Level = Level.Info,
    public val category: String? = null,
    public val ty: String? = null,
    public val data: Map<String, String> = emptyMap(),
)

public data class User(
    public val id: String? = null,
    public val email: String? = null,
    public val ipAddress: String? = null,
    public val username: String? = null,
    public val data: Map<String, String> = emptyMap(),
)

public data class ExceptionValue(
    public val ty: String,
    public val value: String? = null,
    public val module: String? = null,
)

public data class ThreadInfo(
    public val id: Long? = null,
    public val name: String? = null,
    public val crashed: Boolean = false,
    public val current: Boolean = false,
)

public class Attachment(
    public val buffer: ByteArray,
    public val filename: String,
    public val contentType: String? = null,
    public val attachmentType: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Attachment) return false
        return filename == other.filename && buffer.contentEquals(other.buffer)
    }

    override fun hashCode(): Int = filename.hashCode() * 31 + buffer.contentHashCode()
}

public sealed class Context {
    public data class Trace(
        public val traceId: TraceId,
        public val spanId: SpanId,
        public val parentSpanId: SpanId? = null,
        public val op: String? = null,
        public val traceDescription: String? = null,
        public val status: String? = null,
    ) : Context()

    public data class Device(
        public val name: String? = null,
        public val family: String? = null,
        public val model: String? = null,
        public val memorySize: Long? = null,
        public val freeMemory: Long? = null,
    ) : Context()

    public data class Os(
        public val name: String? = null,
        public val version: String? = null,
        public val build: String? = null,
        public val kernelVersion: String? = null,
    ) : Context()

    public data class Runtime(
        public val name: String? = null,
        public val version: String? = null,
    ) : Context()

    public data class App(
        public val appStartTime: Instant? = null,
        public val deviceAppHash: String? = null,
        public val buildType: String? = null,
        public val appIdentifier: String? = null,
        public val appName: String? = null,
        public val appVersion: String? = null,
        public val appBuild: String? = null,
    ) : Context()

    public data class Browser(
        public val name: String? = null,
        public val version: String? = null,
    ) : Context()

    public data class Custom(
        public val data: Map<String, String> = emptyMap(),
    ) : Context()
}

public data class EnvelopeHeaders(
    public val eventId: Uuid? = null,
    public val dsn: Dsn? = null,
    public val sentAt: Instant = Clock.System.now(),
    public val trace: DynamicSamplingContext? = null,
) {
    public fun withEventId(eventId: Uuid): EnvelopeHeaders = copy(eventId = eventId)
    public fun withTrace(trace: DynamicSamplingContext?): EnvelopeHeaders = copy(trace = trace)

    public companion object {
        public fun new(): EnvelopeHeaders = EnvelopeHeaders()
    }
}

public data class DynamicSamplingContext(
    public val traceId: TraceId,
    public val publicKey: String? = null,
    public val sampleRate: Double? = null,
    public val release: String? = null,
    public val environment: String? = null,
    public val transaction: String? = null,
    public val userSegment: String? = null,
    public val sampled: Boolean? = null,
) {
    public fun withTraceId(traceId: TraceId): DynamicSamplingContext = copy(traceId = traceId)
    public fun withPublicKey(publicKey: String?): DynamicSamplingContext = copy(publicKey = publicKey)
    public fun withSampleRate(sampleRate: Double?): DynamicSamplingContext = copy(sampleRate = sampleRate)
    public fun withRelease(release: String?): DynamicSamplingContext = copy(release = release)
    public fun withEnvironment(environment: String?): DynamicSamplingContext = copy(environment = environment)
    public fun withTransaction(transaction: String?): DynamicSamplingContext = copy(transaction = transaction)
    public fun withUserSegment(userSegment: String?): DynamicSamplingContext = copy(userSegment = userSegment)
    public fun withSampled(sampled: Boolean?): DynamicSamplingContext = copy(sampled = sampled)

    public fun toHeader(): String {
        val pairs = mutableListOf<String>()
        pairs.add("sentry-trace_id=${traceId.value}")
        if (publicKey != null) pairs.add("sentry-public_key=$publicKey")
        if (sampleRate != null) pairs.add("sentry-sample_rate=$sampleRate")
        if (release != null) pairs.add("sentry-release=$release")
        if (environment != null) pairs.add("sentry-environment=$environment")
        if (transaction != null) pairs.add("sentry-transaction=$transaction")
        if (userSegment != null) pairs.add("sentry-user_segment=$userSegment")
        if (sampled != null) pairs.add("sentry-sampled=$sampled")
        return pairs.joinToString(",")
    }

    public companion object {
        public fun new(): DynamicSamplingContext = DynamicSamplingContext(traceId = TraceId.random())

        public fun fromHeader(header: String): DynamicSamplingContext? {
            var traceId: TraceId? = null
            var publicKey: String? = null
            var sampleRate: Double? = null
            var release: String? = null
            var environment: String? = null
            var transaction: String? = null
            var userSegment: String? = null
            var sampled: Boolean? = null

            for (part in header.split(',')) {
                val kv = part.trim().split('=', limit = 2)
                if (kv.size != 2) continue
                val key = kv[0].removePrefix("sentry-")
                val value = kv[1]
                when (key) {
                    "trace_id" -> traceId = TraceId.from(value)
                    "public_key" -> publicKey = value
                    "sample_rate" -> sampleRate = value.toDoubleOrNull()
                    "release" -> release = value
                    "environment" -> environment = value
                    "transaction" -> transaction = value
                    "user_segment" -> userSegment = value
                    "sampled" -> sampled = value.toBooleanStrictOrNull()
                }
            }

            return if (traceId != null) {
                DynamicSamplingContext(
                    traceId = traceId,
                    publicKey = publicKey,
                    sampleRate = sampleRate,
                    release = release,
                    environment = environment,
                    transaction = transaction,
                    userSegment = userSegment,
                    sampled = sampled,
                )
            } else null
        }
    }
}

public sealed class ItemContainer {
    public data class Logs(public val logs: List<Log>) : ItemContainer()
}

public sealed class EnvelopeItem {
    public data class Event(public val event: io.github.kotlinmania.sentry.Event) : EnvelopeItem()
    public data class Attachment(public val attachment: io.github.kotlinmania.sentry.Attachment) : EnvelopeItem()
    public data class ItemContainer(public val container: io.github.kotlinmania.sentry.ItemContainer) : EnvelopeItem()
}

public data class Event(
    public val eventId: Uuid = Uuid.random(),
    public val timestamp: Instant = Clock.System.now(),
    public var message: String? = null,
    public var level: Level = Level.Info,
    public var logger: String? = null,
    public var platform: String = "other",
    public var serverName: String? = null,
    public var release: String? = null,
    public var environment: String? = null,
    public var sdk: SdkInfo? = SdkInfo(),
    public var tags: Map<String, String> = emptyMap(),
    public var extra: Map<String, String> = emptyMap(),
    public var contexts: Map<String, Context> = emptyMap(),
    public var user: User? = null,
    public var fingerprint: List<String> = emptyList(),
    public var breadcrumbs: List<Breadcrumb> = emptyList(),
    public var exception: List<ExceptionValue> = emptyList(),
    public var threads: List<ThreadInfo> = emptyList(),
    public var transaction: String? = null,
)

public data class Envelope(
    public val headers: EnvelopeHeaders = EnvelopeHeaders(),
    public val items: List<EnvelopeItem> = emptyList(),
) {
    public fun uuid(): Uuid? = headers.eventId
    public fun event(): Event? = items.filterIsInstance<EnvelopeItem.Event>().firstOrNull()?.event

    public companion object {
        public fun fromEvent(event: Event, dsc: DynamicSamplingContext? = null): Envelope {
            val headers = EnvelopeHeaders(eventId = event.eventId, trace = dsc)
            return Envelope(headers, listOf(EnvelopeItem.Event(event)))
        }
    }
}
