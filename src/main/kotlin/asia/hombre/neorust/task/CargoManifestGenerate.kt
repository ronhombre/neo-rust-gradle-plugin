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
import asia.hombre.neorust.options.RustBinariesOptions
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.RustExamplesOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustLibraryOptions
import asia.hombre.neorust.options.RustManifestOptions
import asia.hombre.neorust.options.RustProfileOptions
import asia.hombre.neorust.options.RustTestsOptions
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import readRustCrateFromFile
import relativeToManifest
import writeArrayField
import writeBooleanField
import writeCrateField
import writeField
import writeTable
import javax.inject.Inject

/**
 * Generate a Cargo-compatible `Cargo.toml` which is used when `cargo` is run.
 *
 * @since 0.2.0
 * @author Ron Lauren Hombre
 */
abstract class CargoManifestGenerate @Inject constructor(): DefaultTask() {
    @get:Inject
    internal abstract val objects: ObjectFactory

    @get:Nested
    internal abstract val crateLibrary: Property<CrateLibrary>
    @get:Nested
    internal abstract val rustManifestOptions: Property<RustManifestOptions>
    @get:Nested
    internal abstract val rustProfileOptions: Property<RustProfileOptions>
    @get:Nested
    internal abstract val rustFeaturesOptions: Property<RustFeaturesOptions>

    //Cargo Targets
    @get:Nested
    internal abstract val rustLibraryOptions: Property<RustLibraryOptions>
    @get:Nested
    internal abstract val rustBinariesOptions: ListProperty<RustBinariesOptions>
    @get:Nested
    internal abstract val rustExamplesOptions: ListProperty<RustExamplesOptions>
    @get:Nested
    internal abstract val rustTestsOptions: ListProperty<RustTestsOptions>

    @get:Input
    internal abstract val featuresList: MapProperty<String, List<String>>
    @get:InputDirectory
    internal abstract val resolvedCrates: DirectoryProperty

    @get:OutputFile
    abstract val manifestPath: RegularFileProperty

    @TaskAction
    fun generateManifest() {
        val cargoToml = manifestPath.get().asFile
        //Create parent directories if they don't exist.
        //Delete old Cargo.toml since it might be outdated.
        if(cargoToml.exists()) cargoToml.delete()

        val manifestOptions = rustManifestOptions.get()

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
        val packageOptions = manifestOptions.packageConfig.get()

        content.append(
            """
                # THIS FILE IS AUTO-GENERATED by neo-rust-gradle-plugin
                # MODIFY YOUR build.gradle.kts/build.gradle FILE INSTEAD!
                # CHANGES HERE WILL BE LOST!!!
            """.trimIndent()
        )


        content.writeTable("package") {
            if(!packageOptions.name.isPresent)
                throw IllegalArgumentException("Package 'name' cannot be unset.")

            writeField("name", packageOptions.name.get())
            if(packageOptions.version.isPresent)
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
                writeField("license-file", packageOptions.licenseFile.get())
            if(packageOptions.keywords.isPresent)
                writeArrayField("keywords", packageOptions.keywords.get())
            if(packageOptions.categories.isPresent)
                writeArrayField("categories", packageOptions.categories.get())
            if(packageOptions.workspace.isPresent)
                writeField("workspace", packageOptions.workspace.get())
            if(packageOptions.buildFile.isPresent)
                writeField("build", packageOptions.buildFile.get())
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
            if(packageOptions.autoBins.isPresent)
                writeBooleanField("autobins", packageOptions.autoBins.get())
            if(packageOptions.autoExamples.isPresent)
                writeBooleanField("autoexamples", packageOptions.autoExamples.get())
            if(packageOptions.autoTests.isPresent)
                writeBooleanField("autotests", packageOptions.autoTests.get())
            if(packageOptions.autoBenches.isPresent)
                writeBooleanField("autobenches", packageOptions.autoBenches.get())
        }

        if(rustProfileOptions.get().dev.get().isNotEmpty()) content.writeTable("profile.dev") {
            rustProfileOptions.get().dev.get().forEach { dev ->
                writeField(dev.key, dev.value)
            }
        }

        if(rustProfileOptions.get().release.get().isNotEmpty()) content.writeTable("profile.release") {
            rustProfileOptions.get().release.get().forEach { release ->
                writeField(release.key, release.value)
            }
        }

        if(rustProfileOptions.get().test.get().isNotEmpty()) content.writeTable("profile.test") {
            rustProfileOptions.get().test.get().forEach { test ->
                writeField(test.key, test.value)
            }
        }

        if(rustProfileOptions.get().bench.get().isNotEmpty()) content.writeTable("profile.bench") {
            rustProfileOptions.get().bench.get().forEach { bench ->
                writeField(bench.key, bench.value)
            }
        }

        val featuresMap: MutableList<RustFeaturesOptions.Feature> = mutableListOf()

        featuresMap.addAll(rustFeaturesOptions.get().list.get())
        featuresMap.addAll(featuresList.get().map { feature ->
            RustFeaturesOptions.Feature(feature.key, feature.value)
        })

        if(featuresMap.isNotEmpty()) {
            content.writeTable("features") {
                featuresMap.forEach { feature ->
                    writeArrayField(feature.name, feature.values, true)
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

        val rustLibraryOptions = rustLibraryOptions.get()

        if(rustLibraryOptions.isEnabled) content.writeTable("lib") {
            writeField("name", rustLibraryOptions.name.orNull)
            writeField("path", rustLibraryOptions.path.get().relativeToManifest(cargoToml))
            writeBooleanField("test", rustLibraryOptions.test.orNull)
            writeBooleanField("doctest", rustLibraryOptions.doctest.orNull)
            writeBooleanField("bench", rustLibraryOptions.bench.orNull)
            writeBooleanField("doc", rustLibraryOptions.doc.orNull)
            writeBooleanField("procMacro", rustLibraryOptions.procMacro.orNull)
            writeBooleanField("harness", rustLibraryOptions.harness.orNull)
            writeArrayField("crate-type", rustLibraryOptions.crateType.get())
            //This has no effect on [lib], but we let the user make that mistake
            writeArrayField("required-features", rustLibraryOptions.requiredFeatures.get())
        }

        val rustBinariesOptions = rustBinariesOptions.get()

        rustBinariesOptions.forEach { binary ->
            content.writeTable("[bin]") {
                writeField("name", binary.name.orNull)
                writeField("path", binary.path.get().relativeToManifest(cargoToml))
                writeBooleanField("test", binary.test.orNull)
                writeBooleanField("doctest", binary.doctest.orNull)
                writeBooleanField("bench", binary.bench.orNull)
                writeBooleanField("doc", binary.doc.orNull)
                writeBooleanField("procMacro", binary.procMacro.orNull)
                writeBooleanField("harness", binary.harness.orNull)
                //The user can't modify this, so we ignore it
                //writeArrayField("crate-type", binary.crateType.get())
                writeArrayField("required-features", binary.requiredFeatures.get())
            }
        }

        val rustExamplesOptions = rustExamplesOptions.get()

        rustExamplesOptions.forEach { example ->
            content.writeTable("[example]") {
                writeField("name", example.name.orNull)
                writeField("path", example.path.get().relativeToManifest(cargoToml))
                writeBooleanField("test", example.test.orNull)
                writeBooleanField("doctest", example.doctest.orNull)
                writeBooleanField("bench", example.bench.orNull)
                writeBooleanField("doc", example.doc.orNull)
                writeBooleanField("procMacro", example.procMacro.orNull)
                writeBooleanField("harness", example.harness.orNull)
                //The user can't modify this, so we ignore it
                //writeArrayField("crate-type", example.crateType.get())
                writeArrayField("required-features", example.requiredFeatures.get())
            }
        }

        val rustTestsOptions = rustTestsOptions.get()

        rustTestsOptions.forEach { test ->
            content.writeTable("[test]") {
                writeField("name", test.name.orNull)
                writeField("path", test.path.get().relativeToManifest(cargoToml))
                writeBooleanField("test", test.test.orNull)
                writeBooleanField("doctest", test.doctest.orNull)
                writeBooleanField("bench", test.bench.orNull)
                writeBooleanField("doc", test.doc.orNull)
                writeBooleanField("procMacro", test.procMacro.orNull)
                writeBooleanField("harness", test.harness.orNull)
                //The user can't modify this, so we ignore it
                //writeArrayField("crate-type", example.crateType.get())
                writeArrayField("required-features", test.requiredFeatures.get())
            }
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
}