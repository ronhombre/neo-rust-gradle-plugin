package asia.hombre.neorust.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Global Cargo test options
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustTestOptions @Inject constructor(): RustBenchOptions() {
    @get:Input
    @get:Optional
    abstract val testThreads: Property<Int>
}