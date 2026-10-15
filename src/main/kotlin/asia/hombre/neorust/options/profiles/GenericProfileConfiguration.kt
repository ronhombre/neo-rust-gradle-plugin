package asia.hombre.neorust.options.profiles

import asia.hombre.neorust.option.DebugFlag
import asia.hombre.neorust.option.LTOFlag
import asia.hombre.neorust.option.OptLevel
import asia.hombre.neorust.option.PanicFlag
import asia.hombre.neorust.option.StripFlag
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class GenericProfileConfiguration @Inject constructor(@get:Input internal val overrideMode: Boolean) {
    @get:Input
    @get:Optional
    abstract val optLevel: Property<OptLevel>

    @get:Input
    @get:Optional
    abstract val debug: Property<DebugFlag>

    @get:Input
    @get:Optional
    abstract val splitDebugInfo: Property<String>

    @get:Input
    @get:Optional
    abstract val strip: Property<StripFlag>

    @get:Input
    @get:Optional
    abstract val debugAssertions: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val overflowChecks: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val lto: Property<LTOFlag>

    @get:Input
    @get:Optional
    abstract val panic: Property<PanicFlag>

    @get:Input
    @get:Optional
    abstract val incremental: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val codegenUnits: Property<Int>

    @get:Input
    @get:Optional
    abstract val rpath: Property<Boolean>

    init {
        //These are all or nothing flags that cannot be overridden by-part. It has to be done for the whole compilation
        if(overrideMode) {
            panic.disallowChanges()
            lto.disallowChanges()
            rpath.disallowChanges()
        }
    }

    @Internal
    internal fun isUsed(): Boolean {
        return optLevel.isPresent or
                debug.isPresent or
                splitDebugInfo.isPresent or
                strip.isPresent or
                debugAssertions.isPresent or
                overflowChecks.isPresent or
                lto.isPresent or
                panic.isPresent or
                incremental.isPresent or
                codegenUnits.isPresent or
                rpath.isPresent
    }
}