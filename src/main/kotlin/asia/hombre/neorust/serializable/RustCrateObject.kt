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

package asia.hombre.neorust.serializable

import java.io.Serializable

/**
 * A serializable copy of RustCrateOptions
 *
 * @since 0.5.0
 * @author Ron Lauren Hombre
 */
internal data class RustCrateObject(
    val name: String,
    val version: String,
    val path: String? = null,
    val git: String? = null,
    val rev: String? = null,
    val branch: String? = null,
    val registry: String? = null,
    val features: Set<String> = emptySet(),
    val defaultFeatures: Boolean? = null,
    val optional: Boolean? = null
): Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID: Long = 3L
    }
}