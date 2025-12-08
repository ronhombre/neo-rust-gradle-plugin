package asia.hombre.neorust.options

import asia.hombre.neorust.serializable.RustCrateObject
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.Serializable
import javax.inject.Inject

/**
 * Define a Crate dependency for Cargo to use
 *
 * @since 0.3.0
 * @author Ron Lauren Hombre
 */
abstract class RustCrateOptions @Inject constructor(@get:Input internal val name: String, @get:Input internal val version: String): Serializable {
    @get:Input
    @get:Optional
    abstract val path: Property<String>
    @get:Input
    @get:Optional
    abstract val git: Property<String>
    @get:Input
    @get:Optional
    abstract val rev: Property<String>
    @get:Input
    @get:Optional
    abstract val branch: Property<String>
    @get:Input
    @get:Optional
    abstract val registry: Property<String>
    @get:Input
    @get:Optional
    abstract val features: ListProperty<String>
    @get:Input
    @get:Optional
    abstract val defaultFeatures: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val optional: Property<Boolean>

    internal fun toObject(): RustCrateObject {
        return RustCrateObject(
            name,
            version,
            if(path.isPresent) path.get() else null,
            if(git.isPresent) git.get() else null,
            if(rev.isPresent) rev.get() else null,
            if(branch.isPresent) branch.get() else null,
            if(registry.isPresent) registry.get() else null,
            features.get().toSet(),
            if(defaultFeatures.isPresent) defaultFeatures.get() else null,
            if(optional.isPresent) optional.get() else null
        )
    }

    internal fun fromObject(rustCrateObject: RustCrateObject) {
        path.set(rustCrateObject.path)
        git.set(rustCrateObject.git)
        rev.set(rustCrateObject.rev)
        branch.set(rustCrateObject.branch)
        registry.set(rustCrateObject.registry)
        features.set(rustCrateObject.features)
        defaultFeatures.set(rustCrateObject.defaultFeatures)
        optional.set(rustCrateObject.optional)
    }

    /**
     * Copies and sets properties from another RustCrateOptions instance if they are not set already or is empty (array)
     */
    internal fun copyIfNotSetFrom(other: RustCrateOptions) {
        if(!path.isPresent) path.set(other.path)
        if(!git.isPresent) git.set(other.git)
        if(!rev.isPresent) rev.set(other.rev)
        if(!branch.isPresent) branch.set(other.branch)
        if(!registry.isPresent) registry.set(other.registry)
        if(features.get().isEmpty()) features.set(other.features)
        if(!defaultFeatures.isPresent) defaultFeatures.set(other.defaultFeatures)
        if(!optional.isPresent) optional.set(other.optional)
    }
}