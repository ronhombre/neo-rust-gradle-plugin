package asia.hombre.neorust.task

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoTargettedTask
import asia.hombre.neorust.options.RustTargetOptions
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Execute benchmarks of a package
 *
 * This runs `cargo bench`
 */
open class CargoBench: CargoTargettedTask() {
    private val benchOptions = project.extensions.getByType(RustExtension::class.java).rustBenchOptions

    @get:Input
    var noRun: Boolean? = null
        get() = field?: benchOptions.noRun

    @get:Input
    var noCapture: Boolean? = null
        get() = field?: benchOptions.noCapture

    @get:Input
    var noFailFast: Boolean? = null
        get() = field?: benchOptions.noFailFast

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("bench")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(noRun!!)
            args.add("--no-run")

        if(noCapture!!)
            args.add("--nocapture")

        if(noFailFast!!)
            args.add("--no-fail-fast")

        return args
    }

    override fun getTargetOptions(): RustTargetOptions {
        return benchOptions
    }
}