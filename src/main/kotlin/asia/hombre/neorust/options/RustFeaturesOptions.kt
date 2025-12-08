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

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import java.io.Serializable
import javax.inject.Inject

/**
 * Configure features for the Cargo project
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
abstract class RustFeaturesOptions @Inject constructor() {
    @get:Input
    internal abstract val list: ListProperty<Feature>

    internal data class Feature(val name: String, val values: List<String>): Serializable
}