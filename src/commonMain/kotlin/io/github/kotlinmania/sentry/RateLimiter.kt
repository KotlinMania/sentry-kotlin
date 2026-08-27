// port-lint: source transports/ratelimit.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.sentry

import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

public enum class RateLimitingCategory {
    All,
    Error,
    Session,
    Transaction,
    Attachment,
    LogItem,
}

public class RateLimiter {
    private var global: Instant? = null
    private var error: Instant? = null
    private var session: Instant? = null
    private var transaction: Instant? = null
    private var attachment: Instant? = null
    private var logItem: Instant? = null

    public fun updateFromRetryAfter(header: String) {
        val seconds = header.toDoubleOrNull()
        val newTime =
            if (seconds != null) {
                Clock.System.now() + ceil(seconds).toLong().seconds
            } else {
                Clock.System.now() + 60.seconds
            }
        global = newTime
    }

    public fun updateFromSentryHeader(header: String) {
        for (group in header.split(',')) {
            parseGroup(group.trim())
        }
    }

    private fun parseGroup(group: String) {
        val splits = group.split(':')
        if (splits.size < 3) return
        val seconds = splits[0].toDoubleOrNull() ?: return
        val categories = splits[1]
        val newTime = Clock.System.now() + ceil(seconds).toLong().seconds

        if (categories.isEmpty()) {
            global = newTime
        }

        for (category in categories.split(';')) {
            when (category) {
                "error" -> error = newTime
                "session" -> session = newTime
                "transaction" -> transaction = newTime
                "attachment" -> attachment = newTime
                "log_item" -> logItem = newTime
            }
        }
    }

    public fun updateFrom429() {
        global = Clock.System.now() + 60.seconds
    }

    public fun isDisabled(category: RateLimitingCategory): Duration? {
        val now = Clock.System.now()
        val g = global
        if (g != null && g > now) {
            return g - now
        }
        val target =
            when (category) {
                RateLimitingCategory.All -> global
                RateLimitingCategory.Error -> error
                RateLimitingCategory.Session -> session
                RateLimitingCategory.Transaction -> transaction
                RateLimitingCategory.Attachment -> attachment
                RateLimitingCategory.LogItem -> logItem
            } ?: return null
        return if (target > now) target - now else null
    }

    public fun isEnabled(category: RateLimitingCategory): Boolean = isDisabled(category) == null

    public fun filterEnvelope(envelope: Envelope): Envelope? {
        val filteredItems =
            envelope.items.filter { item ->
                val cat =
                    when (item) {
                        is EnvelopeItem.Event -> RateLimitingCategory.Error
                        is EnvelopeItem.Attachment -> RateLimitingCategory.Attachment
                        is EnvelopeItem.ItemContainer ->
                            when (item.container) {
                                is ItemContainer.Logs -> RateLimitingCategory.LogItem
                            }
                    }
                isEnabled(cat)
            }
        return if (filteredItems.isNotEmpty()) {
            Envelope(headers = envelope.headers, items = filteredItems)
        } else {
            null
        }
    }

    public companion object {
        public fun new(): RateLimiter = RateLimiter()
    }
}
