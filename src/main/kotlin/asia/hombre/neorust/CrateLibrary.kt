package asia.hombre.neorust

import asia.hombre.neorust.options.RustCrateOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal

/**
 * Holds crates used in this project
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
class CrateLibrary(@Internal internal val projectName: String, @Internal internal val objects: ObjectFactory) {
    @Internal
    internal val dependencies: MutableList<RustCrateOptions> = mutableListOf()
    @Internal
    internal val buildDependencies: MutableList<RustCrateOptions> = mutableListOf()
    @Internal
    internal val devDependencies: MutableList<RustCrateOptions> = mutableListOf()
}