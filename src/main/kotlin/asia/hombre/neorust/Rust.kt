package asia.hombre.neorust

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.task.*
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

class Rust: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("rust", RustExtension::class.java)

        //Detect test environment
        if(IS_TEST_ENVIRONMENT)
            println("Warning: Test environment detected!\nConfigs and Tasks are now explicitly named.")

        //Configurations
        val implementationConfig =
            modifyIfConflictingConfig(target, "implementation") + addIfTest()
        tryCreateConfig {
            target.configurations.create(implementationConfig) {
                isVisible = true
                isTransitive = true
                description = "Rust dependencies"
            }
        }

        extension.implementationName = implementationConfig

        val devConfig =
            modifyIfConflictingConfig(target, "devOnly") + addIfTest()
        tryCreateConfig {
            target.configurations.create(devConfig) {
                isVisible = true
                isTransitive = true
                description = "Rust Developer dependencies"
            }
        }

        extension.devOnlyName = devConfig

        val buildConfig =
            modifyIfConflictingConfig(target, "buildOnly") + addIfTest()
        tryCreateConfig {
            target.configurations.create(buildConfig) {
                isVisible = true
                isTransitive = true
                description = "Rust Build dependencies"
            }
        }

        extension.buildOnlyName = buildConfig

        //Tasks
        tryRegisterTask {
            target.tasks.register("resolveRustDependencies", CargoResolver::class.java) {
                group = "build"
            }
        }

        tryRegisterTask {
            target.tasks.register("generateCargoManifest", CargoManifestGenerate::class.java) {
                dependsOn("resolveRustDependencies")
                group = "build"
            }
        }
        var cleanTask = "clean" + addIfTest()
        cleanTask += addIfConflictingTask(target, cleanTask)
        tryRegisterTask {
            target.tasks.register(cleanTask, Exec::class.java) {
                group = "build"

                commandLine("cargo", "clean")

                errorOutput = System.out
            }
        }

        var benchTask = "bench" + addIfTest()
        benchTask += addIfConflictingTask(target, benchTask)
        tryRegisterTask {
            target.tasks.register(benchTask, CargoBench::class.java) {
                group = "build"
            }
        }

        var buildTask = "build" + addIfTest()
        buildTask += addIfConflictingTask(target, buildTask)
        tryRegisterTask {
            target.tasks.register(buildTask, CargoBuild::class.java) {
                dependsOn("generateCargoManifest")
                group = "build"
            }
        }

        var publishTask = "publish" + addIfTest()
        publishTask += addIfConflictingTask(target, publishTask)
        tryRegisterTask {
            target.tasks.register(publishTask, CargoPublish::class.java) {
                group = "publishing"
            }
        }

        var testTask = "test" + addIfTest()
        testTask += addIfConflictingTask(target, testTask)
        tryRegisterTask {
            target.tasks.register(testTask, CargoTest::class.java) {
                group = "verification"
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

    private fun tryCreateConfig(block: () -> Unit) {
        try {
            block.invoke()
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