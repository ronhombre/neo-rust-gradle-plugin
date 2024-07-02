package asia.hombre.neorust

import asia.hombre.neorust.options.RustCrateOptions
import org.gradle.api.Action
import org.gradle.api.artifacts.*

class RustCrate(crateConfig: Action<RustCrateOptions>): Dependency {
    val options = RustCrateOptions()
    private var reasons: String? = null

    init {
        crateConfig.execute(options)
    }

    override fun getGroup(): String {
        return options.registry
    }

    override fun getName(): String {
        return options.name
    }

    override fun getVersion(): String {
        return options.version
    }

    override fun contentEquals(dependency: Dependency): Boolean {
        if(dependency !is RustCrate) return false
        if(dependency.options.name != options.name) return false
        if(dependency.options.version != options.version) return false
        if(dependency.options.path != options.path) return false
        if(dependency.options.registry != options.registry) return false
        if(dependency.options.features != options.features) return false
        if(dependency.options.defaultFeatures != options.defaultFeatures) return false
        if(dependency.options.optional != options.optional) return false

        return true
    }

    override fun copy(): Dependency {
        return RustCrate {
            name = options.name
            version = options.version
            path = options.path
            registry = options.registry
            features.addAll(options.features)
            defaultFeatures = options.defaultFeatures
            optional = options.optional
        }
    }

    override fun getReason(): String? {
        return reasons
    }

    override fun because(reason: String?) {
        reasons = reason
    }

    override fun toString(): String {
        return this::class.simpleName +
                "(Name=${options.name}, Version=${options.version}, Path=${options.path}, Registry=${options.registry}, Features=[${options.features.joinToString(", ")}], DefaultFeatures=${options.defaultFeatures}, Optional=${options.optional})"
    }
}