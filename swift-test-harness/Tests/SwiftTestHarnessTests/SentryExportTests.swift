import Testing
import Sentry

@Suite("Sentry Swift Export Smoke Tests")
struct SentryExportTests {
    @Test("Sentry swift module imports and exports core types")
    func swiftModuleLoads() {
        let projectId = ProjectId(value: "42")
        #expect(projectId.value == "42")

        let dsn = Dsn.Companion.shared.parse(dsnString: "https://public@example.com/42")
        #expect(dsn.publicKey == "public")
        #expect(dsn.host == "example.com")
        #expect(dsn.projectId.value == "42")
    }
}
