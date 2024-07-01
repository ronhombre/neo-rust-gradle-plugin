package asia.hombre.neorust.task

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.options.RustTargetOptions
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Execute benchmarks of a package
 *
 * This runs `cargo bench`
 */
class CargoTest: CargoBench() {
    private val testOptions = project.extensions.getByType(RustExtension::class.java).rustTestOptions

    @get:Input
    var testThreads: Int = 0
        get() = testOptions.testThreads.takeIf { it != 0 } ?: field

    @TaskAction
    fun test() {
        project.exec {
            it.apply {
                val args = mutableListOf("cargo", "test")

                args.addAll(compileArgs())

                commandLine = args
            }
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(testThreads > 0)
            args.add("--test-threads=$testThreads")

        return args
    }

    override fun getTargetOptions(): RustTargetOptions {
        return testOptions
    }
}