// port-lint: tests sentry/src/lib.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertEquals

class LibTest {
    @Test
    fun testSentryVersion() {
        assertEquals("0.46.1", Sentry.VERSION)
    }
}
