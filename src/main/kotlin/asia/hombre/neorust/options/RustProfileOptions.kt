package asia.hombre.neorust.options

import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class RustProfileOptions {
    @get:Input
    @get:Optional
    abstract val dev: MapProperty<String, String>
    @get:Input
    @get:Optional
    abstract val release: MapProperty<String, String>
    @get:Input
    @get:Optional
    abstract val test: MapProperty<String, String>
    @get:Input
    @get:Optional
    abstract val bench: MapProperty<String, String>
}