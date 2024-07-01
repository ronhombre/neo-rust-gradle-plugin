package asia.hombre.neorust.options

import org.gradle.api.tasks.Input

class RustTestOptions: RustBenchOptions() {
    @get:Input
    var testThreads: Int = 0
}