package asia.hombre.neorust.options

import org.gradle.api.tasks.Input

open class RustBenchOptions: RustTargetOptions() {

    @get:Input
    var noRun: Boolean = false

    @get:Input
    var noCapture: Boolean = false

    @get:Input
    var noFailFast: Boolean = false
}