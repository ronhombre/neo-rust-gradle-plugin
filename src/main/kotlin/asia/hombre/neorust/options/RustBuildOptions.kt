package asia.hombre.neorust.options

import asia.hombre.neorust.option.CargoMessageFormat
import asia.hombre.neorust.option.CargoTiming
import asia.hombre.neorust.extension.RustExtension
import org.gradle.api.Action
import org.gradle.api.tasks.Input

open class RustBuildOptions(private val extension: RustExtension): RustTargetOptions() {
    @get:Input
    var workspace: Boolean = false

    @get:Input
    var exclude: MutableList<String> = mutableListOf()

    @get:Input
    var release: Boolean = false

    @get:Input
    var profile: String = ""

    @get:Input
    var timings: CargoTiming = CargoTiming.none
        set(value) {
            field = value

            if(field != CargoTiming.none && !extension.unstableFlags.contains("unstable-options"))
                extension.unstableFlags.add("unstable-options")
        }

    @get:Input
    var messageFormat: MutableList<CargoMessageFormat> = mutableListOf()

    @get:Input
    var buildPlan: Boolean = false
        set(value) {
            field = value

            if(field && !extension.unstableFlags.contains("unstable-options"))
                extension.unstableFlags.add("unstable-options")
        }

    @get:Input
    var futureIncompatReport: Boolean = false
}