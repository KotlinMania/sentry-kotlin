// port-lint: tests test_basic.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BasicTest {
    @Test
    fun testBasicCaptureMessage() {
        var lastEventId: Uuid? = null
        val events =
            TestHelpers.withCapturedEvents {
                configureScope { scope ->
                    scope.setTag("worker", "worker1")
                }
                captureMessage("Hello World!", Level.Warning)
                lastEventId = lastEventId()
            }
        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("Hello World!", event.message)
        assertEquals(Level.Warning, event.level)
        assertEquals("worker1", event.tags["worker"])
        assertEquals(lastEventId, event.eventId)
    }

    @Test
    fun testEventTraceContextFromPropagationContext() {
        var lastEventId: Uuid? = null
        var span: Span? = null
        val events =
            TestHelpers.withCapturedEvents {
                configureScope { scope ->
                    span = scope.span
                }
                captureMessage("Hello World!", Level.Warning)
                lastEventId = lastEventId()
            }
        assertEquals(1, events.size)
        val event = events.first()
        val traceContext = event.contexts["trace"]
        assertNull(span)
        assertNotNull(traceContext)
        assertTrue(traceContext is Context.Trace)
    }

    @Test
    fun testBreadcrumbs() {
        val events =
            TestHelpers.withCapturedEvents {
                addBreadcrumb {
                    Breadcrumb(
                        ty = "log",
                        message = "Old breadcrumb to be removed",
                    )
                }
                configureScope { scope -> scope.clearBreadcrumbs() }
                addBreadcrumb {
                    Breadcrumb(
                        ty = "log",
                        message = "First breadcrumb",
                    )
                }
                addBreadcrumb(
                    Breadcrumb(
                        ty = "log",
                        message = "Second breadcrumb",
                    ),
                )
                addBreadcrumb {
                    listOf(
                        Breadcrumb(
                            ty = "log",
                            message = "Third breadcrumb",
                        ),
                        Breadcrumb(
                            ty = "log",
                            message = "Fourth breadcrumb",
                        ),
                    )
                }
                addBreadcrumb { null }
                captureMessage("Hello World!", Level.Warning)
            }
        assertEquals(1, events.size)
        val event = events.first()

        val messages = event.breadcrumbs.map { Pair(it.message, it.ty) }
        assertEquals(
            listOf(
                Pair("First breadcrumb", "log"),
                Pair("Second breadcrumb", "log"),
                Pair("Third breadcrumb", "log"),
                Pair("Fourth breadcrumb", "log"),
            ),
            messages,
        )
    }

    @Test
    fun testFactory() {
        var eventCount = 0
        val testTransport =
            object : Transport {
                override fun sendEnvelope(envelope: Envelope) {
                    val event = envelope.event()
                    assertNotNull(event)
                    assertEquals("test", event.message)
                    eventCount++
                }
            }

        val options =
            ClientOptions(
                dsn = Dsn.parse("http://foo@example.com/42"),
                transport = { opts ->
                    assertEquals("example.com", opts.dsn?.host)
                    testTransport
                },
            )

        val hub = Hub(client = Client.from(options))
        Hub.run(hub) {
            captureMessage("test", Level.Error)
        }

        assertEquals(1, eventCount)
    }

    @Test
    fun testReentrantConfigureScope() {
        val events =
            TestHelpers.withCapturedEvents {
                configureScope { scope1 ->
                    scope1.setTag("which_scope", "scope1")

                    configureScope { scope2 ->
                        scope2.setTag("which_scope", "scope2")
                    }
                }

                captureMessage("look ma, no deadlock!", Level.Info)
            }

        assertEquals(1, events.size)
        assertEquals("scope2", events[0].tags["which_scope"])
    }

    @Test
    fun testAttachmentSentFromScope() {
        val envelopes =
            TestHelpers.withCapturedEnvelopes {
                withScope({ scope ->
                    scope.addAttachment(
                        Attachment(
                            buffer = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
                            filename = "test-file.bin",
                        ),
                    )
                }) {
                    captureMessage("test", Level.Error)
                }
            }

        assertEquals(1, envelopes.size)
        val items = envelopes[0].items.toList()
        assertEquals(2, items.size)
        val attachmentItem = items[1] as EnvelopeItem.Attachment
        assertEquals("test-file.bin", attachmentItem.attachment.filename)
        assertTrue(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9).contentEquals(attachmentItem.attachment.buffer))
    }

    @Test
    fun testPanicScopePop() {
        val options = ClientOptions()
        val events =
            TestHelpers.withCapturedEventsOptions(
                {
                    val hub = Hub.current()
                    val scope1 = hub.pushScope()
                    val scope2 = hub.pushScope()

                    assertFailsWith<IllegalStateException> {
                        scope1.close()
                    }

                    assertFailsWith<IllegalStateException> {
                        scope2.close()
                    }
                },
                options,
            )

        assertEquals(2, events.size)
        assertEquals("panic", events[0].exception[0].ty)
        assertEquals("Popped scope guard out of order", events[0].exception[0].value)
        assertEquals("panic", events[1].exception[0].ty)
        assertEquals("Popped scope guard out of order", events[1].exception[0].value)
    }

    @Test
    fun testBasicCaptureLog() {
        val options = ClientOptions(enableLogs = true)
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions({
                val attributes = mutableMapOf<String, LogAttribute>()
                attributes["test"] = LogAttribute.from("a string")
                attributes["sentry.sdk.name"] = LogAttribute.from("sentry.kotlin")
                attributes["sentry.sdk.version"] = LogAttribute.from("0.1.0")
                val log =
                    Log(
                        level = LogLevel.Warn,
                        body = "this is a test",
                        traceId = TraceId.random(),
                        attributes = attributes,
                    )

                Hub.current().captureLog(log)
            }, options)

        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val item = envelope.items.first() as EnvelopeItem.ItemContainer
        val logsContainer = item.container as ItemContainer.Logs
        val log = logsContainer.logs.first()
        assertEquals(LogLevel.Warn, log.level)
        assertEquals("this is a test", log.body)
        assertNotNull(log.traceId)
        assertNull(log.severityNumber)
        assertTrue(log.attributes.containsKey("sentry.sdk.name"))
        assertTrue(log.attributes.containsKey("sentry.sdk.version"))
        assertTrue(log.attributes.containsKey("test"))
    }

    @Test
    fun testBasicCaptureLogMacroMessage() {
        val options = ClientOptions(enableLogs = true)
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions({
                loggerInfo("Hello, world!")
            }, options)

        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val item = envelope.items.first() as EnvelopeItem.ItemContainer
        val logsContainer = item.container as ItemContainer.Logs
        val log = logsContainer.logs.first()
        assertEquals(LogLevel.Info, log.level)
        assertEquals("Hello, world!", log.body)
        assertNotNull(log.traceId)
        assertNull(log.severityNumber)
        assertTrue(log.attributes.containsKey("sentry.sdk.name"))
        assertTrue(log.attributes.containsKey("sentry.sdk.version"))
    }

    @Test
    fun testBasicCaptureLogMacroMessageFormatted() {
        val options = ClientOptions(enableLogs = true)
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions({
                val failedRequests = listOf("request1", "request2", "request3")
                val template = "Critical system errors detected for user %s, total failures: %d"
                val body = "Critical system errors detected for user test_user, total failures: ${failedRequests.size}"
                val attributes =
                    mapOf(
                        "sentry.message.template" to LogAttribute.from(template),
                        "sentry.message.parameter.0" to LogAttribute.from("test_user"),
                        "sentry.message.parameter.1" to LogAttribute.from(3),
                    )
                loggerWarn(body, attributes)
            }, options)

        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val item = envelope.items.first() as EnvelopeItem.ItemContainer
        val logsContainer = item.container as ItemContainer.Logs
        val log = logsContainer.logs.first()
        assertEquals(LogLevel.Warn, log.level)
        assertEquals("Critical system errors detected for user test_user, total failures: 3", log.body)
        assertEquals(
            LogAttribute.from("Critical system errors detected for user %s, total failures: %d"),
            log.attributes["sentry.message.template"],
        )
        assertEquals(LogAttribute.from("test_user"), log.attributes["sentry.message.parameter.0"])
        assertEquals(LogAttribute.from(3), log.attributes["sentry.message.parameter.1"])
        assertNotNull(log.traceId)
        assertNull(log.severityNumber)
        assertTrue(log.attributes.containsKey("sentry.sdk.name"))
        assertTrue(log.attributes.containsKey("sentry.sdk.version"))
    }

    @Test
    fun testBasicCaptureLogMacroMessageWithAttributes() {
        val options = ClientOptions(enableLogs = true)
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions({
                val attributes =
                    mapOf(
                        "user.id" to LogAttribute.from("12345"),
                        "user.active" to LogAttribute.from(true),
                        "request.duration" to LogAttribute.from(150L),
                        "success" to LogAttribute.from(false),
                    )
                loggerError("Failed to process request", attributes)
            }, options)

        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val item = envelope.items.first() as EnvelopeItem.ItemContainer
        val logsContainer = item.container as ItemContainer.Logs
        val log = logsContainer.logs.first()
        assertEquals(LogLevel.Error, log.level)
        assertEquals("Failed to process request", log.body)
        assertNull(log.attributes["sentry.message.template"])
        assertNotNull(log.traceId)
        assertNull(log.severityNumber)
        assertTrue(log.attributes.containsKey("sentry.sdk.name"))
        assertTrue(log.attributes.containsKey("sentry.sdk.version"))
        assertEquals(LogAttribute.from("12345"), log.attributes["user.id"])
        assertEquals(LogAttribute.from(true), log.attributes["user.active"])
        assertEquals(LogAttribute.from(150L), log.attributes["request.duration"])
        assertEquals(LogAttribute.from(false), log.attributes["success"])
    }

    @Test
    fun testBasicCaptureLogMacroMessageFormattedWithAttributes() {
        val options = ClientOptions(enableLogs = true)
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions({
                val body = "Database query users_by_region completed in 42 ms with 15 results"
                val attributes =
                    mapOf(
                        "hello" to LogAttribute.from("test"),
                        "operation.name" to LogAttribute.from("database_query"),
                        "operation.success" to LogAttribute.from(true),
                        "operation.time_ms" to LogAttribute.from(42L),
                        "world" to LogAttribute.from(10L),
                        "sentry.message.template" to LogAttribute.from("Database query %s completed in %d ms with %d results"),
                        "sentry.message.parameter.0" to LogAttribute.from("users_by_region"),
                        "sentry.message.parameter.1" to LogAttribute.from(42L),
                        "sentry.message.parameter.2" to LogAttribute.from(15L),
                    )
                loggerDebug(body, attributes)
            }, options)

        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val item = envelope.items.first() as EnvelopeItem.ItemContainer
        val logsContainer = item.container as ItemContainer.Logs
        val log = logsContainer.logs.first()
        assertEquals(LogLevel.Debug, log.level)
        assertEquals("Database query users_by_region completed in 42 ms with 15 results", log.body)
        assertNotNull(log.traceId)
        assertNull(log.severityNumber)
        assertTrue(log.attributes.containsKey("sentry.sdk.name"))
        assertTrue(log.attributes.containsKey("sentry.sdk.version"))
        assertEquals(LogAttribute.from("test"), log.attributes["hello"])
        assertEquals(LogAttribute.from("database_query"), log.attributes["operation.name"])
        assertEquals(LogAttribute.from(true), log.attributes["operation.success"])
        assertEquals(LogAttribute.from(42L), log.attributes["operation.time_ms"])
        assertEquals(LogAttribute.from(10L), log.attributes["world"])
        assertEquals(LogAttribute.from("users_by_region"), log.attributes["sentry.message.parameter.0"])
        assertEquals(LogAttribute.from(42L), log.attributes["sentry.message.parameter.1"])
        assertEquals(LogAttribute.from(15L), log.attributes["sentry.message.parameter.2"])
    }

    @Test
    fun testTransactionEnvelopeDscHeaders() {
        var traceId: TraceId? = null
        val dsn = Dsn.parse("http://foo@example.com/42")
        val envelopes =
            TestHelpers.withCapturedEnvelopesOptions(
                {
                    val transactionCtx = TransactionContext.new("name transaction", "op")
                    traceId = transactionCtx.traceId
                    val transaction = startTransaction(transactionCtx)
                    configureScope { scope ->
                        scope.span =
                            Span(
                                traceId = transaction.traceId,
                                spanId = transaction.spanId,
                                op = transaction.op,
                            )
                    }
                    transaction.finish()
                },
                ClientOptions(
                    dsn = dsn,
                    tracesSampleRate = 1.0,
                ),
            )

        assertNotNull(traceId)
        assertEquals(1, envelopes.size)
        val envelope = envelopes.first()
        val uuid = checkNotNull(envelope.uuid())

        val expected =
            EnvelopeHeaders.new().withEventId(uuid).withTrace(
                DynamicSamplingContext
                    .new()
                    .withTraceId(traceId)
                    .withPublicKey(dsn.publicKey)
                    .withSampleRate(1.0)
                    .withSampled(true)
                    .withTransaction("name transaction"),
            )
        assertEquals(expected.eventId, envelope.headers.eventId)
        assertEquals(expected.trace?.traceId, envelope.headers.trace?.traceId)
        assertEquals(expected.trace?.publicKey, envelope.headers.trace?.publicKey)
        assertEquals(expected.trace?.sampleRate, envelope.headers.trace?.sampleRate)
        assertEquals(expected.trace?.sampled, envelope.headers.trace?.sampled)
    }
}
