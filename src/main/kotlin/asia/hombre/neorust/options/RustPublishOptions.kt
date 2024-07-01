package asia.hombre.neorust.options

import org.gradle.api.tasks.Input

open class RustPublishOptions {
    @get:Input
    var dryRun: Boolean = false

    @get:Input
    var noVerify: Boolean = false

    @get:Input
    var allowDirty: Boolean = false

    @get:Input
    var token: String = System.getenv("CARGO_REGISTRY_TOKEN")?: System.getenv("CARGO_REGISTRIES_NAME_TOKEN") ?: ""

    @get:Input
    var index: String = ""

    @get:Input
    var registry: String = ""
}