package asia.hombre.neorust.internal

import asia.hombre.neorust.task.CargoClean
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * A Cargo task with a binary target
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class CargoTargettedTask @Inject constructor(): CargoDefaultTask() {
    @get:Input
    @get:Optional
    abstract val lib: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val bin: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val bins: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val example: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val examples: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val test: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val tests: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val bench: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val benches: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val allTargets: Property<Boolean>

    override fun compileArgs(): List<String> {
        val args = super.compileArgs() as MutableList<String>

        if(lib.getOrElse(false))
            args.add("--lib")

        bin.apply {
            if(isPresent && get().isNotEmpty() && this@CargoTargettedTask !is CargoClean) {
                args.add("--bin")
                args.addAll(get())
            }
        }

        if(bins.getOrElse(false))
            args.add("--bins")

        example.apply {
            if(isPresent && get().isNotEmpty()) {
                args.add("--example")
                args.addAll(get())
            }
        }

        if(examples.getOrElse(false))
            args.add("--examples")

        test.apply {
            if(isPresent && get().isNotEmpty()) {
                args.add("--test")
                args.addAll(get())
            }
        }

        if(tests.getOrElse(false))
            args.add("--tests")

        bench.apply {
            if(isPresent && get().isNotEmpty()) {
                args.add("--bench")
                args.addAll(get())
            }
        }

        if(benches.getOrElse(false))
            args.add("--benches")

        if(allTargets.getOrElse(false))
            args.add("--all-targets")

        return args
    }
}