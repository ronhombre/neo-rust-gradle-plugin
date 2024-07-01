import asia.hombre.neorust.extension.RustExtension
import org.gradle.api.Action
import org.gradle.api.Project

fun Project.rust(rustConfiguration: Action<RustExtension>) {
    this.extensions.configure("rust", rustConfiguration)
}