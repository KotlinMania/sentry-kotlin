// port-lint: source sentry/src/init.rs
package io.github.kotlinmania.sentry

public class ClientInitGuard(
    public val client: Client,
) : AutoCloseable {
    public fun isEnabled(): Boolean = client.isEnabled()

    override fun close() {
        endSession()
        client.close(null)
    }
}

public fun sentryInit(opts: ClientOptions): ClientInitGuard {
    val resolvedOpts = applyDefaults(opts)
    val client = Client(resolvedOpts)
    Hub.current().bindClient(client)
    if (resolvedOpts.autoSessionTracking && resolvedOpts.sessionMode == SessionMode.Application) {
        startSession()
    }
    return ClientInitGuard(client)
}

public fun sentryInit(dsn: String): ClientInitGuard = sentryInit(ClientOptions(dsn = Dsn.parse(dsn)))

public fun initSentry(opts: ClientOptions): ClientInitGuard = sentryInit(opts)

public fun initSentry(dsn: String): ClientInitGuard = sentryInit(dsn)

public object Sentry {
    public fun init(opts: ClientOptions): ClientInitGuard = sentryInit(opts)
    public fun init(dsn: String): ClientInitGuard = sentryInit(dsn)
}
