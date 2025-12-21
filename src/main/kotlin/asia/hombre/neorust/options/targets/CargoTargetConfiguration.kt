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

package asia.hombre.neorust.options.targets

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
abstract class CargoTargetConfiguration @Inject constructor(name: String) {
    @Suppress("PropertyName")
    @get:Internal
    internal open val SOURCE_DIRECTORY = "undefined"

    @get:Inject
    abstract val project: Project

    @get:Input
    abstract val name: Property<String>

    @get:InputFile
    @get:Optional
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

    @get:Input
    @get:Optional
    abstract val buildFeatures: ListProperty<String>

    @get:Internal
    var isEnabled = false

    init {
        this.name.convention(name)
    }

    /**
     * An extremely flexible resolver for any `.rs` file for NRGP. This is not meant to be a public-facing API.
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