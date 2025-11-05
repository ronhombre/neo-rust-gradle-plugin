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
import asia.hombre.neorust.task.RunBinary
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import setBenchProperties
import setBuildProperties
import setDefaultProperties
import setPublishProperties
import setTargettedProperties
import setTestProperties

@Suppress("unused")
class Rust: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("rust", RustExtension::class.java)
        val crateLibrary = CrateLibrary(target.name, target.objects)
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

        tryRegisterTask {
            target.tasks.register("generateCargoManifest", CargoManifestGenerate::class.java) {
                group = "build"
                this.ext = extension
                setDefaultProperties()

                rustManifestOptions.set(extension.rustManifestOptions)
                rustProfileOptions.set(extension.rustProfileOptions)
                rustBinaryOptions.set(extension.rustBinaryOptions)
                rustFeaturesOptions.set(extension.rustFeaturesOptions)
                this.crateLibrary.set(crateLibrary)
                featuresList.set(extension.featuresList)
            }.get()
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
                this.bin.addAll(target.provider<Iterable<String>> {
                    extension.rustBinaryOptions.list.map { it.name.get() }.toSet().asIterable()
                })

                inputs.file(manifestPath)
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
            extension.rustManifestOptions.packageConfig.name.convention(project.name)
            extension.rustManifestOptions.packageConfig.version.convention(project.version.toString())
            extension.rustManifestOptions.packageConfig.description.convention(project.description)

            extension.rustBinaryOptions.list.forEach { binary ->
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

                        inputs.file(manifestPath)
                    }.get()
                } as CargoBuild
                tryRegisterTask {
                    target.tasks.register(runTask, RunBinary::class.java) {
                        dependsOn(binaryBuildTask)
                        group = "run"
                        description = "Execute binary '$lowercaseBinaryName' using the profile '$lowercaseProfile'"

                        this.targetDirectory.set(extension.targetDirectory)
                        this.manifestPath.set(extension.manifestPath)
                        this.binaryName.set(lowercaseBinaryName)
                        this.buildProfile.set(
                            if(cargoBuildTask.release.isPresent && cargoBuildTask.release.get())
                                "release"
                            else
                                "debug"
                        )
                        this.arguments.set(binary.arguments.get())
                        this.environment.set(binary.environment.get())

                        inputs.file(manifestPath)
                    }.get()
                }
            }

            if(extension.rustManifestOptions.libConfig.crateType.isPresent) {
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