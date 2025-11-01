package asia.hombre.neorust.options

import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class RustFeaturesOptions @Inject constructor() {
    @Internal
    internal val list: MutableList<Feature> = mutableListOf()

    internal data class Feature(val name: String, val values: List<String>)
}