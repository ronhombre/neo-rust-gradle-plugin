package asia.hombre.neorust.options.lints

import org.gradle.api.Named
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class LintConfiguration @Inject constructor(private val name: String): Named {
    @get:Internal
    internal val lints: MutableMap<String, LintProperty> = emptyMap<String, LintProperty>().toMutableMap()

    @Internal
    override fun getName(): String = name

    operator fun set(key: String, property: LintProperty) {
        lints[key] = property
    }

    operator fun set(key: String, level: String) {
        lints[key] = LintProperty(level)
    }

    operator fun get(key: String): LintProperty? = lints[key]
}