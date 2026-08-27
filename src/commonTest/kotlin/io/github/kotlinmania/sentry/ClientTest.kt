// port-lint: tests sentry/tests/test_client.rs
package io.github.kotlinmania.sentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ClientTest {
    @Test
    fun testIntoClient() {
        val c1 = Client.fromConfig("https://public@example.com/42%21")
        val dsn1 = c1.dsn()
        assertNotNull(dsn1)
        assertEquals("public", dsn1.publicKey)
        assertEquals("example.com", dsn1.host)
        assertEquals(Scheme.Https, dsn1.scheme)
        assertEquals("42%21", dsn1.projectId.value)

        val c2 =
            Client.fromConfig(
                Pair(
                    "https://public@example.com/42%21",
                    ClientOptions(
                        release = "foo@1.0",
                        tracesSampler = { ctx -> if (ctx.name().isEmpty()) 0.0 else 1.0 },
                    ),
                ),
            )
        val dsn2 = c2.dsn()
        assertNotNull(dsn2)
        assertEquals("public", dsn2.publicKey)
        assertEquals("example.com", dsn2.host)
        assertEquals(Scheme.Https, dsn2.scheme)
        assertEquals("42%21", dsn2.projectId.value)
        assertEquals("foo@1.0", c2.options().release)

        val c3 = Client.fromConfig(null)
        assertNull(c3.options().dsn)
    }

    @Test
    fun testUnwindSafe() {
        val transport = TestTransport.new()
        val options =
            ClientOptions(
                dsn = Dsn.parse("https://public@example.com/1"),
                transport = { transport },
            )
        val client = Client.from(options)
        Hub.current().bindClient(client)
        captureMessage("Hello World!", Level.Warning)
        Hub.current().bindClient(null)

        val events = transport.fetchAndClearEvents()
        assertEquals(1, events.size)
    }

    @Test
    fun testConcurrentInit() {
        val guard1 = sentryInit(ClientOptions())
        val guard2 = sentryInit(ClientOptions())
        guard1.close()
        guard2.close()
    }

    @Test
    fun testInvalidProxy() {
        val guard = sentryInit(ClientOptions(httpsProxy = ""))
        guard.close()
    }
}
