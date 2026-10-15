package asia.hombre.neorust.options.profiles

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import javax.inject.Inject

abstract class ProfileConfiguration @Inject constructor(private val name: String): GenericProfileConfiguration(false), Named {
    @get:Inject
    internal abstract val objects: ObjectFactory

    @get:Nested
    internal abstract val overrides: MapProperty<String, GenericProfileConfiguration>

    @Internal
    override fun getName(): String = name
}