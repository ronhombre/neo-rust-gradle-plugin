package asia.hombre.neorust

import asia.hombre.neorust.extension.CrateExtension
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.task.*
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Exec
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

        //Configurations
        /*val implementationName =
            modifyIfConflictingConfig(target, "implementation") + addIfTest()
        tryCreateConfig {
            target.configurations.create(implementationName) {
                isVisible = false
                isTransitive = true
                description = "Rust dependencies"
            }
        }

        extension.implementationName = implementationName

        val devOnlyName =
            modifyIfConflictingConfig(target, "devOnly") + addIfTest()
        tryCreateConfig {
            target.configurations.create(devOnlyName) {
                isVisible = false
                isTransitive = true
                description = "Rust Developer dependencies"
            }
        }

        extension.devOnlyName = devOnlyName

        val buildOnlyName =
            modifyIfConflictingConfig(target, "buildOnly") + addIfTest()
        tryCreateConfig {
            target.configurations.create(buildOnlyName) {
                isVisible = false
                isTransitive = true
                description = "Rust Build dependencies"
            }
        }

        extension.buildOnlyName = buildOnlyName*/

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
            }
        }
        /*tryRegisterTask {
            target.tasks.register("resolveRustDependencies", CargoResolver::class.java) {
                dependsOn("findCargo")

                group = "build"

                this.rustExtension.set(extension)
                this.crateExtension.set(crateExtension)
            }
        }*/

        tryRegisterTask {
            target.tasks.register("generateCargoManifest", CargoManifestGenerate::class.java) {
                group = "build"
                this.ext = extension
                setDefaultProperties()

                extension.rustManifestOptions.packageConfig.name.convention(project.name)
                extension.rustManifestOptions.packageConfig.version.convention(project.version.toString())
                extension.rustManifestOptions.packageConfig.description.convention(project.description)

                rustManifestOptions.set(extension.rustManifestOptions)
                rustBinaryOptions.set(extension.rustBinaryOptions)
                this.crateExtension.set(crateExtension)
                featuresList.set(extension.featuresList)
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
                dependsOn("findCargo")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
            }
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
            }
        }

        var buildTask = "build" + addIfTest()
        buildTask += addIfConflictingTask(target, buildTask)
        tryRegisterTask {
            target.tasks.register(buildTask, CargoBuild::class.java) {
                dependsOn("generateCargoManifest", "findCargo")
                group = "build"
                this.ext = extension
                setDefaultProperties()
                setTargettedProperties()
                setBuildProperties()
                this.bin.set(extension.rustBinaryOptions.list)
            }
        }

        var runTask = "runBinary" + addIfTest()
        runTask += addIfConflictingTask(target, runTask)
        //TODO: Auto-generate for all available binary
        tryRegisterTask {
            target.tasks.register(runTask, Exec::class.java) {
                dependsOn(buildTask)
                group = "run"

                //TODO: Run built executable
            }
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
            }
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

    private fun tryRegisterTask(block: () -> Unit) {
        try {
            block.invoke()
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