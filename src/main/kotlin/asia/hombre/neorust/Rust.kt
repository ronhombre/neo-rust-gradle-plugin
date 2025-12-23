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

package asia.hombre.neorust

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import asia.hombre.neorust.options.RustCrateOptions
import asia.hombre.neorust.options.targets.BinaryConfiguration
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoCheck
import asia.hombre.neorust.task.CargoClean
import asia.hombre.neorust.task.CargoManifestGenerate
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoRun
import asia.hombre.neorust.task.CargoTest
import asia.hombre.neorust.task.ResolveCrates
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import readRustCrateFromFile
import readTomlStringFields
import setBenchProperties
import setBuildProperties
import setDefaultProperties
import setPublishProperties
import setRunProperties
import setTargettedProperties
import setTestProperties
import java.io.File
import java.io.IOException
import java.security.MessageDigest

@Suppress("unused")
class Rust: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("rust", RustExtension::class.java)
        val crateLibrary = target.objects.newInstance(CrateLibrary::class.java)
        target.dependencies.extensions.add("rustDependencies", crateLibrary)

        //Detect test environment
        if(IS_TEST_ENVIRONMENT)
            println("Warning: Test environment detected!\nConfigs and Tasks are now explicitly named.")

        //Tasks
        target.tasks.register("checkCargoExists", CargoCheck::class.java) {
            checkCache.set(target.layout.buildDirectory.file(".cargo-exists"))

            onlyIf {
                !checkCache.get().asFile.exists()
            }
        }

        val crates = target.configurations.create("crateNoConfigure") {
            isCanBeResolved = true
            isCanBeConsumed = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category::class.java, "cargo-manifest"))
            }
        }

        val devCrates = target.configurations.create("devCrateNoConfigure") {
            isCanBeResolved = true
            isCanBeConsumed = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category::class.java, "cargo-manifest"))
            }
        }

        val buildCrates = target.configurations.create("buildCrateNoConfigure") {
            isCanBeResolved = true
            isCanBeConsumed = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category::class.java, "cargo-manifest"))
            }
        }

        val resolveCrates = tryRegisterTask {
            target.tasks.register("resolveCrates", ResolveCrates::class.java) {
                inputs.files(crates)
                inputs.files(devCrates)
                inputs.files(buildCrates)

                cratesArtifacts.set(crates.incoming.artifacts)
                devCratesArtifacts.set(devCrates.incoming.artifacts)
                buildCratesArtifacts.set(buildCrates.incoming.artifacts)
                referenceManifestPath.set(extension.manifestPath)
                resolvedOutput.set(target.layout.buildDirectory.dir("resolver"))

                outputs.dir(resolvedOutput)
            }.get()
        } as ResolveCrates

        val generateCargoManifest = tryRegisterTask {
            target.tasks.register("generateCargoManifest", CargoManifestGenerate::class.java) {
                group = "build"
                rustPackageOptions.set(extension.rustPackageOptions)
                rustProfileOptions.set(extension.rustProfileOptions)
                rustFeaturesOptions.set(extension.rustFeaturesOptions)
                libraryConfiguration.set(extension.libraryConfiguration)
                binaryConfiguration.set(extension.binariesConfiguration)
                exampleConfiguration.set(extension.examplesConfiguration)
                testConfiguration.set(extension.testsConfiguration)
                benchmarkConfiguration.set(extension.benchmarksConfiguration)
                this.crateLibrary.set(crateLibrary)
                featuresList.set(extension.featuresList)
                manifestPath.set(extension.manifestPath)
                resolvedCrates.set(resolveCrates.resolvedOutput)

                inputs.dir(resolvedCrates)
                outputs.file(manifestPath)
            }.get()
        } as CargoManifestGenerate

        target.configurations.create("crateElements") {
            isCanBeResolved = false
            isCanBeConsumed = true
            @Suppress("UnstableApiUsage")
            isCanBeDeclared = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category::class.java, "cargo-manifest"))
            }
            outgoing {
                artifact(generateCargoManifest.manifestPath) {
                    builtBy(generateCargoManifest)
                }
            }
        }

        var cleanTask = "clean" + addIfTest()
        cleanTask += addIfConflictingTask(target, cleanTask)
        tryRegisterTask {
            target.tasks.register(cleanTask, CargoClean::class.java) {
                dependsOn("generateCargoManifest")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()

                inputs.file(manifestPath)

                doNotTrackState("Clean task must run on demand")
            }.get()
        }

        var publishTask = "publish" + addIfTest()
        publishTask += addIfConflictingTask(target, publishTask)
        tryRegisterTask {
            target.tasks.register(publishTask, CargoPublish::class.java) {
                dependsOn("generateCargoManifest")
                group = "publishing"
                this.ext = extension
                setDefaultProperties()
                setPublishProperties()

                inputs.file(manifestPath)
            }.get()
        }

        //Auto-resolvers
        if(extension.autoLib.get()) autoResolveLibrary(target, extension)
        if(extension.autoBins.get()) autoResolveBinaries(target, extension)
        if(extension.autoTests.get()) autoResolveTests(target, extension)
        if(extension.autoBenches.get()) autoResolveBenchmarks(target, extension)
        if(extension.autoExamples.get()) autoResolveExamples(target, extension)

        target.afterEvaluate {
            extension.rustPackageOptions.apply {
                name.convention(project.name.lowercase())
                version.convention(project.version.toString())
                description.convention(project.description)
            }

            //List of all resolved crates that are referenced using the Gradle `project(":name")` DSL. Flattened and
            //no duplicates. Error checked to catch edge cases and to inform the user about them.
            val gradleRustDependencies = try {
                resolveCrates
                    .resolvedOutput
                    .asFileTree
                    .files
                    .filter { it.extension == "rc" }
                    .map(target.objects::readRustCrateFromFile)
            } catch (e: IOException) {
                logger.error("Couldn't read the resolved Rust crates. Do we have the correct file permissions?", e)
                null
            } catch (e: RuntimeException) {
                logger.error("Corrupted resolved Rust crate detected and has been deleted. Please re-run any task to re-generate it.", e)
                null
            } catch (e: Exception) {
                logger.error("Unknown error encountered.", e)
                null
            }
            val digestBuffer = ByteArray(DEFAULT_BUFFER_SIZE)

            var buildTask = "build" + addIfTest()
            buildTask += addIfConflictingTask(target, buildTask)
            tryRegisterTask {
                val task = target.tasks.register(buildTask, CargoBuild::class.java)

                task.configure {
                    dependsOn("generateCargoManifest")
                    group = "build"
                    description = "Builds all the binaries using the default profile"
                    this.ext = extension
                    setDefaultProperties()
                    setTargettedProperties()
                    setBuildProperties()

                    inputs.file(manifestPath)

                    doNotTrackState("Build task must run on demand")
                }

                return@tryRegisterTask task.get()
            }

            var benchTask = "bench" + addIfTest()
            benchTask += addIfConflictingTask(target, benchTask)
            tryRegisterTask {
                target.tasks.register(benchTask, CargoBench::class.java) {
                    dependsOn("generateCargoManifest")
                    group = "verification"
                    this.ext = extension
                    setDefaultProperties()
                    setTargettedProperties()
                    setBenchProperties()

                    inputs.file(manifestPath)

                    doNotTrackState("Bench task must run on demand")
                }.get()
            }

            var testTask = "test" + addIfTest()
            testTask += addIfConflictingTask(target, testTask)
            tryRegisterTask {
                target.tasks.register(testTask, CargoTest::class.java) {
                    dependsOn("generateCargoManifest")
                    group = "verification"
                    this.ext = extension
                    setDefaultProperties()
                    setTargettedProperties()
                    setBenchProperties()
                    setTestProperties()

                    inputs.file(manifestPath)

                    doNotTrackState("Test task must run on demand")
                }.get()
            }

            extension
                .binariesConfiguration
                .filterNot {
                    it.isExcluded
                }.forEach { config ->
                    target.addBuildTaskForTarget(digestBuffer, gradleRustDependencies, extension, config)
                    target.addRunTaskForTarget(digestBuffer, gradleRustDependencies, extension, config)
                }

            extension
                .examplesConfiguration
                .filterNot {
                    it.isExcluded
                }.forEach { config ->
                    target.addRunTaskForTarget(digestBuffer, gradleRustDependencies, extension, config)
                }

            extension
                .testsConfiguration
                .filterNot {
                    it.isExcluded
                }.forEach { config ->
                    target.addTestTaskForTarget(digestBuffer, gradleRustDependencies, extension, config)
                }

            extension
                .benchmarksConfiguration
                .filterNot {
                    it.isExcluded
                }.forEach { config ->
                    target.addBenchTaskForTarget(digestBuffer, gradleRustDependencies, extension, config)
                }

            if(extension.libraryConfiguration.path.isPresent) {
                var buildLibraryTask = "buildLibraryOnly" + addIfTest()
                buildLibraryTask += addIfConflictingTask(target, buildLibraryTask)
                tryRegisterTask {
                    val task = target.tasks.register(buildLibraryTask, CargoBuild::class.java)

                    task.configure {
                        dependsOn("generateCargoManifest")
                        group = "build"
                        description = "Builds the library only"
                        this.ext = extension
                        setDefaultProperties()
                        setTargettedProperties()
                        setBuildProperties()
                        this.lib.set(true)

                        inputs.file(manifestPath)
                    }

                    return@tryRegisterTask task.get()
                }
                var buildLibraryReleaseTask = "buildLibraryOnlyRelease" + addIfTest()
                buildLibraryReleaseTask += addIfConflictingTask(target, buildLibraryReleaseTask)
                tryRegisterTask {
                    val task = target.tasks.register(buildLibraryReleaseTask, CargoBuild::class.java)

                    task.configure {
                        dependsOn("generateCargoManifest")
                        group = "build"
                        description = "Builds the library only as release"
                        this.ext = extension
                        setDefaultProperties()
                        setTargettedProperties()
                        setBuildProperties()
                        this.lib.set(true)
                        this.release.set(true)

                        inputs.file(manifestPath)
                    }

                    return@tryRegisterTask task.get()
                }
            }

            target.tasks.configureEach {
                if(this is CargoDefaultTask) {
                    dependsOn.add("checkCargoExists")
                }
            }
        }
    }

    private fun Project.addBuildTaskForTarget(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration
    ) {
        //Dev
        configureAsIndependentBuild(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            false
        )
        //Release
        configureAsIndependentBuild(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            true
        )
    }

    private fun Project.addRunTaskForTarget(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration
    ) {
        //Dev
        configureAsIndependentRun(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            false
        )
        //Release
        configureAsIndependentRun(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            true
        )
    }

    private fun Project.addBenchTaskForTarget(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration
    ) {
        //Dev
        configureAsIndependentBench(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            false
        )
    }

    private fun Project.addTestTaskForTarget(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration
    ) {
        //Dev
        configureAsIndependentTest(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            false
        )
        //Release
        configureAsIndependentTest(
            buffer,
            gradleRustDependencies,
            extension,
            configuration,
            true
        )
    }

    private fun Project.configureAsIndependentBuild(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration,
        release: Boolean
    ) {
        tryRegisterTask {
            val task = this.tasks.register(
                "build" + configuration.name.uppercaseFirstChar() + if(release) "Release" else "Dev",
                CargoBuild::class.java
            )

            task.configure {
                dependsOn("generateCargoManifest")
                group = "build"
                description = "Build '${configuration.actualName}' Binary as " + if(release) "release." else "dev."

                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()

                this.release.set(release)
                this.features.addAll(configuration.buildFeatures.get())

                //Get off the Global properties
                this.bin.set(mutableListOf())

                this.bin.add(configuration.actualName)

                inputs.file(manifestPath)

                doNotTrackState("Build task must run on demand")
            }

            return@tryRegisterTask task.get()
        } as CargoBuild
    }

    private fun Project.configureAsIndependentRun(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration,
        release: Boolean
    ) {
        tryRegisterTask {
            val task = this.tasks.register(
                "run" + configuration.name.uppercaseFirstChar() + if(release) "Release" else "Dev",
                CargoRun::class.java
            )

            task.configure {
                dependsOn("generateCargoManifest")
                group = "application"
                description = "Run '${configuration.actualName}' Binary as " + if(release) "release." else "dev."

                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                setRunProperties(configuration)

                this.release.set(release)
                this.features.addAll(configuration.buildFeatures.get())

                //Get off the Global properties
                this.bin.set(mutableListOf())

                this.bin.add(configuration.actualName)

                inputs.file(manifestPath)

                doNotTrackState("Run task must run on demand")
            }

            return@tryRegisterTask task.get()
        } as CargoRun
    }

    private fun Project.configureAsIndependentBench(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration,
        release: Boolean
    ) {
        tryRegisterTask {
            val task = this.tasks.register(
                "bench" + configuration.name.uppercaseFirstChar(),
                CargoBench::class.java
            )

            task.configure {
                dependsOn("generateCargoManifest")
                group = "verification"
                description = "Benchmark '${configuration.actualName}'"

                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                setRunProperties(configuration)
                setBenchProperties()

                this.release.set(release)
                this.features.addAll(configuration.buildFeatures.get())

                //Get off the Global properties
                this.bin.set(mutableListOf())
                this.test.set(mutableListOf())
                this.bench.set(mutableListOf())

                this.bench.add(configuration.actualName)

                inputs.file(manifestPath)

                doNotTrackState("Bench task must run on demand")
            }

            return@tryRegisterTask task.get()
        } as CargoBench
    }

    private fun Project.configureAsIndependentTest(
        buffer: ByteArray,
        gradleRustDependencies: List<RustCrateOptions>?,
        extension: RustExtension,
        configuration: BinaryConfiguration,
        release: Boolean
    ) {
        tryRegisterTask {
            val task = this.tasks.register(
                "test" + configuration.name.uppercaseFirstChar() + if(release) "Release" else "Dev",
                CargoTest::class.java
            )

            task.configure {
                dependsOn("generateCargoManifest")
                group = "verification"
                description = "Test '${configuration.actualName}' as " + if(release) "release." else "dev."

                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                setRunProperties(configuration)
                setBenchProperties()
                setTestProperties()

                this.release.set(release)
                this.features.addAll(configuration.buildFeatures.get())

                //Get off the Global properties
                this.bin.set(mutableListOf())
                this.test.set(mutableListOf())
                this.bench.set(mutableListOf())

                this.test.add(configuration.actualName)

                inputs.file(manifestPath)

                doNotTrackState("Test task must run on demand")
            }

            return@tryRegisterTask task.get()
        } as CargoTest
    }

    //TODO: Switch to xxHash
    private fun hashSourceFiles(buffer: ByteArray, dependencies: List<RustCrateOptions>?, manifestPath: RegularFileProperty): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")

        dependencies?.forEach { crate ->
            val crateManifestDir = manifestPath
                .get()
                .asFile
                .toPath()
                .resolveSibling(crate.path.get())
                .normalize()

            val libraryPath = crateManifestDir.resolve("Cargo.toml")
                .toFile()
                .readTomlStringFields("lib", listOf("path"))["path"]?: return ByteArray(0)

            val libraryRustSourceDir = crateManifestDir
                .resolve(libraryPath)
                .normalize()
                .toFile()
                .parentFile

            libraryRustSourceDir
                .walkTopDown()
                .filter { it.isFile && it.extension == "rs" }
                .toSet()
                .forEach {
                    it.inputStream().use { input ->
                        var bytesRead = input.read(buffer)
                        while (bytesRead > 0) {
                            digest.update(buffer, 0, bytesRead)
                            bytesRead = input.read(buffer)
                        }
                    }
                }
        }

        return digest.digest()
    }

    private fun autoResolveLibrary(project: Project, extension: RustExtension) {
        val libraryDirectory = project
            .layout
            .projectDirectory
            .dir("src")
            .dir("library")
            .dir("rust")

        val potentialLibraryFile = libraryDirectory.file("lib.rs")

        if(potentialLibraryFile.asFile.exists()) {
            extension.libraryConfiguration.path.set(potentialLibraryFile)
        }
    }

    private fun resolveMainRustFiles(layout: ProjectLayout, sourceDir: String): List<File> {
        val mainDirectory = layout
            .projectDirectory
            .dir("src")
            .dir(sourceDir)
            .dir("rust")

        return mainDirectory
            .asFile
            .listFiles()
            .orEmpty()
            .filter { file -> file.extension == "rs" || (file.isDirectory && file.resolve("main.rs").isFile) }
    }

    private fun autoResolveBinaries(project: Project, extension: RustExtension) {
        resolveMainRustFiles(project.layout, "main").forEach { file ->
            extension.binariesConfiguration.register(file.nameWithoutExtension.lowercase()) {
                path.set(file)
            }
        }
    }

    private fun autoResolveTests(project: Project, extension: RustExtension) {
        resolveMainRustFiles(project.layout, "test").forEach { file ->
            extension.testsConfiguration.register(file.nameWithoutExtension.lowercase()) {
                path.set(file)
            }
        }
    }

    private fun autoResolveBenchmarks(project: Project, extension: RustExtension) {
        resolveMainRustFiles(project.layout, "bench").forEach { file ->
            extension.benchmarksConfiguration.register(file.nameWithoutExtension.lowercase()) {
                path.set(file)
            }
        }
    }

    private fun autoResolveExamples(project: Project, extension: RustExtension) {
        resolveMainRustFiles(project.layout, "example").forEach { file ->
            extension.examplesConfiguration.register(file.nameWithoutExtension.lowercase()) {
                path.set(file)
            }
        }
    }

    private fun addIfTest(): String {
        return if(IS_TEST_ENVIRONMENT) "NeoRust" else ""
    }

    private fun addIfConflictingTask(project: Project, taskName: String): String {
        return if(project.tasks.findByName(taskName) != null) "Rust" else ""
    }

    private fun modifyIfConflictingConfig(project: Project, configName: String): String {
        return if(project.configurations.findByName(configName) != null)
            "rust" + configName.uppercaseFirstChar()
        else
            configName
    }

    private fun tryCreateConfig(block: () -> Configuration): Configuration {
        try {
            return block.invoke()
        } catch(e: InvalidUserDataException) {
            throw IllegalStateException(
                "Plugin did not expect a conflicting config name! Is there another Rust Gradle Plugin?"
            )
        }
    }

    private fun tryRegisterTask(block: () -> Task): Task {
        try {
            return block.invoke()
        } catch(e: InvalidUserDataException) {
            val name = e.message?.substringAfter('\'')?.substringBefore('\'')
            throw IllegalStateException(
                "Plugin did not expect a conflicting task name '$name'! Is there another Rust Gradle Plugin?"
            )
        }
    }

    companion object {
        val IS_TEST_ENVIRONMENT = System.getenv("NEORUST_TESTING") != null
        val IS_DEBUG_ENVIRONMENT = System.getenv("NEORUST_DEBUGGING") != null
    }
}