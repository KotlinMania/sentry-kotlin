// port-lint: source lib.rs
package io.github.kotlinmania.sentry

public class Scope {
    public var tags: Map<String, String> = emptyMap()
    public var extra: Map<String, String> = emptyMap()
    public var contexts: Map<String, Context> = emptyMap()
    public var breadcrumbs: List<Breadcrumb> = emptyList()
    public var user: User? = null
    public var level: Level? = null
    public var fingerprint: List<String> = emptyList()
    public var attachments: List<Attachment> = emptyList()
    public var eventProcessors: List<(Event) -> Event?> = emptyList()
    public var span: Span? = null
    public val propagationContext: PropagationContext = PropagationContext.new()

    public fun addBreadcrumb(breadcrumb: Breadcrumb, maxBreadcrumbs: Int = 100) {
        val updated = breadcrumbs + breadcrumb
        breadcrumbs =
            if (updated.size > maxBreadcrumbs) {
                updated.takeLast(maxBreadcrumbs)
            } else {
                updated
            }
    }

    public fun clearBreadcrumbs() {
        breadcrumbs = emptyList()
    }

    public fun setTag(key: String, value: String) {
        tags = tags + (key to value)
    }

    public fun removeTag(key: String) {
        tags = tags - key
    }

    public fun setExtra(key: String, value: String) {
        extra = extra + (key to value)
    }

    public fun removeExtra(key: String) {
        extra = extra - key
    }

    public fun setContext(key: String, context: Context) {
        contexts = contexts + (key to context)
    }

    public fun removeContext(key: String) {
        contexts = contexts - key
    }

    public fun addAttachment(attachment: Attachment) {
        attachments = attachments + attachment
    }

    public fun clearAttachments() {
        attachments = emptyList()
    }

    public fun addEventProcessor(processor: (Event) -> Event?) {
        eventProcessors = eventProcessors + processor
    }

    public fun clear() {
        breadcrumbs = emptyList()
        tags = emptyMap()
        extra = emptyMap()
        contexts = emptyMap()
        user = null
        level = null
        fingerprint = emptyList()
        attachments = emptyList()
        span = null
    }

    public fun clone(): Scope {
        val cloned = Scope()
        cloned.tags = tags
        cloned.extra = extra
        cloned.contexts = contexts
        cloned.breadcrumbs = breadcrumbs
        cloned.user = user
        cloned.level = level
        cloned.fingerprint = fingerprint
        cloned.attachments = attachments
        cloned.eventProcessors = eventProcessors
        cloned.span = span
        return cloned
    }

    public fun applyToEvent(event: Event): Event? {
        if (tags.isNotEmpty()) {
            event.tags = tags + event.tags
        }
        if (extra.isNotEmpty()) {
            event.extra = extra + event.extra
        }
        if (contexts.isNotEmpty()) {
            event.contexts = contexts + event.contexts
        }
        if (user != null && event.user == null) {
            event.user = user
        }
        val lvl = level
        if (lvl != null && event.level == Level.Info) {
            event.level = lvl
        }
        if (event.breadcrumbs.isEmpty() && breadcrumbs.isNotEmpty()) {
            event.breadcrumbs = breadcrumbs
        }
        val s = span
        if (s != null) {
            event.contexts = event.contexts + (
                "trace" to
                    Context.Trace(
                        traceId = s.traceId,
                        spanId = s.spanId,
                        parentSpanId = s.parentSpanId,
                        op = s.op,
                        traceDescription = s.spanDescription,
                        status = s.status,
                    )
            )
        } else if (!event.contexts.containsKey("trace")) {
            event.contexts = event.contexts + (
                "trace" to
                    Context.Trace(
                        traceId = propagationContext.traceId,
                        spanId = propagationContext.spanId,
                    )
            )
        }
        var processedEvent: Event? = event
        for (processor in eventProcessors) {
            val next = processor(processedEvent!!) ?: return null
            processedEvent = next
        }
        return processedEvent
    }
}
