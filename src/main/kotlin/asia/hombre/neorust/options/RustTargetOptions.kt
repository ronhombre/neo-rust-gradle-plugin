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

package asia.hombre.neorust.options

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Customizable configuration for "lib", "bin", "bench", "test", and "example" Cargo targets.
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class RustTargetOptions @Inject constructor() {
    @Suppress("PropertyName")
    @get:Internal
    internal open val SOURCE_DIRECTORY = "main"

    @get:Inject
    abstract val project: Project

    @get:Input
    @get:Optional
    abstract val name: Property<String>

    @get:InputFile
    abstract val path: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val test: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val doctest: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val bench: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val doc: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val procMacro: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val harness: Property<Boolean>

    @get:Input
    abstract val crateType: ListProperty<String>

    @get:Input
    abstract val requiredFeatures: ListProperty<String>

    @get:Internal
    var isEnabled = false

    /**
     * Attempts to resolve a `.rs` file as the `path` for this Cargo target.
     *
     * The file will be searched in directory `src/main/rust/` of this Gradle project.
     *
     * So if you do `resolve("example.rs")`, it will be internally resolved as `src/main/rust/example.rs`. This makes it
     * extremely easy to define multiple targets.
     *
     * It is also possible to do `resolve("bin", "client.rs")`, and this will be resolved as
     * `src/main/rust/bin/client.rs`.
     *
     * @param paths A list of arguments defining the location of the main file for this binary Cargo target.
     * @return `true` if the file exists in the path and has been applied, `false` otherwise.
     */
    open fun resolve(vararg paths: String): Boolean {
        var currentDirectory = project
            .layout
            .projectDirectory
            .dir("src")
            .dir(SOURCE_DIRECTORY)
            .dir("rust")

        paths.forEachIndexed { index, path ->
            if(index != paths.lastIndex) currentDirectory = currentDirectory.dir(path)
        }

        val finalFilePath = currentDirectory.file(paths.last())

        if(!finalFilePath.asFile.exists()) {
            project.logger.error("${finalFilePath.asFile.path} could not be resolved and has not been applied. It does not exist or we don't have permission to read it.")

            return false
        }

        this.path.set(finalFilePath)

        return true
    }
}