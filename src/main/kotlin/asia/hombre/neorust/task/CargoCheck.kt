package asia.hombre.neorust.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.classpath.Instrumented
import java.io.IOException
import javax.inject.Inject

abstract class CargoCheck @Inject constructor(): DefaultTask() {

    @get:OutputFile
    abstract val checkCache: RegularFileProperty

    @TaskAction
    fun checkCargo() {
        try {
            val execResult = Instrumented.exec(Runtime.getRuntime(), "cargo version", "")

            execResult.waitFor()

            if (execResult.exitValue() != 0)
                throw RuntimeException("Command failed with exit value: ${execResult.exitValue()}")

            checkCache.get().asFile.apply {
                parentFile.mkdirs()
                createNewFile()
            }
        } catch (_: IOException) {
            throw RuntimeException("Cannot find cargo. Install it or set the path environment variable for your system.")
        }
    }
}