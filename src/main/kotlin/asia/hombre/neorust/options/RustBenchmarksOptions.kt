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

import javax.inject.Inject

abstract class RustBenchmarksOptions @Inject constructor(): RustTargetOptions() {
    override val SOURCE_DIRECTORY: String = "bench"
    init {
        //Benchmarks are always the "bin" crate type
        //https://doc.rust-lang.org/cargo/reference/cargo-targets.html#:~:text=Binaries%2C%20tests%2C%20and%20benchmarks%20are%20always%20the%20%E2%80%9Cbin%E2%80%9D%20crate%20type.
        this.crateType.finalizeValue()
        path.convention(
            project
                .layout
                .projectDirectory
                .dir("src")
                .dir(SOURCE_DIRECTORY)
                .dir("rust")
                .file("bench.rs")
        )
    }

    /**
     * Attempts to resolve a `.rs` file as the `path` for this Cargo target.
     *
     * The file will be searched in directory `src/bench/rust/` of this Gradle project.
     *
     * So if you do `resolve("bench.rs")`, it will be internally resolved as `src/bench/rust/bench.rs`. This
     * makes it extremely easy to define multiple bench targets.
     *
     * It is also possible to do `resolve("extra", "client.rs")`, and this will be resolved as
     * `src/bench/rust/extra/client.rs`.
     *
     * @param paths A list of arguments defining the location of the main file for this bench Cargo target.
     * @return `true` if the file exists in the path and has been applied, `false` otherwise.
     */
    override fun resolve(vararg paths: String): Boolean {
        return super.resolve(*paths)
    }
}