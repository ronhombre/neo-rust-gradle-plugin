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

import asia.hombre.neorust.option.BuildProfile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Customizable configuration for any Rust binary target
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class BinaryConfiguration @Inject constructor(name: String): CargoTargetConfiguration(name) {
    override val SOURCE_DIRECTORY: String
        get() = "main"

    @get:Input
    abstract val buildProfile: Property<BuildProfile>

    @get:Input
    @get:Optional
    abstract val arguments: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val workingDir: Property<String>

    @get:Input
    @get:Optional
    abstract val environment: MapProperty<String, String>

    init {
        //Binaries, Tests, Benchmarks, and Examples are always the "bin" crate type
        //https://doc.rust-lang.org/cargo/reference/cargo-targets.html#:~:text=Binaries%2C%20tests%2C%20and%20benchmarks%20are%20always%20the%20%E2%80%9Cbin%E2%80%9D%20crate%20type.
        this.crateType.finalizeValue()
        buildProfile.convention(BuildProfile.DEFAULT)
    }

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
    override fun resolve(vararg paths: String): Boolean {
        return super.resolve(*paths)
    }
}