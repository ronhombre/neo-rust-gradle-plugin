package asia.hombre.neorust.extension

import asia.hombre.neorust.RustCrate
import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.options.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

@Suppress("unused")
open class RustExtension @Inject constructor(val project: Project) {
    @Internal
    val DEFAULT_TARGET_DIR: Path = project.layout.buildDirectory.get().asFile.resolve("target").toPath()
    /**
     * The package to publish. See cargo-pkgid(1) for the SPEC format.
     */
    @get:Input
    var packageSelect: String = ""

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
    var target: String = ""

    /**
     * Directory for all generated artifacts and intermediate files. May also be specified with the `CARGO_TARGET_DIR`
     * environment variable, or the build.target-dir config value. Defaults to target in the root of the workspace.
     */
    @get:Input
    var targetDirectory: String = (System.getenv("CARGO_TARGET_DIR")?.let { Paths.get(it) } ?: DEFAULT_TARGET_DIR).toAbsolutePath().toString()

    @get:Input
    var allFeatures: Boolean = false

    @get:Input
    var features: String = ""

    @get:Input
    var noDefaultFeatures: Boolean = false

    @get:Input
    var manifestPath: String = project.layout.buildDirectory.file("Cargo.toml").get().asFile.absolutePath

    @get:Input
    var ignoreRustVersion: Boolean = false

    @get:Input
    var locked: Boolean = false

    @get:Input
    var offline: Boolean = false

    @get:Input
    var frozen: Boolean = false

    @get:Input
    var jobs: Int = 0

    @get:Input
    var keepGoing: Boolean = false

    @get:Input
    var verbose: Boolean = false

    @get:Input
    var quiet: Boolean = false

    @get:Input
    var color: CargoColor = CargoColor.none

    @get:Input
    var toolchain: String = ""

    @get:Input
    var config: MutableMap<String, String> = mutableMapOf()

    @get:Input
    var configPaths: MutableList<Path> = mutableListOf()

    @get:Input
    var unstableFlags: MutableList<String> = mutableListOf()

    @Internal
    var implementationName = ""

    @Internal
    internal var dependencies: MutableList<RustCrate> = mutableListOf()

    @Internal
    var buildOnlyName = ""

    @Internal
    internal var buildDependencies: MutableList<RustCrate> = mutableListOf()

    @Internal
    var devOnlyName = ""

    @Internal
    internal var devDependencies: MutableList<RustCrate> = mutableListOf()

    internal val rustManifestOptions: RustManifestOptions = RustManifestOptions(project)
    internal val rustBenchOptions: RustBenchOptions = RustBenchOptions()
    internal val rustBuildOptions: RustBuildOptions = RustBuildOptions(this)
    internal val rustPublishOptions: RustPublishOptions = RustPublishOptions()
    internal val rustTestOptions: RustTestOptions = RustTestOptions()

    fun manifest(rustManifestOptions: Action<RustManifestOptions>) {
        rustManifestOptions.execute(this.rustManifestOptions)
    }

    fun building(rustBuildOptions: Action<RustBuildOptions>) {
        rustBuildOptions.execute(this.rustBuildOptions)
    }

    fun benchmarking(rustBenchOptions: Action<RustBenchOptions>) {
        rustBenchOptions.execute(this.rustBenchOptions)
    }

    fun publishing(rustPublishOptions: Action<RustPublishOptions>) {
        rustPublishOptions.execute(this.rustPublishOptions)
    }

    fun testing(rustTestOptions: Action<RustTestOptions>) {
        rustTestOptions.execute(this.rustTestOptions)
    }
}