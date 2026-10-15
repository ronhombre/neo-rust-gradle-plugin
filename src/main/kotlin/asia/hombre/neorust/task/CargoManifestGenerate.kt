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

package asia.hombre.neorust.task

import asia.hombre.neorust.CrateLibrary
import asia.hombre.neorust.option.OptLevel
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustPackageOptions
import asia.hombre.neorust.options.profiles.GenericProfileConfiguration
import asia.hombre.neorust.options.profiles.ProfileConfiguration
import asia.hombre.neorust.options.targets.BenchmarkConfiguration
import asia.hombre.neorust.options.targets.BinaryConfiguration
import asia.hombre.neorust.options.targets.CargoTargetConfiguration
import asia.hombre.neorust.options.targets.ExampleConfiguration
import asia.hombre.neorust.options.targets.LibraryConfiguration
import asia.hombre.neorust.options.targets.TestConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.NormalizeLineEndings
import readRustCrateFromFile
import relativeToManifest
import writeArrayField
import writeBooleanField
import writeCrateField
import writeField
import writeNumberField
import writeTable
import java.io.File
import javax.inject.Inject

/**
 * Generate a Cargo-compatible `Cargo.toml` which is used when `cargo` is run.
 *
 * @since 0.2.0
 * @author Ron Lauren Hombre
 */
@CacheableTask
abstract class CargoManifestGenerate @Inject constructor(): DefaultTask() {
    @get:Inject
    internal abstract val objects: ObjectFactory

    @get:Nested
    internal abstract val crateLibrary: Property<CrateLibrary>
    @get:Nested
    internal abstract val rustPackageOptions: Property<RustPackageOptions>
    @get:Nested
    internal abstract val rustFeaturesOptions: Property<RustFeaturesOptions>

    //Cargo Targets
    @get:Nested
    internal abstract val libraryConfiguration: ListProperty<LibraryConfiguration>
    @get:Nested
    internal abstract val binaryConfiguration: ListProperty<BinaryConfiguration>
    @get:Nested
    internal abstract val exampleConfiguration: ListProperty<ExampleConfiguration>
    @get:Nested
    internal abstract val testConfiguration: ListProperty<TestConfiguration>
    @get:Nested
    internal abstract val benchmarkConfiguration: ListProperty<BenchmarkConfiguration>

    //Profiles
    @get:Nested
    internal abstract val profileConfiguration: ListProperty<ProfileConfiguration>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:NormalizeLineEndings
    internal abstract val resolvedCrates: DirectoryProperty

    @get:OutputFile
    abstract val manifestPath: RegularFileProperty

    @TaskAction
    fun generateManifest() {
        val cargoToml = manifestPath.get().asFile
        //Create parent directories if they don't exist.
        //Delete old Cargo.toml since it might be outdated.
        if(cargoToml.exists()) cargoToml.delete()

        val resolvedDir = resolvedCrates.get().asFile

        //Collect all crates to their own lists so we can append resolved crates
        val dependencies = mutableListOf<RustCrateOptions>()
        val devDependencies = mutableListOf<RustCrateOptions>()
        val buildDependencies = mutableListOf<RustCrateOptions>()

        dependencies.addAll(crateLibrary.get().dependencies.get())
        devDependencies.addAll(crateLibrary.get().devDependencies.get())
        buildDependencies.addAll(crateLibrary.get().buildDependencies.get())

        //Add all resolved crates from Gradle subprojects
        dependencies.addAll(resolvedDir.resolve("crates").listFiles()?.map(objects::readRustCrateFromFile)?: emptyList())
        devDependencies.addAll(resolvedDir.resolve("devCrates").listFiles()?.map(objects::readRustCrateFromFile)?: emptyList())
        buildDependencies.addAll(resolvedDir.resolve("buildCrates").listFiles()?.map(objects::readRustCrateFromFile)?: emptyList())

        //Apply configuration for resolved crates
        crateLibrary.get().unresolvedDependencies.get().forEach { crate ->
            dependencies.find { it.name == crate.name }?.copyIfNotSetFrom(crate)
        }
        crateLibrary.get().unresolvedDevDependencies.get().forEach { crate ->
            devDependencies.find { it.name == crate.name }?.copyIfNotSetFrom(crate)
        }
        crateLibrary.get().unresolvedBuildDependencies.get().forEach { crate ->
            buildDependencies.find { it.name == crate.name }?.copyIfNotSetFrom(crate)
        }

        val content = StringBuilder()
        val packageOptions = rustPackageOptions.get()

        content.append(
            """
                # THIS FILE IS AUTO-GENERATED by neo-rust-gradle-plugin
                # MODIFY YOUR build.gradle.kts/build.gradle FILE INSTEAD!
                # CHANGES HERE WILL BE LOST!!!
            """.trimIndent()
        )

        content.writeTable("package") {
            writeField("name", packageOptions.name.get())
            writeField("version", packageOptions.version.get())
            if(packageOptions.authors.isPresent)
                writeArrayField("authors", packageOptions.authors.get())
            if(packageOptions.edition.isPresent)
                writeField("edition", packageOptions.edition.get())
            if(packageOptions.rustVersion.isPresent)
                writeField("rust-version", packageOptions.rustVersion.get())
            if(packageOptions.description.isPresent)
                writeField("description", packageOptions.description.get())
            if(packageOptions.documentation.isPresent)
                writeField("documentation", packageOptions.documentation.get())
            if(packageOptions.readme.isPresent)
                writeField("readme", packageOptions.readme.get())
            if(packageOptions.homepage.isPresent)
                writeField("homepage", packageOptions.homepage.get())
            if(packageOptions.repository.isPresent)
                writeField("repository", packageOptions.repository.get())
            if(packageOptions.license.isPresent)
                writeField("license", packageOptions.license.get())
            if(packageOptions.licenseFile.isPresent)
                writeField("license-file", packageOptions.licenseFile.get().asFile.absolutePath)
            if(packageOptions.keywords.isPresent)
                writeArrayField("keywords", packageOptions.keywords.get())
            if(packageOptions.categories.isPresent)
                writeArrayField("categories", packageOptions.categories.get())
            if(packageOptions.workspace.isPresent)
                writeField("workspace", packageOptions.workspace.get().asFile.absolutePath)
            if(packageOptions.buildFile.isPresent)
                writeField("build", packageOptions.buildFile.get().asFile.absolutePath)
            if(packageOptions.links.isPresent)
                writeField("links", packageOptions.links.get())
            if(packageOptions.exclude.isPresent)
                writeArrayField("exclude", packageOptions.exclude.get())
            if(packageOptions.include.isPresent)
                writeArrayField("include", packageOptions.include.get())

            when {
                !packageOptions.publishEnabled.getOrElse(true) -> append("publish = false\n")
                packageOptions.publish.getOrElse(mutableListOf()).isNotEmpty() -> writeArrayField("publish", packageOptions.publish.get())
            }

            if(packageOptions.defaultRun.isPresent)
                writeField("default-run", packageOptions.defaultRun.get())

            //Disable auto-discovery since we violate the standard Rust directory structure anyway
            writeBooleanField("autolib", false)
            writeBooleanField("autobins", false)
            writeBooleanField("autoexamples", false)
            writeBooleanField("autotests", false)
            writeBooleanField("autobenches", false)
        }

        for (configuration in profileConfiguration.get()) {
            content.writeProfileConfiguration("profile.${configuration.name.lowercase()}", configuration)

            for ((name, overrideConfiguration) in configuration.overrides.get()) {
                if(name == "#build#") content.writeProfileConfiguration(
                    "profile.${configuration.name.lowercase()}.build-override",
                    overrideConfiguration
                )
                else {
                    val packageName = if(name == "*") "\"*\"" else name
                    content.writeProfileConfiguration(
                        "profile.${configuration.name.lowercase()}.package.$packageName",
                        overrideConfiguration
                    )
                }
            }
        }

        val featuresMap: Map<String, List<String>> = rustFeaturesOptions.get().featureMap.get()

        if(featuresMap.isNotEmpty()) {
            content.writeTable("features") {
                featuresMap.forEach { (name, features) ->
                    writeArrayField(name, features.toSet(), true)
                }
            }
        }

        if(dependencies.isNotEmpty()) {
            content.writeTable("dependencies") {
                dependencies.forEach { rustCrate ->
                    writeCrateField(logger, rustCrate)
                }
            }
        }

        if(devDependencies.isNotEmpty()) {
            content.writeTable("dev-dependencies") {
                devDependencies.forEach { rustCrate ->
                    writeCrateField(logger, rustCrate)
                }
            }
        }

        if(buildDependencies.isNotEmpty()) {
            content.writeTable("build-dependencies") {
                buildDependencies.forEach { rustCrate ->
                    writeCrateField(logger, rustCrate)
                }
            }
        }

        val rustLibraryOptions = libraryConfiguration.get()

        rustLibraryOptions.firstOrNull()?.let {
            content.writeTargetConfiguration("lib", it, cargoToml)
        }

        val rustBinariesOptions = binaryConfiguration.get().filterNot { it.isExcluded || it.isShadow }

        rustBinariesOptions.forEach { binary ->
            content.writeTargetConfiguration("[bin]", binary, cargoToml)
        }

        val rustExamplesOptions = exampleConfiguration.get().filterNot { it.isExcluded || it.isShadow }

        rustExamplesOptions.forEach { example ->
            content.writeTargetConfiguration("[example]", example, cargoToml)
        }

        val rustTestsOptions = testConfiguration.get().filterNot { it.isExcluded || it.isShadow }

        rustTestsOptions.forEach { test ->
            content.writeTargetConfiguration("[test]", test, cargoToml)
        }

        val rustBenchOptions = benchmarkConfiguration.get().filterNot { it.isExcluded || it.isShadow }

        rustBenchOptions.forEach { bench ->
            content.writeTargetConfiguration("[bench]", bench, cargoToml)
        }

        //TODO: Resolve custom registries
        /*writeTable(content, "registries") {
            writeField("path", libOptions.path)
            writeArrayField("crate-type", libOptions.crateType)
        }*/

        /*if(featuresList.get().isNotEmpty()) {
            content.writeTable("features") {
                featuresList.get().forEach { (key, values) ->
                    writeArrayField(key, values, true)
                }
            }
        }*/

        cargoToml.writeText(content.removePrefix("\n").toString())
    }

    private fun StringBuilder.writeProfileConfiguration(name: String, configuration: GenericProfileConfiguration) {
        if(!configuration.isUsed()) return

        writeTable(name) {
            configuration.optLevel.orNull?.let {
                val key = "opt-level"
                when(it) {
                    OptLevel.z,
                    OptLevel.s -> writeField(key, it.name)
                    OptLevel.zero -> writeNumberField(key, 0)
                    OptLevel.one -> writeNumberField(key, 1)
                    OptLevel.two -> writeNumberField(key, 2)
                    OptLevel.three -> writeNumberField(key, 3)
                }
            }
            writeField("debug", configuration.debug.orNull?.name?.replace('_', '-'))
            writeField("split-debuginfo", configuration.splitDebugInfo.orNull)
            writeField("strip", configuration.strip.orNull?.name)
            writeBooleanField("debug-assertions", configuration.debugAssertions.orNull)
            writeBooleanField("overflow-checks", configuration.overflowChecks.orNull)
            writeField("lto", configuration.lto.orNull?.name)
            writeField("panic", configuration.panic.orNull?.name)
            writeBooleanField("incremental", configuration.incremental.orNull)
            writeNumberField("codegen-units", configuration.codegenUnits.orNull)
            writeBooleanField("rpath", configuration.rpath.orNull)
        }
    }

    private fun StringBuilder.writeTargetConfiguration(name: String, configuration: CargoTargetConfiguration, cargoToml: File) {
        if(!configuration.isEnabled && (!configuration.path.isPresent || !configuration.path.get().asFile.exists())) {
            val type = configuration.javaClass.name.substringAfterLast(".").substringBefore("Configuration")
            val errorMessage = "Target with type $type and name $name cannot be found at the expected location: '${configuration.currentDirectory.asFile.path}' as $name.rs or $name${File.separator}main.rs"
            throw IllegalArgumentException(errorMessage + if(name == "lib") " or lib.rs" else "")
        }
        writeTable(name) {
            writeField("name", configuration.actualName)
            writeField("path", configuration.path.get().relativeToManifest(cargoToml))
            writeBooleanField("test", configuration.test.orNull)
            writeBooleanField("doctest", configuration.doctest.orNull)
            writeBooleanField("bench", configuration.bench.orNull)
            writeBooleanField("doc", configuration.doc.orNull)
            writeBooleanField("procMacro", configuration.procMacro.orNull)
            writeBooleanField("harness", configuration.harness.orNull)
            //The user can't modify this, so we ignore it for the other configurations
            if(configuration is LibraryConfiguration)
                writeArrayField("crate-type", configuration.crateType.get())
            writeArrayField("required-features", configuration.requiredFeatures.get())
        }
    }
}