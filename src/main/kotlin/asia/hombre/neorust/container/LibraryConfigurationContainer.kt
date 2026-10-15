package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalBuildConfigurationContainer
import asia.hombre.neorust.options.targets.LibraryConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class LibraryConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalBuildConfigurationContainer<LibraryConfiguration>(
    objects,
    LibraryConfiguration::class.java
)