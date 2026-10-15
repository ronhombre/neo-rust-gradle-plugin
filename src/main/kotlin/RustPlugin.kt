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
import asia.hombre.neorust.container.BenchmarksConfigurationContainer
import asia.hombre.neorust.container.BinariesConfigurationContainer
import asia.hombre.neorust.container.ExamplesConfigurationContainer
import asia.hombre.neorust.container.LintsConfigurationContainer
import asia.hombre.neorust.container.ProfilesConfigurationContainer
import asia.hombre.neorust.container.TestsConfigurationContainer
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.container.InternalBuildConfigurationContainer
import asia.hombre.neorust.internal.escapedString
import asia.hombre.neorust.internal.task.CargoDefaultTask
import asia.hombre.neorust.internal.validatePackageName
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTestOptions
import asia.hombre.neorust.options.profiles.GenericProfileConfiguration
import asia.hombre.neorust.options.profiles.ProfileConfiguration
import asia.hombre.neorust.options.targets.BenchmarkConfiguration
import asia.hombre.neorust.options.targets.BinaryConfiguration
import asia.hombre.neorust.options.targets.CargoTargetConfiguration
import asia.hombre.neorust.options.targets.ExampleConfiguration
import asia.hombre.neorust.options.targets.LibraryConfiguration
import asia.hombre.neorust.options.targets.TestConfiguration
import asia.hombre.neorust.serializable.RustCrateObject
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoRun
import asia.hombre.neorust.task.CargoTest
import org.gradle.api.Action
import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.registering
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

internal fun CargoDefaultTask.setDefaultProperties(ext: RustExtension) {
    packageSelect.convention(ext.packageSelect)
    target.convention(ext.target)
    targetDirectory.convention(ext.targetDirectory)
    allFeatures.convention(ext.allFeatures)
    manifestPath.convention(ext.manifestPath)
    ignoreRustVersion.convention(ext.ignoreRustVersion)
    noDefaultFeatures.convention(ext.noDefaultFeatures)
    locked.convention(ext.locked)
    offline.convention(ext.offline)
    frozen.convention(ext.frozen)
    verbose.convention(ext.verbose)
    quiet.convention(ext.quiet)
    color.convention(ext.color)
    toolchain.convention(ext.toolchain)
    config.convention(ext.config)
    configPaths.convention(ext.configPaths)
    unstableFlags.convention(ext.unstableFlags)

    jobs.convention(ext.jobs)
    keepGoing.convention(ext.keepGoing)
    futureIncompatReport.convention(ext.futureIncompatReport)
}

internal fun CargoBuild.setBuildProperties(ext: RustExtension) {
    workspace.convention(ext.rustBuildOptions.workspace)
    exclude.convention(ext.rustBuildOptions.exclude)
    release.convention(ext.rustBuildOptions.release)
    profile.convention(ext.rustBuildOptions.profile)
    timings.convention(ext.rustBuildOptions.timings)
    messageFormat.convention(ext.rustBuildOptions.messageFormat)
    buildPlan.convention(ext.rustBuildOptions.buildPlan)
    buildAll.convention(ext.rustBuildOptions.buildAll)
}

internal fun CargoBench.setTestExecutionOptions(ext: RustExtension) {
    noRun.convention(ext.rustTestExecutionOptions.noRun)
    noCapture.convention(ext.rustTestExecutionOptions.noCapture)
    noFailFast.convention(ext.rustTestExecutionOptions.noFailFast)
}

internal fun CargoTest.setTestProperties(ext: RustExtension) {
    testThreads.convention(ext.rustTestExecutionOptions.testThreads)
}

internal fun CargoPublish.setPublishProperties(ext: RustExtension) {
    dryRun.convention(ext.rustPublishOptions.dryRun)
    noVerify.convention(ext.rustPublishOptions.noVerify)
    allowDirty.convention(ext.rustPublishOptions.allowDirty)
    token.convention(ext.rustPublishOptions.token)
    index.convention(ext.rustPublishOptions.index)
    registry.convention(ext.rustPublishOptions.registry)
}

internal fun CargoRun.setRunProperties(configuration: BinaryConfiguration) {
    arguments.convention(configuration.arguments)
    environment.convention(configuration.environment)
    workingDir.convention(configuration.workingDir)
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
fun RustExtension.benchmarking(rustBenchmarkOptions: Action<RustTestOptions>) {
    rustBenchmarkOptions.execute(this.rustBenchmarkOptions)
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
 * Crate compiler options
 */
/*@Suppress("unused")
fun RustExtension.profiles(rustProfileOptions: Action<RustProfileOptions>) {
    rustProfileOptions.execute(this.rustProfileOptions)
}*/

/**
 * Configure this project as a library Crate
 */
@Suppress("unused")
fun RustExtension.library(libraryConfig: Action<LibraryConfiguration>) {
    if(this.libraryConfiguration.isNotEmpty()) {
        throw IllegalArgumentException("`library {}` should only be configured once!")
    }
    this.libraryConfiguration.add(
        objects.newInstance(LibraryConfiguration::class.java, this.name.get()).also {
            libraryConfig.execute(it)
        }
    )
}

@Suppress("unused")
fun RustExtension.lints(action: LintsConfigurationContainer.() -> Unit) {
    action(lintsConfiguration)
}

@Suppress("unused")
fun RustExtension.binaries(action: BinariesConfigurationContainer.() -> Unit) {
    action(binariesConfiguration)
}

@Suppress("unused")
fun RustExtension.tests(action: TestsConfigurationContainer.() -> Unit) {
    action(testsConfiguration)
}

@Suppress("unused")
fun RustExtension.benchmarks(action: BenchmarksConfigurationContainer.() -> Unit) {
    action(benchmarksConfiguration)
}

@Suppress("unused")
fun RustExtension.examples(action: ExamplesConfigurationContainer.() -> Unit) {
    action(examplesConfiguration)
}

/**
 * Exclude a binary or more from the build with the specific name
 */
@Suppress("unused")
@JvmName("excludeBinary")
fun NamedDomainObjectContainer<BinaryConfiguration>.exclude(vararg name: String) {
    name.forEach {
        this[it].isExcluded = true
    }
}

/**
 * Exclude an example or more from the build with the specific name
 */
@Suppress("unused")
@JvmName("excludeExample")
fun NamedDomainObjectContainer<ExampleConfiguration>.exclude(vararg name: String) {
    name.forEach {
        this[it].isExcluded = true
    }
}

/**
 * Exclude a test or more from the build with the specific name
 */
@Suppress("unused")
@JvmName("excludeTest")
fun NamedDomainObjectContainer<TestConfiguration>.exclude(vararg name: String) {
    name.forEach {
        this[it].isExcluded = true
    }
}

/**
 * Exclude a bench or more from the build with the specific name
 */
@Suppress("unused")
@JvmName("excludeBenchmark")
fun NamedDomainObjectContainer<BenchmarkConfiguration>.exclude(vararg name: String) {
    name.forEach {
        this[it].isExcluded = true
    }
}

@Suppress("unused")
fun RustExtension.profiles(action: ProfilesConfigurationContainer.() -> Unit) {
    action(profilesConfiguration)
}

@Suppress("unused")
fun ProfileConfiguration.buildOverride(action: GenericProfileConfiguration.() -> Unit) {
    val overrides = this.overrides.getOrElse(emptyMap()).toMutableMap()

    if(overrides.contains("#build#"))
        throw IllegalArgumentException("`buildOverride` has already been declared once.")
    else {
        try {
            val configuration = this.objects.newInstance(GenericProfileConfiguration::class.java, true)
            action(configuration)
            overrides["#build#"] = configuration

            this.overrides.set(overrides)
        } catch (e: IllegalStateException) {
            val propertyName = e.message?.substringAfter('\'')?.substringBefore('\'')
            throw IllegalStateException("You cannot override '$propertyName'. You must set it in the profile '${this.name}'.", e)
        }
    }
}

@Suppress("unused")
fun ProfileConfiguration.packageOverride(packageName: String, action: GenericProfileConfiguration.() -> Unit) {
    if(!validatePackageName(packageName) && packageName != "*")
        throw IllegalArgumentException("`packageName` must be a valid cargo package name.")

    val overrides = this.overrides.getOrElse(emptyMap()).toMutableMap()

    if(overrides.contains(packageName))
        throw IllegalArgumentException("`packageOverride(\"$packageName\")` has already been declared once.")
    else {
        try {
            val configuration = this.objects.newInstance(GenericProfileConfiguration::class.java, true)
            action(configuration)
            overrides[packageName] = configuration

            this.overrides.set(overrides)
        } catch (e: IllegalStateException) {
            val propertyName = e.message?.substringAfter('\'')?.substringBefore('\'')
            throw IllegalStateException("You cannot override '$propertyName'. You must set it in the profile '${this.name}'.", e)
        }
    }
}

/**
 * Set a feature for Cargo
 */
@Suppress("unused")
fun RustFeaturesOptions.feature(name: String, vararg values: String) {
    this.featureMap.getting(name).orNull?.toMutableList()?.apply {
        addAll(values.toList())
    }?: this.featureMap.put(name, values.toList())
}

internal inline fun StringBuilder.writeTable(name: String, block: StringBuilder.() -> Unit) {
    this.append("\n[$name]\n")
    this.block()
}

internal fun StringBuilder.writeField(key: String, value: String?) {
    if (!value.isNullOrBlank()) {
        append("$key = \"${value.escapedString()}\"\n".replace("\\", "\\\\"))
    }
}

internal fun StringBuilder.writeNumberField(key: String, value: Number?) {
    if (value != null) {
        append("$key = $value\n")
    }
}

internal fun StringBuilder.writeArrayField(key: String, values: Collection<String>, forced: Boolean = false) {
    if (values.isNotEmpty() || forced) {
        append("$key = [")
        values.forEachIndexed { index, value ->
            if (index > 0) append(", ")
            append("\"${value.trim().escapedString()}\"")
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
            crateOptions.add("path = \"${crate.path.get().escapedString()}\"")
        else if(crate.git.isPresent) {
            crateOptions.add("git = \"${crate.git.get().escapedString()}\"")

            //We prioritize `rev` because `rev` and `branch` conflicts in Cargo, and `rev` points to the exact commit.
            if(crate.rev.isPresent) {
                crateOptions.add("rev = \"${crate.rev.get().escapedString()}\"")

                if(crate.branch.isPresent)
                    logger.warn("Crate `rev` and `branch` are both set but they conflict in Cargo. Prioritizing `rev` and ignoring `branch`.")
            } else if(crate.branch.isPresent)
                crateOptions.add("branch = \"${crate.branch.get().escapedString()}\"")
        }

        if(crate.registry.isPresent && crate.registry.get() != "crates.io")
            crateOptions.add("registry = \"${crate.registry.get().escapedString()}\"")

        if(crate.features.isPresent && crate.features.get().isNotEmpty())
            crateOptions.add("features = [${crate.features.get().joinToString(", ") { "\"${it.escapedString()}\""}}]")

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

/**
 * Inherit the same entry .rs file as the `base`.
 *
 * Changing the entry .rs file for the `base` also applies to this.
 *
 * @param base The base configuration to inherit from.
 * @param configuration The configuration to apply to the inherited configuration.
 */
@Suppress("unused")
fun <T: CargoTargetConfiguration, U: InternalBuildConfigurationContainer<T>> U.inheriting(base: T, configuration: T.() -> Unit = {}) = registering {
    sameAs(base)
    configuration()
}