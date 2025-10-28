package asia.hombre.neorust.options

import asia.hombre.neorust.build.BuildTarget
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustBuildTargetOptions @Inject constructor() {
    @get:Input
    @get:Optional
    abstract val builds: ListProperty<BuildTarget>
}