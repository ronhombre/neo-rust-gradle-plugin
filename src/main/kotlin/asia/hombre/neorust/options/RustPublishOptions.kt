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

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Cargo publishing options
 *
 * Reads CARGO_REGISTRY_TOKEN and CARGO_REGISTRIES_NAME_TOKEN in that order for a token.
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustPublishOptions {
    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noVerify: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val allowDirty: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val token: Property<String>

    @get:Input
    @get:Optional
    abstract val index: Property<String>

    @get:Input
    @get:Optional
    abstract val registry: Property<String>

    init {
        token.convention(System.getenv("CARGO_REGISTRY_TOKEN")?: System.getenv("CARGO_REGISTRIES_NAME_TOKEN"))
    }
}