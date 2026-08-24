// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

public fun captureMessage(message: String, level: Level = Level.Info): Uuid? =
    Hub.current().captureMessage(message, level)

public fun captureEvent(event: Event): Uuid? =
    Hub.current().captureEvent(event)

public fun captureError(error: Throwable): Uuid? =
    Hub.current().captureError(error)

public fun addBreadcrumb(breadcrumb: Breadcrumb) {
    Hub.current().addBreadcrumb(breadcrumb)
}

public fun addBreadcrumb(factory: () -> Any?) {
    Hub.current().addBreadcrumb(factory)
}

public fun configureScope(block: (Scope) -> Unit) {
    Hub.current().configureScope(block)
}

public fun withScope(setup: (Scope) -> Unit, block: () -> Unit) {
    Hub.current().withScope(setup, block)
}

public fun <R> withScope(block: (Scope) -> R): R = Hub.current().withScope(block)

public fun lastEventId(): Uuid? = Hub.current().lastEventId()

public fun startTransaction(context: TransactionContext): Transaction =
    Hub.current().startTransaction(context)

public fun startSession() {}

public fun endSession() {}

public fun loggerInfo(body: String, attributes: Map<String, LogAttribute> = emptyMap()) {
    val attrs = mutableMapOf<String, LogAttribute>()
    attrs["sentry.sdk.name"] = LogAttribute.from("sentry.kotlin")
    attrs["sentry.sdk.version"] = LogAttribute.from("0.1.0")
    attrs.putAll(attributes)
    val log = Log(
        level = LogLevel.Info,
        body = body,
        traceId = Hub.current().scope().propagationContext.traceId,
        attributes = attrs,
    )
    Hub.current().captureLog(log)
}

public fun loggerWarn(body: String, attributes: Map<String, LogAttribute> = emptyMap()) {
    val attrs = mutableMapOf<String, LogAttribute>()
    attrs["sentry.sdk.name"] = LogAttribute.from("sentry.kotlin")
    attrs["sentry.sdk.version"] = LogAttribute.from("0.1.0")
    attrs.putAll(attributes)
    val log = Log(
        level = LogLevel.Warn,
        body = body,
        traceId = Hub.current().scope().propagationContext.traceId,
        attributes = attrs,
    )
    Hub.current().captureLog(log)
}

public fun loggerError(body: String, attributes: Map<String, LogAttribute> = emptyMap()) {
    val attrs = mutableMapOf<String, LogAttribute>()
    attrs["sentry.sdk.name"] = LogAttribute.from("sentry.kotlin")
    attrs["sentry.sdk.version"] = LogAttribute.from("0.1.0")
    attrs.putAll(attributes)
    val log = Log(
        level = LogLevel.Error,
        body = body,
        traceId = Hub.current().scope().propagationContext.traceId,
        attributes = attrs,
    )
    Hub.current().captureLog(log)
}

public fun loggerDebug(body: String, attributes: Map<String, LogAttribute> = emptyMap()) {
    val attrs = mutableMapOf<String, LogAttribute>()
    attrs["sentry.sdk.name"] = LogAttribute.from("sentry.kotlin")
    attrs["sentry.sdk.version"] = LogAttribute.from("0.1.0")
    attrs.putAll(attributes)
    val log = Log(
        level = LogLevel.Debug,
        body = body,
        traceId = Hub.current().scope().propagationContext.traceId,
        attributes = attrs,
    )
    Hub.current().captureLog(log)
}
