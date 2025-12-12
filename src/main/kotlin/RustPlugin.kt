/*
 * Copyright 2025 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *        and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import asia.hombre.neorust.CrateLibrary
import asia.hombre.neorust.exception.DuplicateBinaryTargetException
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import asia.hombre.neorust.internal.CargoTargettedTask
import asia.hombre.neorust.options.RustBenchOptions
import asia.hombre.neorust.options.RustBinariesOptions
import asia.hombre.neorust.options.RustBinaryOptions
import asia.hombre.neorust.options.RustBinaryOptions.Binary
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustBuildTargetOptions
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.RustExamplesOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustLibraryOptions
import asia.hombre.neorust.options.RustManifestOptions
import asia.hombre.neorust.options.RustManifestOptions.Package
import asia.hombre.neorust.options.RustProfileOptions
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTestOptions
import asia.hombre.neorust.options.RustTestsOptions
import asia.hombre.neorust.serializable.RustCrateObject
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest
import org.gradle.api.Action
import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.*

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
    lib.convention(ext.rustBuildTargetOptions.lib)
    bin.set(ext.rustBuildTargetOptions.bin)
    bins.convention(ext.rustBuildTargetOptions.bins)
    example.set(ext.rustBuildTargetOptions.example)
    examples.convention(ext.rustBuildTargetOptions.examples)
    test.set(ext.rustBuildTargetOptions.test)
    tests.convention(ext.rustBuildTargetOptions.tests)
    bench.set(ext.rustBuildTargetOptions.bench)
    benches.convention(ext.rustBuildTargetOptions.benches)
    allTargets.convention(ext.rustBuildTargetOptions.allTargets)
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
    testThreads.convention(ext.rustTestOptions.testThreads)
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
fun RustExtension.targets(rustBuildTargetOptions: Action<RustBuildTargetOptions>) {
    rustBuildTargetOptions.execute(this.rustBuildTargetOptions)
}

/**
 * Crate package configuration (Crate name, version, author, etc)
 */
@Suppress("unused")
fun RustManifestOptions.packaging(packageConfig: Action<Package>) {
    packageConfig.execute(this.packageConfig.get())
}

/**
 * Crate compiler options
 */
@Suppress("unused")
fun RustExtension.profiles(rustProfileOptions: Action<RustProfileOptions>) {
    rustProfileOptions.execute(this.rustProfileOptions)
}

/**
 * Configure this project as a library Crate
 */
@Suppress("unused")
fun RustExtension.library(libraryConfig: Action<RustLibraryOptions>) {
    if(this.rustLibraryOptions.isEnabled) {
        throw IllegalArgumentException("`library {}` should only be configured once!")
    }
    libraryConfig.execute(this.rustLibraryOptions)
    this.rustLibraryOptions.isEnabled = true //Enable it
}

/**
 * Configure a new binary Cargo target
 */
@Suppress("unused")
fun RustExtension.binary(binaryConfig: Action<RustBinariesOptions>) {
    val binary = objects.newInstance(RustBinariesOptions::class.java)

    binaryConfig.execute(binary)

    if(this.rustBinariesOptions.any { it.name.orNull == binary.name.orNull }) {
        throw IllegalArgumentException("`binary {}` parameter `name` must be unique! ${binary.name.orNull} has already been declared.")
    }

    binary.isEnabled = true
    this.rustBinariesOptions.add(binary)
}

/**
 * Configure a new example Cargo target
 */
@Suppress("unused")
fun RustExtension.example(exampleConfig: Action<RustExamplesOptions>) {
    val example = objects.newInstance(RustExamplesOptions::class.java)

    exampleConfig.execute(example)

    if(this.rustExamplesOptions.any { it.name.orNull == example.name.orNull }) {
        throw IllegalArgumentException("`example {}` parameter `name` must be unique! ${example.name.orNull} has already been declared.")
    }

    example.isEnabled = true
    this.rustExamplesOptions.add(example)
}

/**
 * Configure a new test Cargo target
 */
@Suppress("unused")
fun RustExtension.test(testConfig: Action<RustTestsOptions>) {
    val test = objects.newInstance(RustTestsOptions::class.java)

    testConfig.execute(test)

    if(this.rustTestsOptions.any { it.name.orNull == test.name.orNull }) {
        throw IllegalArgumentException("`test {}` parameter `name` must be unique! ${test.name.orNull} has already been declared.")
    }

    test.isEnabled = true
    this.rustTestsOptions.add(test)
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
        this.list.get().find {
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

internal fun StringBuilder.writeBooleanField(key: String, value: Boolean?, defaultValue: Boolean? = null) {
    if (value != defaultValue) {
        append("$key = $value\n")
    }
}

internal fun StringBuilder.writeCrateField(logger: Logger, crate: RustCrateOptions) {
    if(!crate.path.isPresent &&
        !crate.git.isPresent &&
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
        else if(crate.git.isPresent) {
            crateOptions.add("git = \"${crate.git.get()}\"")

            //We prioritize `rev` because `rev` and `branch` conflicts in Cargo, and `rev` points to the exact commit.
            if(crate.rev.isPresent) {
                crateOptions.add("rev = \"${crate.rev.get()}\"")

                if(crate.branch.isPresent)
                    logger.warn("Crate `rev` and `branch` are both set but they conflict in Cargo. Prioritizing `rev` and ignoring `branch`.")
            } else if(crate.branch.isPresent)
                crateOptions.add("branch = \"${crate.branch.get()}\"")
        }

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

internal fun RegularFile.relativeToManifest(manifest: File): String =
    this.asFile
        .toRelativeString(manifest.parentFile)
        .replace("\\", "/")

/**
 * Tries to read a list of TOML fields. Returns nothing if it doesn't exist or is not a String.
 *
 * We're not using an external TOML parser to avoid bloat.
 */
internal fun File.readTomlStringFields(objectKey: String, keys: List<String>): MutableMap<String, String> {
    val searchSpace = keys.toSet().toMutableList() //Remove duplicates and remove if already found
    val result = mutableMapOf<String, String>()
    var isInParent = false
    this.readLines().forEach { line ->
        if(!isInParent) {
            val objectCandidate = line.substringAfter("[").substringBefore("]").trim()

            if(objectCandidate == objectKey) {
                isInParent = true
            }
            return@forEach
        }

        if(line.isBlank()) isInParent = false
        val iterator = searchSpace.iterator()
        while(iterator.hasNext()) {
            val key = iterator.next()
            if(line.length >= key.length && line.take(key.length) == key) {
                if(line.substringAfter(key, "!").substringBefore("=", "!").isNotBlank()) continue
                val fieldValue = line.substringAfter("=").trim()
                if(!fieldValue.startsWith("\"") || !fieldValue.endsWith("\"")) continue
                result[key] = fieldValue.substring(1, fieldValue.length - 1)
                iterator.remove()
            }
        }
    }

    return result
}

internal fun ResolvedArtifactResult.asRustCrate(objectFactory: ObjectFactory, referenceManifestFile: File): RustCrateOptions {
    this.file.readTomlStringFields("package", listOf("name", "version")).apply {
        val name = get("name")
        val version = get("version")
        if(name.isNullOrBlank() || version.isNullOrBlank())
            throw IllegalArgumentException(
                "Cannot resolve this Rust crate because the name or version couldn't be found. Name: $name | Version: $version"
            )
        val options = objectFactory.newInstance(RustCrateOptions::class.java, name, version)

        options.path.set(
            this@asRustCrate
                .file
                .parentFile
                .toRelativeString(referenceManifestFile.parentFile)
                .replace("\\", "/")
        )

        return options
    }
}

internal fun ObjectFactory.readRustCrateFromFile(file: File): RustCrateOptions {
    assert(file.name.endsWith(".rc")) {
        "Attempted to read file $file but it's not a Java serialized RustCrateObject"
    }
    try {
        FileInputStream(file).use {
            val rustCrateObject = ObjectInputStream(it).readObject() as RustCrateObject

            val crateOptions = this.newInstance(
                RustCrateOptions::class.java,
                rustCrateObject.name,
                rustCrateObject.version
            )

            crateOptions.fromObject(rustCrateObject)

            return crateOptions
        }
    } catch (e: Exception) {
        file.delete()
        throw RuntimeException("Failed to deserialize file $file back to a RustCrateObject. Is it corrupted? Please run the task again", e)
    }
}

private fun Any.resolveDependencyNotation(objects: ObjectFactory): RustCrateOptions {
    return when(this) {
        is String -> {
            val name = this.substringBefore(":", "")
            val version = this.substringAfter(":", "")

            if(name.isBlank() || version.isBlank())
                throw IllegalArgumentException("Cannot resolve $this to name and version. Format -> `name:version`")

            objects.newInstance(RustCrateOptions::class.java, name, version)
        }
        is ProjectDependency -> objects.newInstance(RustCrateOptions::class.java, this.name, "unresolved")
        else -> throw IllegalDependencyNotation("${this::class.java.name} is not supported.")
    }
}

/**
 * Add a crate
 */
@Suppress("unused")
fun DependencyHandler.crate(dependencyNotation: Any, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        val crate = dependencyNotation.resolveDependencyNotation(objects)

        options?.execute(crate)

        if(crate.version == "unresolved") {
            add("crateNoConfigure", dependencyNotation)
            unresolvedDependencies.add(crate)
        } else {
            dependencies.add(crate)
        }
    }
}

/**
 * Add a dev only crate
 */
@Suppress("unused")
fun DependencyHandler.devCrate(dependencyNotation: Any, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        val crate = dependencyNotation.resolveDependencyNotation(objects)

        options?.execute(crate)

        if(crate.version == "unresolved") {
            add("devCrateNoConfigure", dependencyNotation)
            unresolvedDevDependencies.add(crate)
        } else {
            devDependencies.add(crate)
        }
    }
}


/**
 * Add a build only crate
 */
@Suppress("unused")
fun DependencyHandler.buildCrate(dependencyNotation: Any, options: Action<RustCrateOptions>? = null) {
    this.extensions.getByType(CrateLibrary::class.java).apply {
        val crate = dependencyNotation.resolveDependencyNotation(objects)

        options?.execute(crate)

        if(crate.version == "unresolved") {
            add("buildCrateNoConfigure", dependencyNotation)
            unresolvedBuildDependencies.add(crate)
        } else {
            buildDependencies.add(crate)
        }
    }
}