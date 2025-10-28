package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoTargettedTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Execute benchmarks of a package
 *
 * This runs `cargo bench`
 */
abstract class CargoBench @Inject constructor(): CargoTargettedTask() {
    @get:Input
    @get:Optional
    abstract val noRun: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val noCapture: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val noFailFast: Property<Boolean>

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("bench")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(noRun.getOrElse(false))
            args.add("--no-run")

        if(noCapture.getOrElse(false))
            args.add("--nocapture")

        if(noFailFast.getOrElse(false))
            args.add("--no-fail-fast")

        return args
    }
}