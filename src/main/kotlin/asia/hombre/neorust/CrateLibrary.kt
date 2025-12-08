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

package asia.hombre.neorust

import asia.hombre.neorust.options.RustCrateOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import javax.inject.Inject

/**
 * Holds crates used in this project
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
internal abstract class CrateLibrary @Inject constructor() {
    @get:Inject
    internal abstract val objects: ObjectFactory

    @get:Nested
    internal abstract val dependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val devDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val buildDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedDevDependencies: ListProperty<RustCrateOptions>
    @get:Nested
    internal abstract val unresolvedBuildDependencies: ListProperty<RustCrateOptions>
}