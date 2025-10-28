package asia.hombre.neorust.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustTestOptions @Inject constructor(): RustBenchOptions() {
    @get:Input
    @get:Optional
    abstract val testThreads: Property<Int>
}