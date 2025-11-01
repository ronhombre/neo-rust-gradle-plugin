package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoTargettedTask
import javax.inject.Inject

/**
 * Cleans the target directory where past builds are cached
 *
 * This runs `cargo clean`
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
abstract class CargoClean @Inject constructor(): CargoTargettedTask() {
    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("clean")
        }
    }
}