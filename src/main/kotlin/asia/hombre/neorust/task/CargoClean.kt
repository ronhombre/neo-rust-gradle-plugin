package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoTargettedTask
import javax.inject.Inject

abstract class CargoClean @Inject constructor(): CargoTargettedTask() {
    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("clean")
        }
    }
}