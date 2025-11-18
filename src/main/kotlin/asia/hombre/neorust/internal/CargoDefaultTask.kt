package asia.hombre.neorust.internal

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.task.CargoClean
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.absolutePathString

/**
 * Any Cargo task needing the default Cargo configuration
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class CargoDefaultTask @Inject constructor() : DefaultTask() {
    @Internal
    lateinit var ext: RustExtension

    @get:Inject
    @get:Internal
    abstract val cmd: ExecOperations

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
     * environment abstract variable, or the build.target-dir config value. Defaults to target in the root of the workspace.
     */
    @get:OutputDirectory
    @get:Optional
    abstract val targetDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val allFeatures: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val features: Property<String>

    @get:InputFile
    abstract val manifestPath: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val ignoreRustVersion: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noDefaultFeatures: Property<Boolean>

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

    @Internal
    internal open fun getInitialArgs(): List<String> {
        return mutableListOf("cargo")
    }

    /**
     * Internally creates a list of all the available arguments to pass to cargo.
     */
    internal open fun compileArgs(): List<String> {
        val args = getInitialArgs() as MutableList<String>

        packageSelect.apply {
            if(isPresent && get().isNotBlank())
                args.addAll(listOf("--package", get()))
        }

        target.apply {
            if(isPresent && get().isNotBlank())
                args.addAll(listOf("--target", get()))
        }

        targetDirectory.apply {
            if(isPresent)
                args.addAll(listOf("--target-dir", get().asFile.absolutePath))
        }

        if(allFeatures.getOrElse(false) && this !is CargoClean)
            args.add("--all-features")

        features.apply {
            if(isPresent && !allFeatures.getOrElse(false) && this@CargoDefaultTask !is CargoClean)
                args.addAll(listOf("--features", get()))
        }

        if(noDefaultFeatures.getOrElse(false) && this !is CargoClean)
            args.add("--no-default-features")

        manifestPath.get().asFile.apply {
            args.addAll(listOf("--manifest-path", this.absolutePath))
        }

        if(ignoreRustVersion.getOrElse(false))
            args.add("--ignore-rust-version")

        if(locked.getOrElse(false))
            args.add("--locked")

        if(offline.getOrElse(false))
            args.add("--offline")

        if(frozen.getOrElse(false))
            args.add("--frozen")

        if(jobs.isPresent)
            args.addAll(listOf("--jobs", jobs.get().toString()))

        if(keepGoing.getOrElse(false))
            args.add("--keep-going")

        if(verbose.getOrElse(false))
            args.add("--verbose")

        if(quiet.getOrElse(false))
            args.add("--quiet")

        if(color.isPresent)
            args.addAll(listOf("--color", color.get().name))

        toolchain.apply {
            if(isPresent && get().isNotBlank())
                args.add("+${toolchain.get()}")
        }

        if(config.isPresent) config.get().forEach { (t, u) ->
            args.addAll(listOf("--config", "$t=$u"))
        }

        if(configPaths.isPresent) configPaths.get().forEach { path ->
            args.addAll(listOf("--config", "\"${path.absolutePathString()}\""))
        }

        if(unstableFlags.isPresent) unstableFlags.get().forEach { flag ->
            args.addAll(listOf("-Z", flag))
        }

        return args
    }

    @TaskAction
    internal open fun cargoTaskAction() {
        cmd.exec {
            commandLine = compileArgs().also { println(it.joinToString(" ")) }
        }
    }
}