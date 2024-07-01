package asia.hombre.neorust.options

import org.gradle.api.tasks.Input

open class RustTargetOptions {
    @get:Input
    var lib: Boolean = false

    @get:Input
    var bin: MutableList<String> = mutableListOf()

    @get:Input
    var bins: Boolean = false

    @get:Input
    var example: MutableList<String> = mutableListOf()

    @get:Input
    var examples: Boolean = false

    @get:Input
    var test: MutableList<String> = mutableListOf()

    @get:Input
    var tests: Boolean = false

    @get:Input
    var bench: MutableList<String> = mutableListOf()

    @get:Input
    var benches: Boolean = false

    @get:Input
    var allTargets: Boolean = false
}