package asia.hombre.neorust

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import java.nio.file.Paths

class Rust: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("rust", RustExtension::class.java)
        val isTestEnvironment = System.getenv("NEORUST_TESTING") != null

        if(isTestEnvironment)
            println("Test environment detected!\nTasks are now explicitly named.")

        var cleanTask = "clean" + addIfTest(isTestEnvironment)
        cleanTask += addIfConflicting(target, cleanTask)
        target.tasks.register(cleanTask, Exec::class.java) {
            apply {
                group = "build"

                commandLine("cargo", "clean")

                errorOutput = System.out
            }
        }
        var benchTask = "bench" + addIfTest(isTestEnvironment)
        benchTask += addIfConflicting(target, benchTask)
        target.tasks.register(benchTask, CargoBench::class.java) {
            apply {
                group = "build"
            }
        }
        var buildTask = "build" + addIfTest(isTestEnvironment)
        buildTask += addIfConflicting(target, buildTask)
        target.tasks.register(buildTask, CargoBuild::class.java) {
            apply {
                group = "build"
            }
        }
        var publishTask = "publish" + addIfTest(isTestEnvironment)
        publishTask += addIfConflicting(target, publishTask)
        target.tasks.register(publishTask, CargoPublish::class.java) {
            apply {
                group = "publishing"
            }
        }
        var testTask = "test" + addIfTest(isTestEnvironment)
        testTask += addIfConflicting(target, testTask)
        target.tasks.register(testTask, CargoTest::class.java) {
            apply {
                group = "verification"
            }
        }
    }

    private fun addIfTest(isTestEnvironment : Boolean): String {
        return if(isTestEnvironment) "NeoRust" else ""
    }

    private fun addIfConflicting(project: Project, taskName: String): String {
        return if(project.tasks.findByName(taskName) != null) "Rust" else ""
    }

    companion object {
        @JvmStatic
        val DEFAULT_TARGET_DIR = Paths.get("./build/target")
    }
}