package asia.hombre.neorust.task

import isMac
import isUnix
import isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject
import kotlin.io.path.Path

/**
 * Runs a binary executable built by Cargo
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
abstract class RunBinary @Inject constructor(): DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    @get:Optional
    abstract val targetDirectory: Property<String>
    @get:Input
    abstract val manifestDirectory: Property<String>
    @get:Input
    abstract val binaryName: Property<String>
    @get:Input
    abstract val buildProfile: Property<String>
    @get:Input
    abstract val arguments: ListProperty<String>
    @get:Input
    abstract val environment: MapProperty<String, String>

    @TaskAction
    fun execute() {
        val path = if(targetDirectory.isPresent) {
            Path(targetDirectory.get())
        } else {
            Path(manifestDirectory.get()).parent.resolve("target")
        }.resolve(buildProfile.get())

        val folder = path.toFile()

        val wasFolderCreated = folder.mkdirs()

        if(!folder.isDirectory && !wasFolderCreated) {
            logger.error("$path is not a directory or cannot be created.")

            throw IllegalStateException("$path is not a directory or cannot be created.")
        }

        val executableFileName: String = if(isWindows()) {
            "${binaryName.get()}.exe"
        } else if(isMac() || isUnix()) {
            binaryName.get()
        } else {
            logger.warn("This is an unknown operating system. Binary executable extension is not known.")

            binaryName.get()
        }

        execOperations.exec {
            commandLine(folder.resolve(executableFileName))
            commandLine.addAll(arguments.get())

            environment(this@RunBinary.environment.get())
        }
    }
}