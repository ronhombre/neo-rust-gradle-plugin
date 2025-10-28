package asia.hombre.neorust.build

import org.gradle.api.tasks.Input

open class BuildTarget(val target: String) {
    @get:Input
    var linker: String? = null

    @get:Input
    var runner: String? = null

    @get:Input
    var rustFlags: MutableList<String> = mutableListOf()

    @get:Input
    var rustDocFlags: MutableList<String> = mutableListOf()

    @get:Input
    var copyTo: String? = null

    @get:Input
    val inclusions: MutableSet<String> = mutableSetOf("dll", "so", "dylib", "a")
}