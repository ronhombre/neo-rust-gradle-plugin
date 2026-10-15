package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalBuildConfigurationContainer
import asia.hombre.neorust.options.targets.BinaryConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class BinariesConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalBuildConfigurationContainer<BinaryConfiguration>(
    objects,
    BinaryConfiguration::class.java
) {
    //TODO: Add binaries configurations options (THIS IS THE SAME FOR THE OTHER CONFIGURATION CONTAINERS!)
    
}