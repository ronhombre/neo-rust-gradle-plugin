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

package asia.hombre.neorust.extension

import asia.hombre.neorust.container.BenchmarksConfigurationContainer
import asia.hombre.neorust.container.BinariesConfigurationContainer
import asia.hombre.neorust.container.ExamplesConfigurationContainer
import asia.hombre.neorust.container.LibraryConfigurationContainer
import asia.hombre.neorust.container.LintsConfigurationContainer
import asia.hombre.neorust.container.ProfilesConfigurationContainer
import asia.hombre.neorust.container.TestsConfigurationContainer
import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTestExecutionOptions
import asia.hombre.neorust.options.RustTestOptions
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import java.nio.file.Path
import javax.inject.Inject

/**
 * Rust Extension
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
@Suppress("unused")
abstract class RustExtension @Inject constructor(project: Project) {
    @Internal
    internal val logger: Logger = project.logger
    @Internal
    internal val objects: ObjectFactory = project.objects

    /**
     * The package to publish. See cargo-pkgid(1) for the SPEC format.
     */
    @get:Input
    @get:Optional
    abstract val packageSelect: Property<String>

    /**
     * Publish for the given architecture. The default is the host architecture. The general format of the triple is
     * `<arch><sub>-<vendor>-<sys>-<abi>`. Run `rustc --print target-list` for a list of supported targets. This flag
     * may be specified multiple times.
     *
     * This may also be specified with the build.target config value.
     *
     * Note that specifying this flag makes Cargo run in a different mode where the target artifacts are placed in a
     * separate directory. See the build cache documentation for more details.
     */
    @get:Input
    @get:Optional
    abstract val target: Property<String>

    /**
     * Directory for all generated artifacts and intermediate files. May also be specified with the `CARGO_TARGET_DIR`
     * environment variable, or the build.target-dir config value. Defaults to target in the root of the workspace.
     */
    @get:InputDirectory
    @get:Optional
    abstract val targetDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val allFeatures: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noDefaultFeatures: Property<Boolean>

    @get:InputFile
    abstract val manifestPath: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val ignoreRustVersion: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val locked: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val offline: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val frozen: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val verbose: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val quiet: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val color: Property<CargoColor>

    @get:Input
    @get:Optional
    abstract val toolchain: Property<String>

    @get:Input
    @get:Optional
    abstract val config: MapProperty<String, String>

    @get:Input
    @get:Optional
    abstract val configPaths: ListProperty<Path>

    @get:Input
    @get:Optional
    abstract val jobs: Property<Int>

    //TODO: Document this doesn't work with benchmarking. Use --no-fail-fast
    @get:Input
    @get:Optional
    abstract val keepGoing: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val futureIncompatReport: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val unstableFlags: ListProperty<String>

    @get:Input
    abstract val autoLib: Property<Boolean>

    @get:Input
    abstract val autoBins: Property<Boolean>

    @get:Input
    abstract val autoTests: Property<Boolean>

    @get:Input
    abstract val autoBenches: Property<Boolean>

    @get:Input
    abstract val autoExamples: Property<Boolean>

    init {
        manifestPath.convention(project.layout.buildDirectory.file("Cargo.toml"))
        targetDirectory.convention(project.layout.buildDirectory.dir("target"))

        autoLib.convention(true)
        autoBins.convention(true)
        autoTests.convention(true)
        autoBenches.convention(true)
        autoExamples.convention(true)

        name.convention(project.name.lowercase())
    }

    @get:Input
    abstract val name: Property<String>

    @get:Input
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

    @get:InputFile
    @get:Optional
    abstract val licenseFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val keywords: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val categories: ListProperty<String>

    @get:InputDirectory
    @get:Optional
    abstract val workspace: DirectoryProperty

    @get:InputFile
    @get:Optional
    abstract val buildFile: RegularFileProperty

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

    @Internal
    internal val rustTestOptions: RustTestOptions = objects.newInstance(
        RustTestOptions::class.java
    )
    @Internal
    internal val rustBenchmarkOptions: RustTestOptions = objects.newInstance(
        RustTestOptions::class.java
    )
    @Internal
    internal val rustBuildOptions: RustBuildOptions = objects.newInstance(
        RustBuildOptions::class.java
    )
    @Internal
    internal val rustPublishOptions: RustPublishOptions = objects.newInstance(
        RustPublishOptions::class.java
    )
    @Internal
    internal val rustTestExecutionOptions: RustTestExecutionOptions = objects.newInstance(
        RustTestExecutionOptions::class.java
    )
    @Internal
    internal val rustFeaturesOptions: RustFeaturesOptions = objects.newInstance(
        RustFeaturesOptions::class.java
    )

    @Internal
    internal val lintsConfiguration: LintsConfigurationContainer = objects.newInstance(
        LintsConfigurationContainer::class.java
    )

    //Cargo Targets
    @Internal
    internal val libraryConfiguration: LibraryConfigurationContainer = objects.newInstance(
        LibraryConfigurationContainer::class.java
    )
    @Internal
    internal val binariesConfiguration: BinariesConfigurationContainer = objects.newInstance(
        BinariesConfigurationContainer::class.java
    )
    @Internal
    internal val examplesConfiguration: ExamplesConfigurationContainer = objects.newInstance(
        ExamplesConfigurationContainer::class.java
    )
    @Internal
    internal val testsConfiguration: TestsConfigurationContainer = objects.newInstance(
        TestsConfigurationContainer::class.java
    )
    @Internal
    internal val benchmarksConfiguration: BenchmarksConfigurationContainer = objects.newInstance(
        BenchmarksConfigurationContainer::class.java
    )

    //Profiles
    @Internal
    internal val profilesConfiguration: ProfilesConfigurationContainer = objects.newInstance(
        ProfilesConfigurationContainer::class.java
    )
}