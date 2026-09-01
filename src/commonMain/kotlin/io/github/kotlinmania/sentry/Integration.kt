// port-lint: source lib.rs
package io.github.kotlinmania.sentry

public interface Integration {
    public val name: String get() = this::class.simpleName ?: "Integration"

    public fun setup(hub: Hub) {
        hub.hashCode()
    }
}

public class AttachStacktraceIntegration : Integration {
    override val name: String = "AttachStacktrace"
}

public class DebugImagesIntegration : Integration {
    override val name: String = "DebugImages"
}

public class ContextIntegration : Integration {
    override val name: String = "Context"
}

public class PanicIntegration : Integration {
    override val name: String = "Panic"
}

public class ProcessStacktraceIntegration : Integration {
    override val name: String = "ProcessStacktrace"
}
