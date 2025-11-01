package asia.hombre.neorust.task

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Tests this project (if available)
 *
 * This runs `cargo test`
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class CargoTest @Inject constructor(): CargoBench() {
    @get:Input
    @get:Optional
    abstract val testThreads: Property<Int>

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            remove("bench")
            add("test")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(testThreads.isPresent)
            args.add("--test-threads=${testThreads.get()}")

        return args
    }
}