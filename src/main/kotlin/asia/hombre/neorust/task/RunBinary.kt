package asia.hombre.neorust.task

import isMac
import isUnix
import isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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
    @get:InputFile
    abstract val manifestPath: RegularFileProperty
    @get:Input
    abstract val binaryName: Property<String>
    @get:Input
    abstract val buildProfile: Property<String>
    @get:Input
    abstract val arguments: ListProperty<String>
    @get:Input
    abstract val environment: MapProperty<String, String>

    @OptIn(ExperimentalTime::class)
    @TaskAction
    fun execute() {
        val folder = if(targetDirectory.isPresent) {
            File(targetDirectory.get())
        } else {
            manifestPath.get().asFile.parentFile.resolve("target")
        }.resolve(buildProfile.get())

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

        val execTime = measureTime {
            execOperations.exec {
                commandLine(folder.resolve(executableFileName))
                commandLine.addAll(arguments.get())

                environment(this@RunBinary.environment.get())
            }
        }

        println("Executing $executableFileName took $execTime")
    }
}