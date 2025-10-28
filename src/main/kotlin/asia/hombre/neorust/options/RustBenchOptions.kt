package asia.hombre.neorust.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustBenchOptions @Inject constructor(): RustTargetOptions() {
    @get:Input
    @get:Optional
    abstract val noRun: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noCapture: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noFailFast: Property<Boolean>
}