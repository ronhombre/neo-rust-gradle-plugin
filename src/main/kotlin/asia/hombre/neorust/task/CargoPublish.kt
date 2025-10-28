package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoDefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Execute benchmarks of a package
 *
 * This runs `cargo bench`
 */
abstract class CargoPublish @Inject constructor(): CargoDefaultTask() {
    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val noVerify: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val allowDirty: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val token: Property<String>
    @get:Input
    @get:Optional
    abstract val index: Property<String>
    @get:Input
    @get:Optional
    abstract val registry: Property<String>

    init {
        dryRun.convention(false)
        noVerify.convention(false)
        allowDirty.convention(false)
        token.convention("")
        index.convention("")
        registry.convention("crates.io") //TODO: Verify this works
    }

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("publish")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(dryRun.getOrElse(false))
            args.add("--dry-run")

        if(noVerify.getOrElse(false))
            args.add("--no-verify")

        if(allowDirty.getOrElse(false))
            args.add("--allow-dirty")

        if(token.get().isBlank())
            throw IllegalArgumentException("Token is unspecified explicitly or through Environment Variables!")
        else
            args.addAll(listOf("--token", token.get()))

        if(index.get().isNotBlank())
            args.addAll(listOf("--index", index.get()))

        if(registry.get().isNotBlank())
            args.addAll(listOf("--registry", registry.get()))

        return args
    }
}