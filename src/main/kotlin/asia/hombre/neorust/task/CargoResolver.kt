package asia.hombre.neorust.task

import asia.hombre.neorust.Rust
import asia.hombre.neorust.RustCrate
import asia.hombre.neorust.extension.RustExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

open class CargoResolver: DefaultTask() {
    @TaskAction
    private fun resolve() {
        //Pass the parsed dependencies to the plugin's extension
        project.extensions.getByType(RustExtension::class.java).apply {
            dependencies = parseAllDependencies(this, implementationName.ifBlank {
                throw IllegalStateException("The plugin's implementation configuration has not been properly created!")
            })
            devDependencies = parseAllDependencies(this, devOnlyName.ifBlank {
                throw IllegalStateException("The plugin's devOnly configuration has not been properly created!")
            })
            buildDependencies = parseAllDependencies(this, buildOnlyName.ifBlank {
                throw IllegalStateException("The plugin's buildOnly configuration has not been properly created!")
            })

            if(Rust.IS_TEST_ENVIRONMENT || Rust.IS_DEBUG_ENVIRONMENT) {
                dependencies.forEach {
                    println("Dependency: $it")
                }
                devDependencies.forEach {
                    println("Dev Dependency: $it")
                }
                buildDependencies.forEach {
                    println("Build Dependency: $it")
                }
            }
        }
    }

    private fun parseAllDependencies(extension: RustExtension, configurationName: String): MutableList<RustCrate> {
        val config = project.configurations.getByName(configurationName)
        val crates = mutableListOf<RustCrate>()

        config.dependencies.forEach { crate: Dependency ->
            if(crate is RustCrate)
                crates.add(crate)
        }

        return crates
    }

    /*private fun parseRawCrate(extension: RustExtension, list: MutableList<RustCrate>, rawCrate: RustCrate) {
        list.add(
            RustCrate(
                rawCrate.name,
                rawCrate.version?: throw IllegalArgumentException("Crate version is required!"),
                rawCrate.group?.let {
                    //Check if the provided group might be a directory
                    //Check if rust is set to offline mode
                    //Check if we have internet and check if we can reach the registry
                    if(!project.layout.projectDirectory.asFile.resolve(it).isDirectory &&
                        ((!extension.offline || hasInternet) && !pingHost(it)))
                        throw IllegalArgumentException(
                            "Crate registry(group) is not a resolvable path or a reachable host!"
                        )
                    return@let it
                }?: throw IllegalArgumentException(
                    "Crate registry(group) is required!\nUse the relative local path to declare local crates."
                )
            )
        )
    }*/

    /*private fun pingHost(hostname: String): Boolean {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(hostname, 80), 1000)
                return true
            }
        } catch (_: IOException) {
            return false
        }
    }

    //We only need to know if one of them succeeds
    private fun isOnline(): Boolean {
        try {
            Socket().use { socket ->
                //International
                socket.connect(InetSocketAddress("google.com", 80), 300)
                return true
            }
        } catch (_: IOException) {
            if(Rust.IS_DEBUG_ENVIRONMENT)
                println("Warning: google.com connection failed.")
        }

        try {
            Socket().use { socket ->
                //China (In case this is run within China's Firewall)
                socket.connect(InetSocketAddress("baidu.com", 80), 300)
                return true
            }
        } catch (_: IOException) {
            if(Rust.IS_DEBUG_ENVIRONMENT)
                println("Warning: baidu.com connection failed.")
        }

        try {
            Socket().use { socket ->
                //Russia (In case this is run within Russia if they are internet restricted)
                socket.connect(InetSocketAddress("yandex.com", 80), 300)
                return true
            }
        } catch (_: IOException) {
            if(Rust.IS_DEBUG_ENVIRONMENT)
                println("Warning: yandex.com connection failed.")
        }

        try {
            Socket().use { socket ->
                //For good measure
                socket.connect(InetSocketAddress("amazon.com", 80), 300)
                return true
            }
        } catch (_: IOException) {
            if(Rust.IS_DEBUG_ENVIRONMENT)
                println("Warning: amazon.com connection failed.")
        }

        return false
    }*/
}