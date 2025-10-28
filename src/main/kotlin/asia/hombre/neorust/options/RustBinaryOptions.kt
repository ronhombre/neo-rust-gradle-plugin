package asia.hombre.neorust.options

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustBinaryOptions @Inject constructor() {
    @get:Inject
    abstract val objectFactory: ObjectFactory

    @Internal
    internal val list: MutableList<Binary> = mutableListOf()

    abstract class Binary {
        @get:Input
        abstract val name: Property<String>

        @get:Input
        @get:Optional
        abstract val doc: Property<Boolean>
    }
}