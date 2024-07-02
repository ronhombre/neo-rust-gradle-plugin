package asia.hombre.neorust.task

import asia.hombre.neorust.option.CargoMessageFormat
import asia.hombre.neorust.option.CargoTiming
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoTargettedTask
import asia.hombre.neorust.options.RustTargetOptions
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Compile the current package
 *
 * This runs `cargo build`
 */
open class CargoBuild: CargoTargettedTask() {
    private val buildOptions = project.extensions.getByType(RustExtension::class.java).rustBuildOptions

    @get:Input
    var workspace: Boolean? = null
        get() = field?: buildOptions.workspace

    @get:Input
    var exclude: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { buildOptions.exclude }

    @get:Input
    var release: Boolean? = null
        get() = field?: buildOptions.release

    @get:Input
    var profile: String = ""
        get() = field.ifBlank { buildOptions.profile }

    @get:Input
    var timings: CargoTiming = CargoTiming.none
        get() = field.takeIf { it != CargoTiming.none }?: buildOptions.timings

    @get:Input
    var messageFormat: MutableList<CargoMessageFormat> = mutableListOf()
        get() = field.ifEmpty { buildOptions.messageFormat }

    @get:Input
    var buildPlan: Boolean? = null
        get() = field?: buildOptions.buildPlan

    @get:Input
    var futureIncompatReport: Boolean? = null
        get() = field?: buildOptions.futureIncompatReport

    @TaskAction
    fun build() {
        project.exec {
            apply {
                val args = mutableListOf("cargo", "build")

                args.addAll(compileArgs())

                commandLine = args
            }
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(workspace!!)
            args.add("--workspace")

        exclude.forEach { excluded ->
            args.addAll(listOf("--exclude", excluded))
        }

        if(release!!)
            args.add("--release")

        if(profile.isNotBlank())
            args.addAll(listOf("--profile", profile))

        if(timings != CargoTiming.none)
            when(timings) {
                CargoTiming.none -> {}
                CargoTiming.default -> args.add("--timings")
                CargoTiming.html -> args.add("--timings=html")
                CargoTiming.json -> args.add("--timings=json")
                CargoTiming.html_and_json -> args.add("--timings=html,json")
            }

        messageFormat.forEach { format ->
            when(format) {
                CargoMessageFormat.human -> {
                    if(messageFormat.contains(CargoMessageFormat.short) ||
                        messageFormat.contains(CargoMessageFormat.json) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
                CargoMessageFormat.short -> {
                    if(messageFormat.contains(CargoMessageFormat.human) ||
                        messageFormat.contains(CargoMessageFormat.json) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
                CargoMessageFormat.json,
                CargoMessageFormat.json_diagnostic_short,
                CargoMessageFormat.json_diagnostic_rendered_ansi,
                CargoMessageFormat.json_render_diagnostics -> {
                    if(messageFormat.contains(CargoMessageFormat.human) ||
                        messageFormat.contains(CargoMessageFormat.json) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
            }
        }

        if(messageFormat.isNotEmpty())
            args.addAll(listOf("--message-format", messageFormat.joinToString(",") { format -> format.name }))

        if(buildPlan!!)
            args.add("--build-plan")

        if(futureIncompatReport!!)
            args.add("--future-incompat-report")

        return args
    }

    override fun getTargetOptions(): RustTargetOptions {
        return buildOptions
    }
}