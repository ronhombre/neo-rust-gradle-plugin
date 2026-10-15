package asia.hombre.neorust.container

import asia.hombre.neorust.internal.container.InternalLintConfigurationContainer
import asia.hombre.neorust.options.lints.LintConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class LintsConfigurationContainer @Inject constructor(
    objects: ObjectFactory
) : InternalLintConfigurationContainer<LintConfiguration>(
    objects,
    LintConfiguration::class.java
)