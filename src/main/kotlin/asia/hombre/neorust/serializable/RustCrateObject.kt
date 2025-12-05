package asia.hombre.neorust.serializable

import java.io.Serializable

/**
 * A serializable copy of RustCrateOptions
 *
 * @since 0.5.0
 * @author Ron Lauren Hombre
 */
internal data class RustCrateObject(
    val name: String,
    val version: String,
    val path: String? = null,
    val git: String? = null,
    val rev: String? = null,
    val registry: String? = null,
    val features: Set<String> = emptySet(),
    val defaultFeatures: Boolean? = null,
    val optional: Boolean? = null
): Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID: Long = 2L
    }
}