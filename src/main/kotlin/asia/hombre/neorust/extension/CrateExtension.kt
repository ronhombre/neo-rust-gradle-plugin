package asia.hombre.neorust.extension

import asia.hombre.neorust.options.RustCrateOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal

class CrateExtension(@Internal internal val projectName: String, @Internal internal val objects: ObjectFactory) {
    @Internal
    internal val dependencies: MutableList<RustCrateOptions> = mutableListOf()
    @Internal
    internal val buildDependencies: MutableList<RustCrateOptions> = mutableListOf()
    @Internal
    internal val devDependencies: MutableList<RustCrateOptions> = mutableListOf()
}