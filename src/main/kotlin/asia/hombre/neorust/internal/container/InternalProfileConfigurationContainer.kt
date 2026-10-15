package asia.hombre.neorust.internal.container

import asia.hombre.neorust.options.profiles.ProfileConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import java.util.function.IntFunction
import javax.inject.Inject

abstract class InternalProfileConfigurationContainer<T : ProfileConfiguration> @Inject constructor(
    objects: ObjectFactory,
    itemType: Class<T>
): NamedDomainObjectContainer<T> by objects.domainObjectContainer(itemType) {

    @Deprecated("Not actually used")
    override fun <E : Any?> toArray(generator: IntFunction<Array<out E?>?>): Array<out E?>? {
        @Suppress("DEPRECATION")
        return super.toArray(generator)
    }
}