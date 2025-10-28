package asia.hombre.neorust.options

import asia.hombre.neorust.option.CargoMessageFormat
import asia.hombre.neorust.option.CargoTiming
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class RustBuildOptions @Inject constructor(): RustTargetOptions() {
    @get:Input
    @get:Optional
    abstract val workspace: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val exclude: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val release: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val profile: Property<String>

    @get:Input
    @get:Optional
    abstract val buildAll: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val timings: Property<CargoTiming>
        /*set(value) {
            field = value

            if(field != CargoTiming.none && !extension.unstableFlags.contains("unstable-options"))
                extension.unstableFlags.add("unstable-options")
        }*/

    @get:Input
    @get:Optional
    abstract val messageFormat: ListProperty<CargoMessageFormat>

    @get:Input
    @get:Optional
    abstract val buildPlan: Property<Boolean>
        /*set(value) {
            field = value

            if(field && !extension.unstableFlags.contains("unstable-options"))
                extension.unstableFlags.add("unstable-options")
        }*/

    @get:Input
    @get:Optional
    abstract val futureIncompatReport: Property<Boolean>
}