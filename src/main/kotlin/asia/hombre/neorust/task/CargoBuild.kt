package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoTargettedTask
import asia.hombre.neorust.option.CargoMessageFormat
import asia.hombre.neorust.option.CargoTiming
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import javax.inject.Inject

/**
 * Builds all or a specific binary
 *
 * This runs `cargo build`
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class CargoBuild @Inject constructor(releaseBuild: Boolean): CargoTargettedTask() {
    @get:Input
    @get:Optional
    abstract val workspace: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val exclude: ListProperty<String>
    @get:Input
    @get:Optional
    abstract val release: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val profile: Property<String>
    @get:Input
    @get:Optional
    abstract val timings: Property<CargoTiming>
    @get:Input
    @get:Optional
    abstract val messageFormat: ListProperty<CargoMessageFormat>
    @get:Input
    @get:Optional
    abstract val buildPlan: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val buildAll: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val futureIncompatReport: Property<Boolean>

    @get:OutputDirectory
    @get:Optional
    abstract val outputTargetDirectory: DirectoryProperty

    init {
        outputTargetDirectory.convention(
            if(releaseBuild) {
                targetDirectory.dir("release")
            } else {
                targetDirectory.dir("debug")
            }
        )
    }

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("build")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(workspace.getOrElse(false))
            args.add("--workspace")

        if(exclude.isPresent) exclude.get().forEach { excluded ->
            args.addAll(listOf("--exclude", excluded))
        }

        if(release.getOrElse(false))
            args.add("--release")

        profile.apply {
            if(isPresent && get().isNotBlank())
                args.addAll(listOf("--profile", get()))
        }

        when(timings.getOrElse(CargoTiming.none)) {
            CargoTiming.none -> {}
            CargoTiming.default -> args.add("--timings")
            CargoTiming.html -> args.add("--timings=html")
            CargoTiming.json -> args.add("--timings=json")
            CargoTiming.html_and_json -> args.add("--timings=html,json")
        }

        if(messageFormat.isPresent) messageFormat.get().forEach { format ->
            when(format) {
                CargoMessageFormat.human -> {
                    if(messageFormat.get().contains(CargoMessageFormat.short) ||
                        messageFormat.get().contains(CargoMessageFormat.json) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.get().contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
                CargoMessageFormat.short -> {
                    if(messageFormat.get().contains(CargoMessageFormat.human) ||
                        messageFormat.get().contains(CargoMessageFormat.json) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.get().contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
                CargoMessageFormat.json,
                CargoMessageFormat.json_diagnostic_short,
                CargoMessageFormat.json_diagnostic_rendered_ansi,
                CargoMessageFormat.json_render_diagnostics -> {
                    if(messageFormat.get().contains(CargoMessageFormat.human) ||
                        messageFormat.get().contains(CargoMessageFormat.json) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_short) ||
                        messageFormat.get().contains(CargoMessageFormat.json_diagnostic_rendered_ansi) ||
                        messageFormat.get().contains(CargoMessageFormat.json_render_diagnostics))
                        throw IllegalArgumentException("The format '" + format.name + "' conflicts with another format!")
                }
            }
        }

        messageFormat.apply {
            if(isPresent && get().isNotEmpty())
                args.addAll(listOf("--message-format", get().joinToString(",") { format -> format.name }))
        }

        if(buildPlan.getOrElse(false))
            args.add("--build-plan")

        if(futureIncompatReport.getOrElse(false))
            args.add("--future-incompat-report")

        return args
    }
}