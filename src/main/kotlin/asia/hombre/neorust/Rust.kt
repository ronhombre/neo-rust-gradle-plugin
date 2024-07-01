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
        target.tasks.register("clean", Exec::class.java) {
            it.apply {
                group = "build"

                commandLine("cargo", "clean")

                errorOutput = System.out
            }
        }
        target.tasks.register("bench", CargoBench::class.java) {
            it.apply {
                group = "build"
            }
        }
        target.tasks.register("build", CargoBuild::class.java) {
            it.apply {
                group = "build"
            }
        }
        target.tasks.register("publish", CargoPublish::class.java) {
            it.apply {
                group = "publishing"
            }
        }
        target.tasks.register("test", CargoTest::class.java) {
            it.apply {
                group = "verification"
            }
        }
    }

    companion object {
        @JvmStatic
        val DEFAULT_TARGET_DIR = Paths.get("./target")
    }
}