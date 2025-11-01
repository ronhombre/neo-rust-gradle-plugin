package asia.hombre.neorust.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Cargo publishing options
 *
 * Reads CARGO_REGISTRY_TOKEN and CARGO_REGISTRIES_NAME_TOKEN in that order for a token.
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustPublishOptions {
    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noVerify: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val allowDirty: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val token: Property<String>

    @get:Input
    @get:Optional
    abstract val index: Property<String>

    @get:Input
    @get:Optional
    abstract val registry: Property<String>

    init {
        token.convention(System.getenv("CARGO_REGISTRY_TOKEN")?: System.getenv("CARGO_REGISTRIES_NAME_TOKEN"))
    }
}