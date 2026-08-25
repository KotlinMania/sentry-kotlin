@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.sentry

import kotlin.time.Clock
import kotlin.time.Instant

public data class TransactionContext(
    public val name: String,
    public val op: String,
    public val traceId: TraceId = TraceId.random(),
    public val spanId: SpanId = SpanId.random(),
    public val parentSpanId: SpanId? = null,
    public val sampled: Boolean? = null,
) {
    public companion object {
        public fun new(name: String, op: String): TransactionContext = TransactionContext(name, op)
    }
}

public class Span(
    public val traceId: TraceId,
    public val spanId: SpanId,
    public val parentSpanId: SpanId? = null,
    public val op: String,
    public var spanDescription: String? = null,
    public var status: String? = null,
    public val startTimestamp: Instant = Clock.System.now(),
    public var timestamp: Instant? = null,
) {
    public var children: List<Span> = emptyList()

    public fun startChild(op: String, description: String? = null): Span {
        val child =
            Span(
                traceId = traceId,
                spanId = SpanId.random(),
                parentSpanId = spanId,
                op = op,
                spanDescription = description,
            )
        children = children + child
        return child
    }

    public fun finish() {
        if (timestamp == null) {
            timestamp = Clock.System.now()
        }
    }
}

public class Transaction(
    public val context: TransactionContext,
    public var hub: Hub? = null,
) {
    public val traceId: TraceId = context.traceId
    public val spanId: SpanId = context.spanId
    public val name: String = context.name
    public val op: String = context.op
    public var transactionDescription: String? = null
    public var status: String? = null
    public val startTimestamp: Instant = Clock.System.now()
    public var timestamp: Instant? = null
    public var spans: List<Span> = emptyList()

    public fun startChild(op: String, description: String? = null): Span {
        val child =
            Span(
                traceId = traceId,
                spanId = SpanId.random(),
                parentSpanId = spanId,
                op = op,
                spanDescription = description,
            )
        spans = spans + child
        return child
    }

    public fun finish(): Event? {
        if (timestamp == null) {
            timestamp = Clock.System.now()
        }
        val event =
            Event(
                transaction = name,
                level = Level.Info,
                contexts =
                    mapOf(
                        "trace" to
                            Context.Trace(
                                traceId = traceId,
                                spanId = spanId,
                                parentSpanId = context.parentSpanId,
                                op = op,
                                traceDescription = transactionDescription,
                                status = status,
                            ),
                    ),
            )
        hub?.captureEvent(event)
        return event
    }
}
