package asia.hombre.neorust.extension

import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.options.RustBenchOptions
import asia.hombre.neorust.options.RustBinaryOptions
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustManifestOptions
import asia.hombre.neorust.options.RustProfileOptions
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTargetOptions
import asia.hombre.neorust.options.RustTestOptions
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import java.nio.file.Path
import javax.inject.Inject

/**
 * Rust Extension
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
@Suppress("unused")
abstract class RustExtension @Inject constructor(project: Project) {
    private val objects: ObjectFactory = project.objects

    /**
     * The package to publish. See cargo-pkgid(1) for the SPEC format.
     */
    @get:Input
    @get:Optional
    abstract val packageSelect: Property<String>

    /**
     * Publish for the given architecture. The default is the host architecture. The general format of the triple is
     * `<arch><sub>-<vendor>-<sys>-<abi>`. Run `rustc --print target-list` for a list of supported targets. This flag
     * may be specified multiple times.
     *
     * This may also be specified with the build.target config value.
     *
     * Note that specifying this flag makes Cargo run in a different mode where the target artifacts are placed in a
     * separate directory. See the build cache documentation for more details.
     */
    @get:Input
    @get:Optional
    abstract val target: Property<String>

    /**
     * Directory for all generated artifacts and intermediate files. May also be specified with the `CARGO_TARGET_DIR`
     * environment variable, or the build.target-dir config value. Defaults to target in the root of the workspace.
     */
    @get:InputDirectory
    @get:Optional
    abstract val targetDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val allFeatures: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val features: Property<String>

    @get:Input
    @get:Optional
    abstract val featuresList: MapProperty<String, List<String>>

    @get:Input
    @get:Optional
    abstract val noDefaultFeatures: Property<Boolean>

    @get:InputFile
    abstract val manifestPath: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val ignoreRustVersion: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val locked: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val offline: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val frozen: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val jobs: Property<Int>

    @get:Input
    @get:Optional
    abstract val keepGoing: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val verbose: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val quiet: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val color: Property<CargoColor>

    @get:Input
    @get:Optional
    abstract val toolchain: Property<String>

    @get:Input
    @get:Optional
    abstract val config: MapProperty<String, String>

    @get:Input
    @get:Optional
    abstract val configPaths: ListProperty<Path>

    @get:Input
    @get:Optional
    abstract val unstableFlags: ListProperty<String>

    init {
        manifestPath.convention(project.layout.buildDirectory.file("Cargo.toml"))
        targetDirectory.convention(project.layout.buildDirectory.dir("target"))
    }

    @Internal
    internal val rustTargetOptions: RustTargetOptions = objects.newInstance(
        RustTargetOptions::class.java
    )
    @Internal
    internal val rustManifestOptions: RustManifestOptions = objects.newInstance(
        RustManifestOptions::class.java
    )
    @Internal
    internal val rustBenchOptions: RustBenchOptions = objects.newInstance(
        RustBenchOptions::class.java
    )
    @Internal
    internal val rustBuildOptions: RustBuildOptions = objects.newInstance(
        RustBuildOptions::class.java
    )
    @Internal
    internal val rustPublishOptions: RustPublishOptions = objects.newInstance(
        RustPublishOptions::class.java
    )
    @Internal
    internal val rustTestOptions: RustTestOptions = objects.newInstance(
        RustTestOptions::class.java
    )
    @Internal
    internal val rustBinaryOptions: RustBinaryOptions = objects.newInstance(
        RustBinaryOptions::class.java
    )
    @Internal
    internal val rustFeaturesOptions: RustFeaturesOptions = objects.newInstance(
        RustFeaturesOptions::class.java
    )
    @Internal
    internal val rustProfileOptions: RustProfileOptions = objects.newInstance(
        RustProfileOptions::class.java
    )
}