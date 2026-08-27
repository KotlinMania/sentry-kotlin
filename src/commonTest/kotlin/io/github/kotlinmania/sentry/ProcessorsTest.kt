// port-lint: tests sentry/tests/test_processors.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessorsTest {
    @Test
    fun testEventProcessors() {
        val events =
            TestHelpers.withCapturedEvents {
                configureScope { scope ->
                    scope.setTag("worker", "worker1")
                    scope.addEventProcessor { event ->
                        event.user = User(email = "foo@example.com")
                        event
                    }
                }
                captureMessage("Hello World!", Level.Warning)
            }

        assertEquals(1, events.size)
        val event = events.first()
        assertEquals(User(email = "foo@example.com"), event.user)
    }

    @Test
    fun testBeforeCallbacks() {
        val options =
            ClientOptions(
                beforeSend = { evt ->
                    evt.logger = "muh_logger"
                    evt
                },
                beforeBreadcrumb = { crumb ->
                    crumb.copy(message = "${crumb.message} aha!")
                },
            )

        val events =
            TestHelpers.withCapturedEventsOptions({
                addBreadcrumb(
                    Breadcrumb(message = "Testing"),
                )
                captureMessage("Hello World!", Level.Warning)
            }, options)

        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("muh_logger", event.logger)
        assertEquals("Testing aha!", event.breadcrumbs[0].message)
    }

    @Test
    fun testBeforeEventCallbackDrop() {
        val options =
            ClientOptions(
                beforeSend = { _ -> null },
            )

        val events =
            TestHelpers.withCapturedEventsOptions({
                addBreadcrumb(
                    Breadcrumb(message = "Testing"),
                )
                captureMessage("Hello World!", Level.Warning)
            }, options)

        assertEquals(0, events.size)
    }

    @Test
    fun testBeforeBreadcrumbCallbackDrop() {
        val options =
            ClientOptions(
                beforeBreadcrumb = { _ -> null },
            )

        val events =
            TestHelpers.withCapturedEventsOptions({
                addBreadcrumb(
                    Breadcrumb(message = "Testing"),
                )
                captureMessage("Hello World!", Level.Warning)
            }, options)

        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("Hello World!", event.message)
        assertEquals(0, event.breadcrumbs.size)
    }
}
