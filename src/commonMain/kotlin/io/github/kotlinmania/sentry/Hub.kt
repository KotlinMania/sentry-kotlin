// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

public class ScopeGuard(
    private val hub: Hub,
    private val expectedDepth: Int,
) {
    private var closed = false

    public fun close() {
        if (!closed) {
            closed = true
            hub.popScopeInternal(expectedDepth)
        }
    }
}

public class Hub(
    client: Client? = null,
    scope: Scope = Scope(),
) {
    public data class Layer(
        public var client: Client?,
        public val scope: Scope,
    )

    private val stack: MutableList<Layer> = mutableListOf(Layer(client, scope))
    private var lastEventId: Uuid? = null

    public fun client(): Client? = stack.lastOrNull()?.client

    public fun scope(): Scope = stack.last().scope

    public fun bindClient(client: Client?) {
        stack.last().client = client
    }

    public fun pushScope(): ScopeGuard {
        val currentLayer = stack.last()
        val newScope = currentLayer.scope.clone()
        stack.add(Layer(currentLayer.client, newScope))
        return ScopeGuard(this, stack.size)
    }

    public fun popScope() {
        if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
        }
    }

    internal fun popScopeInternal(expectedDepth: Int) {
        if (stack.size != expectedDepth) {
            popScope()
            val ex = ExceptionValue(
                ty = "panic",
                value = "Popped scope guard out of order",
            )
            val event = Event(
                level = Level.Fatal,
                exception = listOf(ex),
            )
            captureEvent(event)
            throw IllegalStateException("Popped scope guard out of order")
        }
        popScope()
    }

    public fun withScope(setup: (Scope) -> Unit, block: () -> Unit) {
        val guard = pushScope()
        try {
            setup(scope())
            block()
        } finally {
            guard.close()
        }
    }

    public fun <R> withScope(block: (Scope) -> R): R {
        val guard = pushScope()
        try {
            return block(scope())
        } finally {
            guard.close()
        }
    }

    public fun configureScope(block: (Scope) -> Unit) {
        block(scope())
    }

    public fun captureEvent(event: Event): Uuid? {
        val currentClient = client()
        val eventId = currentClient?.captureEvent(event, scope())
        if (eventId != null) {
            lastEventId = eventId
        }
        return eventId
    }

    public fun captureMessage(message: String, level: Level = Level.Info): Uuid? {
        val event = Event(
            message = message,
            level = level,
        )
        return captureEvent(event)
    }

    public fun captureError(error: Throwable): Uuid? {
        val ex = ExceptionValue(
            ty = error::class.simpleName ?: "Throwable",
            value = error.message,
        )
        val event = Event(
            level = Level.Error,
            exception = listOf(ex),
        )
        return captureEvent(event)
    }

    public fun captureLog(log: Log) {
        val currentClient = client() ?: return
        val item = EnvelopeItem.ItemContainer(ItemContainer.Logs(listOf(log)))
        val envelope = Envelope(
            headers = EnvelopeHeaders(eventId = Uuid.random()),
            items = listOf(item),
        )
        currentClient.captureEnvelope(envelope)
    }

    public fun addBreadcrumb(breadcrumb: Breadcrumb) {
        val beforeBreadcrumb = client()?.options()?.beforeBreadcrumb
        val processed = if (beforeBreadcrumb != null) {
            beforeBreadcrumb(breadcrumb)
        } else {
            breadcrumb
        }
        if (processed != null) {
            val maxBreadcrumbs = client()?.options()?.maxBreadcrumbs ?: 100
            scope().addBreadcrumb(processed, maxBreadcrumbs)
        }
    }

    public fun addBreadcrumb(factory: () -> Any?) {
        val result = factory() ?: return
        when (result) {
            is Breadcrumb -> addBreadcrumb(result)
            is List<*> -> {
                for (item in result) {
                    if (item is Breadcrumb) {
                        addBreadcrumb(item)
                    }
                }
            }
        }
    }

    public fun lastEventId(): Uuid? = lastEventId

    public fun startTransaction(context: TransactionContext): Transaction {
        val tx = Transaction(context, this)
        return tx
    }

    public companion object {
        private var currentHub: Hub = Hub()

        public fun current(): Hub = currentHub

        public fun setCurrent(hub: Hub) {
            currentHub = hub
        }

        public fun with(block: (Hub) -> Unit) {
            block(current())
        }

        public fun run(hub: Hub, block: () -> Unit) {
            val previous = currentHub
            currentHub = hub
            try {
                block()
            } finally {
                currentHub = previous
            }
        }
    }
}
