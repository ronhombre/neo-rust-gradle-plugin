package asia.hombre.neorust.options

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Global Cargo miscellaneous options
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class RustMiscellaneousOptions @Inject constructor() {
    @get:Input
    @get:Optional
    abstract val jobs: Property<Int>

    //TODO: Document this doesn't work with benchmarking. Use --no-fail-fast
    @get:Input
    @get:Optional
    abstract val keepGoing: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val futureIncompatReport: Property<Boolean>
}