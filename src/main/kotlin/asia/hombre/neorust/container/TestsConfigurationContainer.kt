package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalTestConfigurationContainer
import asia.hombre.neorust.options.targets.TestConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class TestsConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalTestConfigurationContainer<TestConfiguration>(
    objects,
    TestConfiguration::class.java
)