// port-lint: source transports/mod.rs
package io.github.kotlinmania.sentry

import kotlin.time.Duration

public interface Transport {
    public fun sendEnvelope(envelope: Envelope)

    public fun flush(timeout: Duration): Boolean = true

    public fun shutdown(timeout: Duration): Boolean = flush(timeout)
}

public fun interface TransportFactory {
    public fun createTransport(options: ClientOptions): Transport
}

/**
 * The default HTTP transport type.
 */
public typealias HttpTransport = DefaultTransport

public class DefaultTransport(
    public val options: ClientOptions,
) : Transport {
    private val rateLimiter = RateLimiter.new()

    override fun sendEnvelope(envelope: Envelope) {
        val filtered = rateLimiter.filterEnvelope(envelope) ?: return
    }

    override fun flush(timeout: Duration): Boolean = true
}

public class DefaultTransportFactory : TransportFactory {
    override fun createTransport(options: ClientOptions): Transport = DefaultTransport(options)
}
