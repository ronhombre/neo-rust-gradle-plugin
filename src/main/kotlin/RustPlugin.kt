
import asia.hombre.neorust.exception.DuplicateBinaryTargetException
import asia.hombre.neorust.CrateLibrary
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import asia.hombre.neorust.internal.CargoTargettedTask
import asia.hombre.neorust.options.RustBenchOptions
import asia.hombre.neorust.options.RustBinaryOptions
import asia.hombre.neorust.options.RustBinaryOptions.Binary
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustManifestOptions
import asia.hombre.neorust.options.RustManifestOptions.Library
import asia.hombre.neorust.options.RustManifestOptions.Package
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTargetOptions
import asia.hombre.neorust.options.RustTestOptions
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoManifestGenerate
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest
import org.gradle.api.Action
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import java.util.*
import kotlin.io.path.Path

internal fun CargoDefaultTask.setDefaultProperties() {
    packageSelect.convention(ext.packageSelect)
    target.convention(ext.target)
    targetDirectory.convention(ext.targetDirectory)
    allFeatures.convention(ext.allFeatures)
    features.convention(ext.features)
    manifestPath.convention(ext.manifestPath)
    ignoreRustVersion.convention(ext.ignoreRustVersion)
    noDefaultFeatures.convention(ext.noDefaultFeatures)
    locked.convention(ext.locked)
    offline.convention(ext.offline)
    frozen.convention(ext.frozen)
    jobs.convention(ext.jobs)
    keepGoing.convention(ext.keepGoing)
    verbose.convention(ext.verbose)
    quiet.convention(ext.quiet)
    color.convention(ext.color)
    toolchain.convention(ext.toolchain)
    config.convention(ext.config)
    configPaths.convention(ext.configPaths)
    unstableFlags.convention(ext.unstableFlags)
}

internal fun CargoTargettedTask.setTargettedProperties() {
    lib.convention(ext.rustTargetOptions.lib)
    bin.set(ext.rustTargetOptions.bin)
    bins.convention(ext.rustTargetOptions.bins)
    example.set(ext.rustTargetOptions.example)
    examples.convention(ext.rustTargetOptions.examples)
    test.set(ext.rustTargetOptions.test)
    tests.convention(ext.rustTargetOptions.tests)
    bench.set(ext.rustTargetOptions.bench)
    benches.convention(ext.rustTargetOptions.benches)
    allTargets.convention(ext.rustTargetOptions.allTargets)
}

internal fun CargoBench.setBenchProperties() {
    noRun.convention(ext.rustBenchOptions.noRun)
    noCapture.convention(ext.rustBenchOptions.noCapture)
    noFailFast.convention(ext.rustBenchOptions.noFailFast)
}

internal fun CargoBuild.setBuildProperties() {
    workspace.convention(ext.rustBuildOptions.workspace)
    exclude.convention(ext.rustBuildOptions.exclude)
    release.convention(ext.rustBuildOptions.release)
    profile.convention(ext.rustBuildOptions.profile)
    timings.convention(ext.rustBuildOptions.timings)
    messageFormat.convention(ext.rustBuildOptions.messageFormat)
    buildPlan.convention(ext.rustBuildOptions.buildPlan)
    buildAll.convention(ext.rustBuildOptions.buildAll)
    futureIncompatReport.convention(ext.rustBuildOptions.futureIncompatReport)
}

internal fun CargoPublish.setPublishProperties() {
    dryRun.convention(ext.rustPublishOptions.dryRun)
    noVerify.convention(ext.rustPublishOptions.noVerify)
    allowDirty.convention(ext.rustPublishOptions.allowDirty)
    token.convention(ext.rustPublishOptions.token)
    index.convention(ext.rustPublishOptions.index)
    registry.convention(ext.rustPublishOptions.registry)
}

internal fun CargoTest.setTestProperties() {
    testThreads.convention((ext.rustTargetOptions as RustTestOptions).testThreads)
}

/**
 * Modify the Cargo crate manifest
 */
@Suppress("unused")
fun RustExtension.manifest(rustManifestOptions: Action<RustManifestOptions>) {
    rustManifestOptions.execute(this.rustManifestOptions)
}

/**
 * Modify the Global build options
 */
@Suppress("unused")
fun RustExtension.building(rustBuildOptions: Action<RustBuildOptions>) {
    rustBuildOptions.execute(this.rustBuildOptions)
}

/**
 * Modify the Global benchmark options
 */
@Suppress("unused")
fun RustExtension.benchmarking(rustBenchOptions: Action<RustBenchOptions>) {
    rustBenchOptions.execute(this.rustBenchOptions)
}

/**
 * Modify the Global publish options
 */
@Suppress("unused")
fun RustExtension.publishing(rustPublishOptions: Action<RustPublishOptions>) {
    rustPublishOptions.execute(this.rustPublishOptions)
}

/**
 * Modify the Global test options
 */
@Suppress("unused")
fun RustExtension.testing(rustTestOptions: Action<RustTestOptions>) {
    rustTestOptions.execute(this.rustTestOptions)
}

/**
 * Register binaries
 */
@Suppress("unused")
fun RustExtension.binaries(rustBinaryOptions: Action<RustBinaryOptions>) {
    rustBinaryOptions.execute(this.rustBinaryOptions)
}

/**
 * Set Cargo features
 */
@Suppress("unused")
fun RustExtension.features(rustFeaturesOptions: Action<RustFeaturesOptions>) {
    rustFeaturesOptions.execute(this.rustFeaturesOptions)
}

/**
 * Define Global build targets that apply to all build, bench, and test tasks
 */
@Suppress("unused")
fun RustExtension.targets(rustTargetOptions: Action<RustTargetOptions>) {
    rustTargetOptions.execute(this.rustTargetOptions)
}

/**
 * Crate package configuration (Crate name, version, author, etc)
 */
@Suppress("unused")
fun RustManifestOptions.packaging(packageConfig: Action<Package>) {
    packageConfig.execute(this.packageConfig)
}

/**
 * Configure this project as a library Crate
 */
@Suppress("unused")
fun RustManifestOptions.lib(libConfig: Action<Library>) {
    libConfig.execute(this.libConfig)
}

/**
 * Register a binary target to be built and configure it for custom builds
 */
@Suppress("unused")
fun RustBinaryOptions.register(name: String, binary: Action<Binary>? = null) {
    val bin = objectFactory.newInstance(Binary::class.java)
    bin.name.set(name)

    binary?.execute(bin)

    if(bin.name.isPresent) {
        this.list.find {
            it.name.get() == bin.name.get() && it.buildProfile.get() == bin.buildProfile.get()
        }?.let {
            throw DuplicateBinaryTargetException(
                "The binary target with name '$name' and profile '${bin.buildProfile.get()}' has already been registered."
            )
        }
        this.list.add(bin)
    }
}

/**
 * Set a feature for Cargo
 */
@Suppress("unused")
fun RustFeaturesOptions.feature(name: String, values: List<String> = emptyList()) {
    this.list.add(RustFeaturesOptions.Feature(name, values))
}

private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())

internal fun isWindows(): Boolean {
    return OS.contains("win")
}

internal fun isMac(): Boolean {
    return OS.contains("mac")
}

internal fun isUnix(): Boolean {
    return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"))
}

internal fun isSolaris(): Boolean {
    return OS.contains("sunos")
}

internal inline fun StringBuilder.writeTable(name: String, block: StringBuilder.() -> Unit) {
    this.append("\n[$name]\n")
    this.block()
}

internal fun StringBuilder.writeField(key: String, value: String?) {
    if (!value.isNullOrBlank()) {
        append("$key = \"$value\"\n".replace("\\", "\\\\"))
    }
}

internal fun StringBuilder.writeArrayField(key: String, values: List<String>, forced: Boolean = false) {
    if (values.isNotEmpty() || forced) {
        append("$key = [")
        values.forEachIndexed { index, value ->
            if (index > 0) append(", ")
            append("\"${value.trim()}\"")
        }
        append("]\n")
    }
}

internal fun StringBuilder.writeBooleanField(key: String, value: Boolean, defaultValue: Boolean = true) {
    if (value != defaultValue) {
        append("$key = $value\n")
    }
}

internal fun StringBuilder.writeCrateField(crate: RustCrateOptions) {
    if(!crate.path.isPresent &&
        !crate.registry.isPresent &&
        !crate.features.isPresent &&
        !crate.defaultFeatures.isPresent &&
        !crate.optional.isPresent)
        append("${crate.name} = \"${crate.version}\"\n")
    else {
        val crateOptions = mutableListOf<String>()

        if(crate.version.isNotBlank())
            crateOptions.add("version = \"${crate.version}\"")

        if(crate.path.isPresent)
            crateOptions.add("path = \"${crate.path.get()}\"")

        if(crate.registry.isPresent && crate.registry.get() != "crates.io")
            crateOptions.add("registry = \"${crate.registry.get()}\"")

        if(crate.features.isPresent && crate.features.get().isNotEmpty())
            crateOptions.add("features = [${crate.features.get().joinToString(", ") { "\"$it\""}}]")

        if(crate.defaultFeatures.isPresent)
            crateOptions.add("default-features = ${crate.defaultFeatures.get()}")

        if(crate.optional.isPresent)
            crateOptions.add("optional = ${crate.optional.get()}")

        append("${crate.name} = { ${crateOptions.joinToString(", ")} }\n")
    }
}

private fun Action<RustCrateOptions>?.get(objectFactory: ObjectFactory, nameVersion: String): RustCrateOptions {
    val options = objectFactory.newInstance<RustCrateOptions>(
        RustCrateOptions::class.java,
        nameVersion.substringBefore(":"),
        nameVersion.substringAfter(":")
    )

    this?.execute(options)

    return options
}

private fun CrateLibrary.configureAndGetGradleCrate(
    dependency: ProjectDependency,
    targetList: MutableList<RustCrateOptions>,
    options: Action<RustCrateOptions>?
) {
    //Shortened for readability and convenience
    val project = dependency.dependencyProject
    project.afterEvaluate {
        val parentProject = project.rootProject.findProject(this@configureAndGetGradleCrate.projectName)?: run {
            project.logger.error("Unable to find parent project when adding ${project.name} as crate.")
            return@afterEvaluate
        }
        try {
            val parentRustExtension = parentProject.extensions.getByType(RustExtension::class.java)
            val rustExtension = project.extensions.getByType(RustExtension::class.java)

            //Wire tasks
            parentProject.tasks.withType(CargoManifestGenerate::class.java).forEach {
                it.dependsOn(project.tasks.withType(CargoManifestGenerate::class.java))
            }

            //Resolve Gradle project as a Cargo crate project
            val crate = this.objects.newInstance(RustCrateOptions::class.java, dependency.name, dependency.version)

            //Apply user configuration
            options?.execute(crate)

            val manifestPath = Path(rustExtension.manifestPath.get()).parent
            val mainProjectPath = Path(parentRustExtension.manifestPath.get()).parent

            //Create a relative path since that's what Cargo wants
            val relativePath = mainProjectPath.relativize(manifestPath)

            //Warn that we have overwritten 'path'
            if(crate.path.isPresent && crate.path.get().isNotBlank()) {
                project.logger.warn("The 'path' property is automatically set and has been overwritten.")
            }

            //Fix Windows pathing. Cargo only recognizes Unix-like paths
            crate.path.set(relativePath.toString().replace("\\", "/"))

            targetList.add(crate)
        } catch (e: UnknownDomainObjectException) {
            project.logger.error("This is not a neo-rust-gradle-plugin supported project.")
            project.logger.debug(e.stackTraceToString())
        }
    }
}

//Non-local crates

/**
 * Add a non-local crate taken from the default registry unless configured
 */
@Suppress("unused")
fun DependencyHandler.crate(nameVersion: String, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        dependencies.add(options.get(this.objects, nameVersion))
    }
}

/**
 * Add a non-local dev only crate taken from the default registry unless configured
 */
@Suppress("unused")
fun DependencyHandler.devCrate(nameVersion: String, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        devDependencies.add(options.get(this.objects, nameVersion))
    }
}

/**
 * Add a non-local build only crate taken from the default registry unless configured
 */
@Suppress("unused")
fun DependencyHandler.buildCrate(nameVersion: String, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        buildDependencies.add(options.get(this.objects, nameVersion))
    }
}

//Local crates

/**
 * Add a local crate taken from a local Gradle module
 */
@Suppress("unused")
fun DependencyHandler.crate(project: ProjectDependency, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        configureAndGetGradleCrate(project, dependencies, options)
    }
}

/**
 * Add a local dev only crate taken from a local Gradle module
 */
@Suppress("unused")
fun DependencyHandler.devCrate(project: ProjectDependency, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        configureAndGetGradleCrate(project, devDependencies, options)
    }
}

/**
 * Add a local build only crate taken from a local Gradle module
 */
@Suppress("unused")
fun DependencyHandler.buildCrate(project: ProjectDependency, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        configureAndGetGradleCrate(project, buildDependencies, options)
    }
}