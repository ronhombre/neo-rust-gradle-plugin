package asia.hombre.neorust.options

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import javax.inject.Inject

/**
 * Configure features for the Cargo project
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
abstract class RustFeaturesOptions @Inject constructor() {
    @get:Internal
    internal abstract val list: ListProperty<Feature>

    internal data class Feature(val name: String, val values: List<String>)
}