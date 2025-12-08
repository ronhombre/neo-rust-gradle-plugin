/*
 * Copyright 2025 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *        and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asia.hombre.neorust.task

import isMac
import isUnix
import isWindows
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
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

    @get:InputDirectory
    abstract val targetDirectory: DirectoryProperty
    @get:InputFile
    abstract val manifestPath: RegularFileProperty
    @get:Input
    abstract val binaryName: Property<String>
    @get:Input
    abstract val buildProfile: Property<String>
    @get:Input
    abstract val workingDir: Property<String>
    @get:Input
    abstract val arguments: ListProperty<String>
    @get:Input
    abstract val environment: MapProperty<String, String>

    @OptIn(ExperimentalTime::class)
    @TaskAction
    fun execute() {
        val folder = targetDirectory.get().asFile
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
                commandLine(folder.resolve(executableFileName), *arguments.get().toTypedArray())

                environment(this@RunBinary.environment.get())

                if (this@RunBinary.workingDir.isPresent) {
                    workingDir(this@RunBinary.workingDir.get())
                }

                //Allow writing into std in when the run task is running
                standardInput = System.`in`
            }
        }

        print("\nExecuting $executableFileName took $execTime")
    }
}