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

import asia.hombre.neorust.option.CargoMessageFormat
import asia.hombre.neorust.option.CargoTiming
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Global Cargo build options
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class RustBuildOptions @Inject constructor(): RustBuildTargetOptions() {
    @get:Input
    @get:Optional
    abstract val workspace: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val exclude: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val release: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val profile: Property<String>

    @get:Input
    @get:Optional
    abstract val buildAll: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val timings: Property<CargoTiming>

    @get:Input
    @get:Optional
    abstract val messageFormat: ListProperty<CargoMessageFormat>

    @get:Input
    @get:Optional
    abstract val buildPlan: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val futureIncompatReport: Property<Boolean>
}