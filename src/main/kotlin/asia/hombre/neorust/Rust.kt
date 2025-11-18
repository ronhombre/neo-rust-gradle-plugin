package asia.hombre.neorust

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import asia.hombre.neorust.option.BuildProfile
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoCheck
import asia.hombre.neorust.task.CargoClean
import asia.hombre.neorust.task.CargoManifestGenerate
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest
import asia.hombre.neorust.task.ResolveCrates
import asia.hombre.neorust.task.RunBinary
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import readRustCrateFromFile
import readTomlStringFields
import setBenchProperties
import setBuildProperties
import setDefaultProperties
import setPublishProperties
import setTargettedProperties
import setTestProperties
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
                rustManifestOptions.set(extension.rustManifestOptions)
                rustProfileOptions.set(extension.rustProfileOptions)
                rustBinaryOptions.set(extension.rustBinaryOptions)
                rustFeaturesOptions.set(extension.rustFeaturesOptions)
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

        /*tryRegisterTask {
            target.tasks.register("generateCargoConfig", CargoConfigGenerate::class.java) {
                group = "build"
                this.ext = extension
                setDefaultProperties()
            }
        }*/
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
            }.get()
        }

        var benchTask = "bench" + addIfTest()
        benchTask += addIfConflictingTask(target, benchTask)
        tryRegisterTask {
            target.tasks.register(benchTask, CargoBench::class.java) {
                dependsOn("generateCargoManifest")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBenchProperties()

                inputs.file(manifestPath)
            }.get()
        }

        var buildTask = "build" + addIfTest()
        buildTask += addIfConflictingTask(target, buildTask)
        tryRegisterTask {
            target.tasks.register(buildTask, CargoBuild::class.java) {
                dependsOn("generateCargoManifest")
                group = "build"
                description = "Builds all the binaries using the default profile"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                this.bin.addAll(target.provider {
                    extension.rustBinaryOptions.list.get().map { it.name.get() }.toSet().asIterable()
                })

                inputs.file(manifestPath)
                outputs.dir(targetDirectory.get())
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
            }.get()
        }

        target.afterEvaluate {
            val packageConfig = extension.rustManifestOptions.packageConfig.get()
            packageConfig.name.convention(project.name)
            packageConfig.version.convention(project.version.toString())
            packageConfig.description.convention(project.description)

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
                logger.error("Corrupted resolved Rust crate detected. Please re-run task `resolveRustCrates` or do `clean` first.", e)
                null
            } catch (e: Exception) {
                logger.error("Unknown error encountered.", e)
                null
            }
            val digestBuffer = ByteArray(DEFAULT_BUFFER_SIZE)

            extension.rustBinaryOptions.list.get().forEach { binary ->
                val lowercaseBinaryName = binary.name.get().lowercase()
                val buildProfile = binary.buildProfile.get()
                val lowercaseProfile = buildProfile.name.lowercase()
                val taskNameSuffix = if(buildProfile == BuildProfile.DEFAULT) "" else lowercaseProfile.uppercaseFirstChar()
                val binaryBuildTask = "build" + lowercaseBinaryName.uppercaseFirstChar() + taskNameSuffix
                val runTask = "run" + lowercaseBinaryName.uppercaseFirstChar() + taskNameSuffix
                val cargoBuildTask = tryRegisterTask {
                    target.tasks.register(binaryBuildTask, CargoBuild::class.java) {
                        dependsOn("generateCargoManifest")
                        group = "build"
                        description = "Build '$lowercaseBinaryName' using the global build profile"

                        this.ext = extension
                        setDefaultProperties()
                        setTargettedProperties()
                        setBuildProperties()

                        when(buildProfile) {
                            BuildProfile.DEFAULT -> logger.debug("Using the default profile for $binaryBuildTask")
                            BuildProfile.DEV -> this.release.set(false)
                            BuildProfile.RELEASE -> this.release.set(true)
                        }
                        this.bin.set(mutableListOf()) //Get off the Global property
                        this.bin.add(binary.name.get())

                        inputs.dir(binary.path.get().asFile.parentFile.also { it.mkdirs() })

                        //TODO: Switch to xxHash
                        //Calculate hash
                        inputs.property("files-hash",
                            let {
                                val digest = MessageDigest.getInstance("SHA-256")

                                gradleRustDependencies?.forEach { crate ->
                                    val crateManifestDir = manifestPath
                                        .get()
                                        .asFile
                                        .toPath()
                                        .resolveSibling(crate.path.get())
                                        .normalize()

                                    val libraryPath = crateManifestDir.resolve("Cargo.toml")
                                        .toFile()
                                        .readTomlStringFields("lib", listOf("path"))["path"]?:
                                    throw IllegalArgumentException(
                                        "Crate ${binary.name.get()} is not a library because the \"path\" value cannot be found."
                                    )

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
                                                var bytesRead = input.read(digestBuffer)
                                                while (bytesRead > 0) {
                                                    digest.update(digestBuffer, 0, bytesRead)
                                                    bytesRead = input.read(digestBuffer)
                                                }
                                            }
                                        }
                                }

                                digest.digest()
                            }
                        )
                        outputs.dir(outputTargetDirectory)
                    }.get()
                } as CargoBuild
                tryRegisterTask {
                    target.tasks.register(runTask, RunBinary::class.java) {
                        dependsOn(binaryBuildTask)
                        group = "run"
                        description = "Execute binary '$lowercaseBinaryName' using the profile '$lowercaseProfile'"

                        this.targetDirectory.set(cargoBuildTask.outputTargetDirectory)
                        this.manifestPath.set(cargoBuildTask.manifestPath)
                        this.binaryName.set(lowercaseBinaryName)
                        this.buildProfile.set(
                            if(cargoBuildTask.release.isPresent && cargoBuildTask.release.get())
                                "release"
                            else
                                "debug"
                        )
                        this.arguments.set(binary.arguments.get())
                        this.environment.set(binary.environment.get())
                    }.get()
                }
            }

            if(extension.rustManifestOptions.libConfig.get().crateType.isPresent) {
                var buildLibraryTask = "buildLibraryOnly" + addIfTest()
                buildLibraryTask += addIfConflictingTask(target, buildLibraryTask)
                tryRegisterTask {
                    target.tasks.register(buildLibraryTask, CargoBuild::class.java) {
                        dependsOn("generateCargoManifest")
                        group = "build"
                        description = "Builds the library only"
                        this.ext = extension
                        setDefaultProperties()
                        setTargettedProperties()
                        setBuildProperties()
                        this.lib.set(true)

                        inputs.file(manifestPath)
                    }.get()
                }
                var buildLibraryReleaseTask = "buildLibraryOnlyRelease" + addIfTest()
                buildLibraryReleaseTask += addIfConflictingTask(target, buildLibraryReleaseTask)
                tryRegisterTask {
                    target.tasks.register(buildLibraryReleaseTask, CargoBuild::class.java) {
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
                    }.get()
                }
            }

            target.tasks.configureEach {
                if(this is CargoDefaultTask) {
                    dependsOn.add("checkCargoExists")
                }
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
            throw IllegalStateException(
                "Plugin did not expect a conflicting task name! Is there another Rust Gradle Plugin?"
            )
        }
    }

    companion object {
        val IS_TEST_ENVIRONMENT = System.getenv("NEORUST_TESTING") != null
        val IS_DEBUG_ENVIRONMENT = System.getenv("NEORUST_DEBUGGING") != null
    }
}