package asia.hombre.neorust.internal.container

import asia.hombre.neorust.options.targets.CargoTargetConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.util.function.IntFunction
import javax.inject.Inject

abstract class InternalBuildConfigurationContainer<T : CargoTargetConfiguration> @Inject constructor(
    objects: ObjectFactory,
    itemType: Class<T>
): NamedDomainObjectContainer<T> by objects.domainObjectContainer(itemType) {

    @Deprecated("Not actually used")
    override fun <E> toArray(generator: IntFunction<Array<out E?>?>): Array<out E?>? {
        @Suppress("DEPRECATION")
        return super.toArray(generator)
    }

    @get:Input
    @get:Optional
    abstract val toolchain: Property<String>

    //TODO: Add '-Z' nightly flag support
}