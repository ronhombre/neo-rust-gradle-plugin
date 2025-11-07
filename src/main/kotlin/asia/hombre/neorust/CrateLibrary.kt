package asia.hombre.neorust

import asia.hombre.neorust.options.RustCrateOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import javax.inject.Inject

/**
 * Holds crates used in this project
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
internal abstract class CrateLibrary @Inject constructor() {
    @get:Inject
    internal abstract val objects: ObjectFactory

    @get:Nested
    internal abstract val dependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val devDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val buildDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedDevDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedBuildDependencies: ListProperty<RustCrateOptions>
}