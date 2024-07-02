package asia.hombre.neorust.task

import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Execute benchmarks of a package
 *
 * This runs `cargo bench`
 */
open class CargoPublish: CargoDefaultTask() {
    private val publishOptions = project.extensions.getByType(RustExtension::class.java).rustPublishOptions

    @get:Input
    var dryRun: Boolean? = null
        get() = field?: publishOptions.dryRun

    @get:Input
    var noVerify: Boolean? = null
        get() = field?: publishOptions.noVerify

    @get:Input
    var allowDirty: Boolean? = null
        get() = field?: publishOptions.allowDirty

    @get:Input
    var token: String = ""
        get() = field.ifBlank { publishOptions.token }

    @get:Input
    var index: String = ""
        get() = field.ifBlank { publishOptions.index }

    @get:Input
    var registry: String = ""
        get() = field.ifBlank { publishOptions.registry }

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("publish")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(dryRun!!)
            args.add("--dry-run")

        if(noVerify!!)
            args.add("--no-verify")

        if(allowDirty!!)
            args.add("--allow-dirty")

        if(token.isBlank())
            throw IllegalArgumentException("Token is unspecified explicitly or through Environment Variables!")
        else
            args.addAll(listOf("--token", token))

        if(index.isNotBlank())
            args.addAll(listOf("--index", index))

        if(registry.isNotBlank())
            args.addAll(listOf("--registry", registry))

        return args
    }
}