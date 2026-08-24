// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

public data class SamplingContext(
    public val transactionContext: TransactionContext,
    public val customSamplingContext: Map<String, String> = emptyMap(),
) {
    public fun name(): String = transactionContext.name
    public fun op(): String = transactionContext.op
    public fun traceId(): TraceId = transactionContext.traceId
    public fun spanId(): SpanId = transactionContext.spanId

    public companion object {
        public fun from(transactionContext: TransactionContext): SamplingContext =
            SamplingContext(transactionContext)
    }
}

public data class ClientOptions(
    public var dsn: Dsn? = null,
    public var release: String? = null,
    public var environment: String? = null,
    public var serverName: String? = null,
    public var sampleRate: Double = 1.0,
    public var tracesSampleRate: Double = 0.0,
    public var tracesSampler: ((SamplingContext) -> Double)? = null,
    public var maxBreadcrumbs: Int = 100,
    public var attachStacktrace: Boolean = false,
    public var sendDefaultPii: Boolean = false,
    public var defaultIntegrations: Boolean = true,
    public var integrations: List<Integration> = emptyList(),
    public var beforeSend: ((Event) -> Event?)? = null,
    public var beforeBreadcrumb: ((Breadcrumb) -> Breadcrumb?)? = null,
    public var transport: TransportFactory? = null,
    public var httpProxy: String? = null,
    public var httpsProxy: String? = null,
    public var acceptInvalidCerts: Boolean = false,
    public var enableLogs: Boolean = false,
    public var autoSessionTracking: Boolean = false,
    public var sessionMode: SessionMode = SessionMode.Application,
    public var shutdownTimeout: Long = 2000L,
) {
    public fun addIntegration(integration: Integration): ClientOptions = apply {
        integrations = integrations + integration
    }

    public companion object {
        public fun new(): ClientOptions = ClientOptions()
    }
}
