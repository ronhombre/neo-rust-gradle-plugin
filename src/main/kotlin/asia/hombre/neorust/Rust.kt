package asia.hombre.neorust

import asia.hombre.neorust.extension.CrateExtension
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.option.BuildProfile
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
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
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.classpath.Instrumented.exec
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import setBenchProperties
import setBuildProperties
import setDefaultProperties
import setPublishProperties
import setTargettedProperties
import java.io.IOException

@Suppress("unused")
class Rust: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("rust", RustExtension::class.java)
        val crateExtension = CrateExtension(target.name, target.objects)
        target.dependencies.extensions.add("rustDependencies", crateExtension)

        //Detect test environment
        if(IS_TEST_ENVIRONMENT)
            println("Warning: Test environment detected!\nConfigs and Tasks are now explicitly named.")

        //Tasks
        tryRegisterTask {
            target.tasks.register("findCargo") {
                group = "build setup"

                doLast {
                    try {
                        val execResult = exec(Runtime.getRuntime(), "cargo version", "")

                        execResult.waitFor()

                        if (execResult.exitValue() != 0) {
                            val error = "Command failed with exit value: ${execResult.exitValue()}"
                            logger.error(error)
                            throw RuntimeException(error)
                        }
                    } catch (_: IOException) {
                        val error = "Cannot find cargo. Install it or set the path environment variable for your system"
                        logger.error(error)
                        throw RuntimeException(error)
                    }
                }
            }.get()
        }

        tryRegisterTask {
            target.tasks.register("generateCargoManifest", CargoManifestGenerate::class.java) {
                group = "build"
                this.ext = extension
                setDefaultProperties()

                rustManifestOptions.set(extension.rustManifestOptions)
                rustBinaryOptions.set(extension.rustBinaryOptions)
                rustFeaturesOptions.set(extension.rustFeaturesOptions)
                this.crateExtension.set(crateExtension)
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
                dependsOn("findCargo")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
            }.get()
        }

        var benchTask = "bench" + addIfTest()
        benchTask += addIfConflictingTask(target, benchTask)
        tryRegisterTask {
            target.tasks.register(benchTask, CargoBench::class.java) {
                dependsOn("generateCargoManifest", "findCargo")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBenchProperties()
            }.get()
        }

        var buildTask = "build" + addIfTest()
        buildTask += addIfConflictingTask(target, buildTask)
        tryRegisterTask {
            target.tasks.register(buildTask, CargoBuild::class.java) {
                dependsOn("generateCargoManifest", "findCargo")
                group = "build"
                description = "Builds all the binaries using the default profile"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                this.bin.set(extension.rustBinaryOptions.list)
            }.get()
        }

        var publishTask = "publish" + addIfTest()
        publishTask += addIfConflictingTask(target, publishTask)
        tryRegisterTask {
            target.tasks.register(publishTask, CargoPublish::class.java) {
                dependsOn("generateCargoManifest", "findCargo")
                group = "publishing"
                this.ext = extension
                setDefaultProperties()
                setPublishProperties()
            }.get()
        }

        var testTask = "test" + addIfTest()
        testTask += addIfConflictingTask(target, testTask)
        tryRegisterTask {
            target.tasks.register(testTask, CargoTest::class.java) {
                dependsOn("generateCargoManifest", "findCargo")
                group = "verification"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBenchProperties()
            }.get()
        }

        target.afterEvaluate {
            extension.rustManifestOptions.packageConfig.name.convention(project.name)
            extension.rustManifestOptions.packageConfig.version.convention(project.version.toString())
            extension.rustManifestOptions.packageConfig.description.convention(project.description)

            extension.rustBinaryOptions.list.forEach { bin ->
                val lowercaseBinaryName = bin.name.get().lowercase()
                val buildProfile = bin.buildProfile.get()
                val lowercaseProfile = buildProfile.name.lowercase()
                val taskNameSuffix = if(buildProfile == BuildProfile.DEFAULT) "" else lowercaseProfile.capitalized()
                val binaryBuildTask = "build" + lowercaseBinaryName.capitalized() + taskNameSuffix
                val runTask = "run" + lowercaseBinaryName.capitalized() + taskNameSuffix
                val cargoBuildTask = tryRegisterTask {
                    target.tasks.register(binaryBuildTask, CargoBuild::class.java) {
                        dependsOn("generateCargoManifest", "findCargo")
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
                        this.bin.add(bin)
                    }.get()
                } as CargoBuild
                tryRegisterTask {
                    target.tasks.register(runTask, RunBinary::class.java) {
                        dependsOn(binaryBuildTask)
                        group = "run"
                        description = "Execute binary '$lowercaseBinaryName' using the profile '$lowercaseProfile'"

                        this.targetDirectory.set(extension.targetDirectory)
                        this.manifestDirectory.set(extension.manifestPath)
                        this.binaryName.set(lowercaseBinaryName)
                        this.buildProfile.set(
                            if(cargoBuildTask.release.isPresent && cargoBuildTask.release.get())
                                "release"
                            else
                                "debug"
                        )
                        this.arguments.set(bin.arguments.get())
                        this.environment.set(bin.environment.get())
                    }.get()
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