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

import javax.inject.Inject

/**
 * Customizable configuration for any Rust example target
 *
 * @since 0.6.0
 * @author Ron Lauren Hombre
 */
abstract class ExampleConfiguration @Inject constructor(name: String): BinaryConfiguration(name) {
    override val SOURCE_DIRECTORY: String
        get() = "example"

    /**
     * Attempts to resolve a `.rs` file as the `path` for this Cargo target.
     *
     * The file will be searched in directory `src/main/rust/` of this Gradle project.
     *
     * So if you do `resolve("example.rs")`, it will be internally resolved as `src/example/rust/example.rs`. This
     * makes it extremely easy to define multiple example targets.
     *
     * It is also possible to do `resolve("client", "client.rs")`, and this will be resolved as
     * `src/example/rust/client/client.rs`.
     *
     * @param paths A list of arguments defining the location of the main file for this example Cargo target.
     * @return `true` if the file exists in the path and has been applied, `false` otherwise.
     */
    override fun resolve(vararg paths: String): Boolean {
        return super.resolve(*paths)
    }
}