package asia.hombre.neorust.options

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustCrateOptions @Inject constructor(internal val name: String, internal val version: String) {
    @get:Input
    @get:Optional
    abstract val path: Property<String>
    @get:Input
    @get:Optional
    abstract val registry: Property<String>
    @get:Input
    @get:Optional
    abstract val features: ListProperty<String>
    @get:Input
    @get:Optional
    abstract val defaultFeatures: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val optional: Property<Boolean>
}