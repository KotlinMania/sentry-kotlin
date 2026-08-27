// port-lint: tests transports/ratelimit.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RateLimiterTest {
    @Test
    fun testSentryHeader() {
        val rl = RateLimiter.new()
        rl.updateFromSentryHeader("120:error:project:reason, 60:session:foo")

        val errDisabled = rl.isDisabled(RateLimitingCategory.Error)
        assertNotNull(errDisabled)
        assertTrue(errDisabled <= 120.seconds)

        val sessionDisabled = rl.isDisabled(RateLimitingCategory.Session)
        assertNotNull(sessionDisabled)
        assertTrue(sessionDisabled <= 60.seconds)

        assertNull(rl.isDisabled(RateLimitingCategory.Transaction))
        assertNull(rl.isDisabled(RateLimitingCategory.LogItem))
        assertNull(rl.isDisabled(RateLimitingCategory.All))

        rl.updateFromSentryHeader(
            """
            30::bar,
            120:invalid:invalid,
            4711:foo;bar;baz;security:project
            """.trimIndent(),
        )

        val txDisabled = rl.isDisabled(RateLimitingCategory.Transaction)
        assertNotNull(txDisabled)
        assertTrue(txDisabled <= 30.seconds)

        val anyDisabled = rl.isDisabled(RateLimitingCategory.All)
        assertNotNull(anyDisabled)
        assertTrue(anyDisabled <= 30.seconds)
    }

    @Test
    fun testSentryHeaderNoCategories() {
        val rl = RateLimiter.new()
        rl.updateFromSentryHeader("120::bar")

        val errDisabled = rl.isDisabled(RateLimitingCategory.Error)
        assertNotNull(errDisabled)
        assertTrue(errDisabled <= 120.seconds)

        val sessionDisabled = rl.isDisabled(RateLimitingCategory.Session)
        assertNotNull(sessionDisabled)
        assertTrue(sessionDisabled <= 120.seconds)

        val txDisabled = rl.isDisabled(RateLimitingCategory.Transaction)
        assertNotNull(txDisabled)
        assertTrue(txDisabled <= 120.seconds)

        val logDisabled = rl.isDisabled(RateLimitingCategory.LogItem)
        assertNotNull(logDisabled)
        assertTrue(logDisabled <= 120.seconds)

        val attachDisabled = rl.isDisabled(RateLimitingCategory.Attachment)
        assertNotNull(attachDisabled)
        assertTrue(attachDisabled <= 120.seconds)

        val anyDisabled = rl.isDisabled(RateLimitingCategory.All)
        assertNotNull(anyDisabled)
        assertTrue(anyDisabled <= 120.seconds)
    }

    @Test
    fun testRetryAfter() {
        val rl = RateLimiter.new()
        rl.updateFromRetryAfter("60")

        val errDisabled = rl.isDisabled(RateLimitingCategory.Error)
        assertNotNull(errDisabled)
        assertTrue(errDisabled <= 60.seconds)

        val sessionDisabled = rl.isDisabled(RateLimitingCategory.Session)
        assertNotNull(sessionDisabled)
        assertTrue(sessionDisabled <= 60.seconds)

        val txDisabled = rl.isDisabled(RateLimitingCategory.Transaction)
        assertNotNull(txDisabled)
        assertTrue(txDisabled <= 60.seconds)

        val anyDisabled = rl.isDisabled(RateLimitingCategory.All)
        assertNotNull(anyDisabled)
        assertTrue(anyDisabled <= 60.seconds)
    }
}
