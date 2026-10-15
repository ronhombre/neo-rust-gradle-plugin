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

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.NormalizeLineEndings
import java.io.File
import javax.inject.Inject

/**
 * Customizable configuration for "lib", "bin", "bench", "test", and "example" Cargo targets.
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class CargoTargetConfiguration @Inject constructor(private val internalName: String): Named {
    @Suppress("PropertyName")
    @get:Internal
    internal open val SOURCE_DIRECTORY: String
        get() = "undefined"

    @get:Internal
    internal var actualName: String = if(name == "main") project.name.lowercase() else name

    @get:Internal
    internal var binaryName: String = actualName

    @get:Inject
    abstract val project: Project

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:NormalizeLineEndings
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
    internal var isEnabled = false

    @get:Internal
    internal var isExcluded = false

    @get:Internal
    internal var isShadow = false

    @get:Internal
    internal val currentDirectory: Directory = project
        .layout
        .projectDirectory
        .dir("src")
        .dir(SOURCE_DIRECTORY)
        .dir("rust")

    init {
        isEnabled = (tryResolve("$name.rs") || tryResolve(name, "main.rs"))
                || this is LibraryConfiguration && tryResolve("lib.rs")
    }

    internal fun sameAs(other: CargoTargetConfiguration) {
        this.path.set(other.path)
        this.isEnabled = other.isEnabled
        this.binaryName = other.binaryName
        this.isShadow = true
    }

    open fun resolve(vararg paths: String): Boolean {
        if(!tryResolve(*paths)) {
            project.logger.error("${paths.joinToString(File.separator)} could not be resolved and has not been applied. It does not exist or we don't have permission to read it.")

            return false
        }

        return true
    }

    /**
     * An extremely flexible resolver for any `.rs` file for NRGP. This is not meant to be a public-facing API.
     */
    private fun tryResolve(vararg paths: String): Boolean {
        var currentDirectory = currentDirectory
        paths.forEachIndexed { index, path ->
            if(index != paths.lastIndex) currentDirectory = currentDirectory.dir(path)
        }

        val finalFilePath = currentDirectory.file(paths.last())

        if(!finalFilePath.asFile.exists()) {
            return false
        }

        this.path.set(finalFilePath)

        return true
    }

    @Internal
    override fun getName(): String = internalName.lowercase()
}