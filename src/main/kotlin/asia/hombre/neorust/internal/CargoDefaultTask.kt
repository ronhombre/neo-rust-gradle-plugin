package asia.hombre.neorust.internal

import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.extension.RustExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.absolutePathString

open class CargoDefaultTask: DefaultTask() {
    private val pluginExtension = project.extensions.getByType(RustExtension::class.java)
    /**
     * The package to publish. See cargo-pkgid(1) for the SPEC format.
     */
    @get:Input
    var packageSelect: String = ""
        get() = field.ifBlank { pluginExtension.packageSelect }

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
        get() = field.ifBlank { pluginExtension.target }

    /**
     * Directory for all generated artifacts and intermediate files. May also be specified with the `CARGO_TARGET_DIR`
     * environment variable, or the build.target-dir config value. Defaults to target in the root of the workspace.
     */
    @get:Input
    var targetDirectory: String = ""
        get() = field.ifBlank { pluginExtension.targetDirectory }

    @get:Input
    var allFeatures: Boolean? = null
        get() = field?: pluginExtension.allFeatures

    @get:Input
    var features: String = ""
        get() = field.ifBlank { pluginExtension.features }

    @get:Input
    var manifestPath: String = ""
        get() = field.ifBlank { pluginExtension.manifestPath }

    @get:Input
    var ignoreRustVersion: Boolean? = null
        get() = field?: pluginExtension.ignoreRustVersion

    @get:Input
    var noDefaultFeatures: Boolean? = null
        get() = field?: pluginExtension.noDefaultFeatures

    @get:Input
    var locked: Boolean? = null
        get() = field?: pluginExtension.locked

    @get:Input
    var offline: Boolean? = null
        get() = field?: pluginExtension.offline

    @get:Input
    var frozen: Boolean? = null
        get() = field?: pluginExtension.frozen

    @get:Input
    var jobs: Int? = null
        get() = field?: pluginExtension.jobs

    @get:Input
    var keepGoing: Boolean? = null
        get() = field?: pluginExtension.keepGoing

    @get:Input
    var verbose: Boolean? = null
        get() = field?: pluginExtension.verbose

    @get:Input
    var quiet: Boolean? = null
        get() = field?: pluginExtension.quiet

    @get:Input
    var color: CargoColor = CargoColor.none
        get() = field.takeIf { it != CargoColor.none }?: pluginExtension.color

    @get:Input
    var toolchain: String = ""
        get() = field.ifBlank { pluginExtension.toolchain }

    @get:Input
    var config: MutableMap<String, String> = mutableMapOf()
        get() = field.ifEmpty { pluginExtension.config }

    @get:Input
    var configPaths: MutableList<Path> = mutableListOf()
        get() = field.ifEmpty { pluginExtension.configPaths }

    @get:Input
    var unstableFlags: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { pluginExtension.unstableFlags }

    @Internal
    internal open fun getInitialArgs(): List<String> {
        return mutableListOf("cargo")
    }

    /**
     * Internally creates a list of all the available arguments to pass to cargo.
     */
    @Internal
    internal open fun compileArgs(): List<String> {
        val args = getInitialArgs() as MutableList<String>

        if(packageSelect.isNotBlank())
            args.addAll(listOf("--package", packageSelect))

        if(target.isNotBlank())
            args.addAll(listOf("--target", target))

        args.addAll(listOf("--target-dir", "\"$targetDirectory\""))

        if(allFeatures!!)
            args.add("--all-features")

        if(features.isNotBlank() && !allFeatures!!)
            args.addAll(listOf("--features", features))

        if(noDefaultFeatures!!)
            args.add("--no-default-features")

        args.addAll(listOf("--manifest-path", "\"$manifestPath\""))

        if(ignoreRustVersion!!)
            args.add("--ignore-rust-version")

        if(locked!!)
            args.add("--locked")

        if(offline!!)
            args.add("--offline")

        if(frozen!!)
            args.add("--frozen")

        if(jobs != 0)
            args.addAll(listOf("--jobs", jobs.toString()))

        if(keepGoing!!)
            args.add("--keep-going")

        if(verbose!!)
            args.add("--verbose")

        if(quiet!!)
            args.add("--quiet")

        if(color != CargoColor.none)
            args.addAll(listOf("--color", color.name))

        if(toolchain.isNotBlank())
            args.add("+$toolchain")

        config.forEach { (t, u) ->
            args.addAll(listOf("--config", "$t=$u"))
        }

        configPaths.forEach { path ->
            args.addAll(listOf("--config", "\"${path.absolutePathString()}\""))
        }

        unstableFlags.forEach { flag ->
            args.addAll(listOf("-Z", flag))
        }

        return args
    }

    @TaskAction
    internal open fun cargoTaskAction() {
        project.exec {
            apply {
                commandLine = compileArgs()
            }
        }
    }
}