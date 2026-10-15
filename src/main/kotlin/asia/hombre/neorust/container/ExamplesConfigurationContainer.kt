package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalBuildConfigurationContainer
import asia.hombre.neorust.options.targets.ExampleConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class ExamplesConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalBuildConfigurationContainer<ExampleConfiguration>(
    objects,
    ExampleConfiguration::class.java
)