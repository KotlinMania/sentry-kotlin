// port-lint: source sentry/src/lib.rs
package io.github.kotlinmania.sentry

public class TestTransport : Transport {
    private val envelopes: MutableList<Envelope> = mutableListOf()
    private val events: MutableList<Event> = mutableListOf()

    override fun sendEnvelope(envelope: Envelope) {
        envelopes.add(envelope)
        val ev = envelope.event()
        if (ev != null) {
            events.add(ev)
        }
    }

    public fun fetchAndClearEvents(): List<Event> {
        val result = events.toList()
        events.clear()
        return result
    }

    public fun fetchAndClearEnvelopes(): List<Envelope> {
        val result = envelopes.toList()
        envelopes.clear()
        return result
    }

    public companion object {
        public fun new(): TestTransport = TestTransport()
    }
}

public object TestHelpers {
    public fun withCapturedEvents(block: () -> Unit): List<Event> = withCapturedEventsOptions(block, ClientOptions())

    public fun withCapturedEventsOptions(block: () -> Unit, options: ClientOptions): List<Event> {
        val transport = TestTransport()
        val opts =
            options.copy(
                dsn = options.dsn ?: Dsn.parse("https://public@example.com/1"),
                transport = { transport },
            )
        val client = Client(opts)
        val hub = Hub(client = client)
        var caught: Throwable? = null
        Hub.run(hub) {
            try {
                block()
            } catch (t: Throwable) {
                caught = t
            }
        }
        val ex = caught
        if (ex != null) throw ex
        return transport.fetchAndClearEvents()
    }

    public fun withCapturedEnvelopes(block: () -> Unit): List<Envelope> = withCapturedEnvelopesOptions(block, ClientOptions())

    public fun withCapturedEnvelopesOptions(block: () -> Unit, options: ClientOptions): List<Envelope> {
        val transport = TestTransport()
        val opts =
            options.copy(
                dsn = options.dsn ?: Dsn.parse("https://public@example.com/1"),
                transport = { transport },
            )
        val client = Client(opts)
        val hub = Hub(client = client)
        var caught: Throwable? = null
        Hub.run(hub) {
            try {
                block()
            } catch (t: Throwable) {
                caught = t
            }
        }
        val ex = caught
        if (ex != null) throw ex
        return transport.fetchAndClearEnvelopes()
    }
}
