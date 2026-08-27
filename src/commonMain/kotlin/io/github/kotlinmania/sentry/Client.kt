// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

public class Client(
    private val options: ClientOptions,
) {
    public val transport: Transport? = options.transport?.createTransport(options)

    public fun isEnabled(): Boolean = options.dsn != null

    public fun dsn(): Dsn? = options.dsn

    public fun options(): ClientOptions = options

    public fun captureEvent(event: Event, scope: Scope? = null): Uuid? {
        if (!isEnabled()) return null

        var processedEvent: Event = event
        if (scope != null) {
            val fromScope = scope.applyToEvent(processedEvent) ?: return null
            processedEvent = fromScope
        }

        val beforeSend = options.beforeSend
        if (beforeSend != null) {
            val fromBeforeSend = beforeSend(processedEvent) ?: return null
            processedEvent = fromBeforeSend
        }

        if (processedEvent.release == null) processedEvent.release = options.release
        if (processedEvent.environment == null) processedEvent.environment = options.environment
        if (processedEvent.serverName == null) processedEvent.serverName = options.serverName

        val dsc =
            if (options.tracesSampleRate > 0.0 || options.tracesSampler != null) {
                val traceCtx = processedEvent.contexts["trace"] as? Context.Trace
                val traceId = traceCtx?.traceId ?: TraceId.random()
                val sampleRate: Double =
                    if (options.tracesSampler != null) {
                        val ctx =
                            TransactionContext(
                                name = processedEvent.transaction ?: "",
                                op = traceCtx?.op ?: "",
                                traceId = traceId,
                                spanId = traceCtx?.spanId ?: SpanId.random(),
                            )
                        options.tracesSampler!!.invoke(SamplingContext.from(ctx))
                    } else {
                        options.tracesSampleRate
                    }
                DynamicSamplingContext(
                    traceId = traceId,
                    publicKey = options.dsn?.publicKey,
                    sampleRate = sampleRate,
                    sampled = sampleRate > 0.0,
                    release = options.release,
                    environment = options.environment,
                    transaction = processedEvent.transaction,
                )
            } else {
                null
            }

        val envelope = Envelope.fromEvent(processedEvent, dsc)
        val finalEnvelope =
            if (scope != null && scope.attachments.isNotEmpty()) {
                Envelope(envelope.headers, envelope.items + scope.attachments.map { EnvelopeItem.Attachment(it) })
            } else {
                envelope
            }

        transport?.sendEnvelope(finalEnvelope)
        return processedEvent.eventId
    }

    public fun captureEnvelope(envelope: Envelope): Uuid? {
        if (!isEnabled()) return null
        transport?.sendEnvelope(envelope)
        return envelope.uuid() ?: Uuid.random()
    }

    public fun close(timeout: Long? = null) {
        // No-op or drain for transport
    }

    public companion object {
        public fun from(options: ClientOptions): Client = Client(options)

        public fun fromConfig(dsn: String?): Client =
            if (dsn != null) {
                val parsed = Dsn.parse(dsn)
                Client(ClientOptions(dsn = parsed))
            } else {
                Client(ClientOptions())
            }

        public fun fromConfig(options: ClientOptions): Client = Client(options)

        public fun fromConfig(config: Pair<String, ClientOptions>): Client {
            val opts = config.second
            opts.dsn = Dsn.parse(config.first)
            return Client(opts)
        }
    }
}
