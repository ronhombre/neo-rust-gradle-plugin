package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalTestConfigurationContainer
import asia.hombre.neorust.options.targets.BenchmarkConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class BenchmarksConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalTestConfigurationContainer<BenchmarkConfiguration>(
    objects,
    BenchmarkConfiguration::class.java
)