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

package asia.hombre.neorust.option

/**
 * Build Profile used to build and run a binary.
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
enum class BuildProfile {
    /**
     * Inherits the build profile from the project when run.
     */
    DEFAULT,

    /**
     * Builds or executes the binary without the '--release' argument.
     *
     * This is faster and is intended for prototyping.
     */
    DEV,

    /**
     * Builds or executes the binary with the '--release' argument.
     *
     * This is slower but makes a compressed and highly optimized version.
     */
    RELEASE
}