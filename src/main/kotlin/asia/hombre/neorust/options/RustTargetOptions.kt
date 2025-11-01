package asia.hombre.neorust.options

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Global Cargo build target options. This is applied everywhere by default.
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustTargetOptions @Inject constructor() {
    @get:Input
    @get:Optional
    abstract val lib: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val bin: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val bins: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val example: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val examples: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val test: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val tests: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val bench: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val benches: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val allTargets: Property<Boolean>
}