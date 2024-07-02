package asia.hombre.neorust.options

import org.gradle.api.tasks.Input

class RustCrateOptions {
    @get:Input
    var name: String = ""

    @get:Input
    var version: String = ""

    @get:Input
    var path: String = ""

    @get:Input
    var registry: String = ""

    @get:Input
    var features = mutableListOf<String>()

    @get:Input
    var defaultFeatures: Boolean = true

    @get:Input
    var optional: Boolean = false
}