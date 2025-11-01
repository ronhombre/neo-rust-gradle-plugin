package asia.hombre.neorust.options

import asia.hombre.neorust.option.BuildProfile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Cargo options for binary targets
 *
 * @since 0.3.0
 * @author Ron Lauren Hombre
 */
abstract class RustBinaryOptions @Inject constructor() {
    @get:Internal
    @get:Inject
    internal abstract val objectFactory: ObjectFactory

    @Internal
    internal val list: MutableList<Binary> = mutableListOf()

    abstract class Binary {
        @get:Input
        abstract val name: Property<String>

        @get:Input
        @get:Optional
        abstract val doc: Property<Boolean>

        @get:Input
        @get:Optional
        abstract val requiredFeatures: ListProperty<String>

        @get:Input
        abstract val buildProfile: Property<BuildProfile>

        @get:Input
        @get:Optional
        abstract val arguments: ListProperty<String>

        @get:Input
        @get:Optional
        abstract val environment: MapProperty<String, String>

        init {
            buildProfile.convention(BuildProfile.DEFAULT)
        }
    }
}