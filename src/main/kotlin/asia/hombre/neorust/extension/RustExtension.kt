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

import asia.hombre.neorust.option.CargoColor
import asia.hombre.neorust.options.RustBenchOptions
import asia.hombre.neorust.options.RustBuildOptions
import asia.hombre.neorust.options.RustBuildTargetOptions
import asia.hombre.neorust.options.RustFeaturesOptions
import asia.hombre.neorust.options.RustPackageOptions
import asia.hombre.neorust.options.RustProfileOptions
import asia.hombre.neorust.options.RustPublishOptions
import asia.hombre.neorust.options.RustTestOptions
import asia.hombre.neorust.options.targets.BenchmarkConfiguration
import asia.hombre.neorust.options.targets.BinaryConfiguration
import asia.hombre.neorust.options.targets.CargoTargetConfiguration
import asia.hombre.neorust.options.targets.ExampleConfiguration
import asia.hombre.neorust.options.targets.LibraryConfiguration
import asia.hombre.neorust.options.targets.TestConfiguration
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
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
    internal val projectName: String = project.name
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
    abstract val features: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val featuresList: MapProperty<String, List<String>>

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
    abstract val jobs: Property<Int>

    @get:Input
    @get:Optional
    abstract val keepGoing: Property<Boolean>

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
    }

    @Internal
    internal val rustBuildTargetOptions: RustBuildTargetOptions = objects.newInstance(
        RustBuildTargetOptions::class.java
    )
    @Internal
    internal val rustPackageOptions: RustPackageOptions = objects.newInstance(
        RustPackageOptions::class.java
    )
    @Internal
    internal val rustBenchOptions: RustBenchOptions = objects.newInstance(
        RustBenchOptions::class.java
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
    internal val rustTestOptions: RustTestOptions = objects.newInstance(
        RustTestOptions::class.java
    )
    @Internal
    internal val rustFeaturesOptions: RustFeaturesOptions = objects.newInstance(
        RustFeaturesOptions::class.java
    )
    @Internal
    internal val rustProfileOptions: RustProfileOptions = objects.newInstance(
        RustProfileOptions::class.java
    )

    //Cargo Targets
    @Internal
    internal val libraryConfiguration: LibraryConfiguration = objects.newInstance(
        LibraryConfiguration::class.java,
        project.name
    )
    @Internal
    internal val binariesConfiguration: NamedDomainObjectContainer<BinaryConfiguration> = objects.safeDomainObjectContainer()
    @Internal
    internal val examplesConfiguration: NamedDomainObjectContainer<ExampleConfiguration> = objects.safeDomainObjectContainer()
    @Internal
    internal val testsConfiguration: NamedDomainObjectContainer<TestConfiguration> = objects.safeDomainObjectContainer()
    @Internal
    internal val benchmarksConfiguration: NamedDomainObjectContainer<BenchmarkConfiguration> = objects.safeDomainObjectContainer()

    private inline fun <reified T : CargoTargetConfiguration> ObjectFactory.safeDomainObjectContainer(): NamedDomainObjectContainer<T> {
        return this.domainObjectContainer(T::class.java) { name ->
            //What the fuck, Gradle!
            try {
                this.newInstance(T::class.java, name)
            } catch (e: Exception) {
                //Unwraps the actual Exception and fallbacks to the current Exception
                val error = e.cause?: e
                //Only by calling logger.error(..) does Gradle actually stop the task. Otherwise, it absorbs the error
                logger.error(error.message, error)
                throw GradleException("Could not create ${T::class.simpleName} '$name'", error)
            }
        }
    }

    fun binaries(action: NamedDomainObjectContainer<BinaryConfiguration>.() -> Unit) {
        action(binariesConfiguration)
    }

    fun tests(action: NamedDomainObjectContainer<TestConfiguration>.() -> Unit) {
        action(testsConfiguration)
    }

    fun benchmarks(action: NamedDomainObjectContainer<BenchmarkConfiguration>.() -> Unit) {
        action(benchmarksConfiguration)
    }

    fun examples(action: NamedDomainObjectContainer<ExampleConfiguration>.() -> Unit) {
        action(examplesConfiguration)
    }
}