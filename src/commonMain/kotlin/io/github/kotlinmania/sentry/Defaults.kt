// port-lint: source sentry/src/defaults.rs
package io.github.kotlinmania.sentry

public object Environment {
    private val vars: MutableMap<String, String> = mutableMapOf()

    public fun get(name: String): String? = vars[name]

    public fun set(name: String, value: String) {
        vars[name] = value
    }

    public fun remove(name: String) {
        vars.remove(name)
    }

    public fun clear() {
        vars.clear()
    }
}

public fun applyDefaults(opts: ClientOptions): ClientOptions {
    val result = opts.copy()
    if (result.transport == null) {
        result.transport = DefaultTransportFactory()
    }
    if (result.defaultIntegrations) {
        val defaults = mutableListOf<Integration>(
            AttachStacktraceIntegration(),
            DebugImagesIntegration(),
            ContextIntegration(),
            PanicIntegration(),
            ProcessStacktraceIntegration(),
        )
        defaults.addAll(result.integrations)
        result.integrations = defaults
    }
    if (result.dsn == null) {
        val envDsn = Environment.get("SENTRY_DSN")
        if (envDsn != null) {
            try {
                result.dsn = Dsn.parse(envDsn)
            } catch (_: Exception) {}
        }
    }
    if (result.release == null) {
        result.release = Environment.get("SENTRY_RELEASE")
    }
    if (result.environment == null) {
        result.environment = Environment.get("SENTRY_ENVIRONMENT") ?: "development"
    }
    if (result.httpProxy == null) {
        result.httpProxy = Environment.get("HTTP_PROXY") ?: Environment.get("http_proxy")
    }
    if (result.httpsProxy == null) {
        result.httpsProxy = Environment.get("HTTPS_PROXY") ?: Environment.get("https_proxy") ?: result.httpProxy
    }
    val sslVerify = Environment.get("SSL_VERIFY")
    if (sslVerify != null) {
        result.acceptInvalidCerts = sslVerify.lowercase() == "false" || sslVerify == "0"
    }
    return result
}
