// port-lint: tests sentry/src/defaults.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultsTest {
    @Test
    fun testDefaultEnvironment() {
        try {
            Environment.clear()
            val opts1 = ClientOptions(environment = "explicit-env")
            val applied1 = applyDefaults(opts1)
            assertEquals("explicit-env", applied1.environment)

            val opts2 = ClientOptions()
            val applied2 = applyDefaults(opts2)
            assertEquals("development", applied2.environment)

            Environment.set("SENTRY_ENVIRONMENT", "env-from-env")
            val opts3 = ClientOptions()
            val applied3 = applyDefaults(opts3)
            assertEquals("env-from-env", applied3.environment)
        } finally {
            Environment.clear()
        }
    }
}
