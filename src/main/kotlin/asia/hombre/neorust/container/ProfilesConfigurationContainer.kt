package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalProfileConfigurationContainer
import asia.hombre.neorust.options.profiles.ProfileConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class ProfilesConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalProfileConfigurationContainer<ProfileConfiguration>(
    objects,
    ProfileConfiguration::class.java
)