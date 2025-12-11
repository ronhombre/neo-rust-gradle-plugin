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
import javax.inject.Inject

/**
 * Customizable configuration for the Rust library target
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class RustLibraryOptions @Inject constructor(): RustTargetOptions() {
    @get:Inject
    abstract val project: Project
    init {
        path.convention(project.layout.projectDirectory.dir("src").dir("main").dir("rust").file("lib.rs"))
    }
}