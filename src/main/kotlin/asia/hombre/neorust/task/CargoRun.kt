package asia.hombre.neorust.task

import org.gradle.api.tasks.CacheableTask
import javax.inject.Inject

@CacheableTask
abstract class CargoRun @Inject constructor(): CargoBuild() {
    init {
        //Checks if this instance is not a subclass and prevents changes to unavailable properties
        if(this !is CargoBench) {
            lib.finalizeValue()
            bins.finalizeValue()
            examples.finalizeValue()
            test.finalizeValue()
            tests.finalizeValue()
            bench.finalizeValue()
            benches.finalizeValue()
            allTargets.finalizeValue()
        }
    }

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            remove("build")
            add("run")
        }
    }
}