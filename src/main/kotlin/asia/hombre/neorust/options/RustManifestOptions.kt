package asia.hombre.neorust.options

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

open class RustManifestOptions @Inject constructor(objectFactory: ObjectFactory) {
    @Internal
    internal val packageConfig = objectFactory.newInstance(Package::class.java)
    @Internal
    internal val libConfig = objectFactory.newInstance(Library::class.java)

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
        @get:Input
        @get:Optional
        abstract val autoBins: Property<Boolean>
        @get:Input
        @get:Optional
        abstract val autoExamples: Property<Boolean>
        @get:Input
        @get:Optional
        abstract val autoTests: Property<Boolean>
        @get:Input
        @get:Optional
        abstract val autoBenches: Property<Boolean>
    }

    abstract class Library {
        @get:Input
        @get:Optional
        abstract val crateType: ListProperty<String>
        @get:Input
        @get:Optional
        abstract val path: Property<String>

        init {
            crateType.convention(mutableListOf())
            path.convention("src/main/rust/lib.rs")
        }
    }
}