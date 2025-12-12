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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Configure Cargo crate manifest options (Cargo.toml)
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustManifestOptions @Inject constructor(objectFactory: ObjectFactory) {
    @get:Nested
    internal abstract val packageConfig: Property<Package>

    init {
        packageConfig.set(objectFactory.newInstance(Package::class.java))
    }

    abstract class Package {
        @get:Input
        @get:Optional
        abstract val name: Property<String>
        @get:Input
        @get:Optional
        abstract val version: Property<String>
        @get:Input
        @get:Optional
        abstract val authors: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val edition: Property<String>
        @get:Input
        @get:Optional
        abstract val rustVersion: Property<String>
        @get:Input
        @get:Optional
        abstract val description: Property<String>
        @get:Input
        @get:Optional
        abstract val documentation: Property<String>
        @get:Input
        @get:Optional
        abstract val readme: Property<String>
        @get:Input
        @get:Optional
        abstract val homepage: Property<String>
        @get:Input
        @get:Optional
        abstract val repository: Property<String>
        @get:Input
        @get:Optional
        abstract val license: Property<String>
        @get:Input
        @get:Optional
        abstract val licenseFile: Property<String>
        @get:Input
        @get:Optional
        abstract val keywords: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val categories: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val workspace: Property<String>
        @get:Input
        @get:Optional
        abstract val buildFile: Property<String>
        @get:Input
        @get:Optional
        abstract val links: Property<String>
        @get:Input
        @get:Optional
        abstract val exclude: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val include: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val publish: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val publishEnabled: Property<Boolean>
        @get:Input
        @get:Optional
        abstract val defaultRun: Property<String>
    }
}