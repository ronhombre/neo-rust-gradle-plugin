package asia.hombre.neorust.internal.container

import asia.hombre.neorust.options.targets.BinaryConfiguration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class InternalTestConfigurationContainer<T: BinaryConfiguration> @Inject constructor(
    objects: ObjectFactory,
    itemType: Class<T>
) : InternalBuildConfigurationContainer<T>(
    objects,
    itemType
) {
    @get:Input
    @get:Optional
    abstract val noRun: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noCapture: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val noFailFast: Property<Boolean>
}