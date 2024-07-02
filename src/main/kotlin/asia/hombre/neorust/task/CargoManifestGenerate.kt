package asia.hombre.neorust.task

import asia.hombre.neorust.RustCrate
import asia.hombre.neorust.extension.RustExtension
import asia.hombre.neorust.internal.CargoDefaultTask
import java.io.File

open class CargoManifestGenerate: CargoDefaultTask() {

    override fun cargoTaskAction() {
        project.extensions.getByType(RustExtension::class.java).apply {
            val cargoToml = File(manifestPath)
            //Create parent directories if they don't exist.
            cargoToml.parentFile.mkdirs()
            //Delete old Cargo.toml since it might be outdated.
            if(cargoToml.exists()) cargoToml.delete()

            val manifestOptions = rustManifestOptions

            val content = StringBuilder()
            val packageOptions = manifestOptions.packageConfig

            writeTable(content, "package") {
                if(packageOptions.name.isBlank())
                    throw IllegalArgumentException("Package 'name' cannot be unset.")

                writeField("name", packageOptions.name)
                writeField("version", packageOptions.version)
                writeArrayField("authors", packageOptions.authors)
                writeField("edition", packageOptions.edition)
                writeField("rust-version", packageOptions.rustVersion)
                writeField("description", packageOptions.description)
                writeField("documentation", packageOptions.documentation)
                writeField("readme", packageOptions.readme)
                writeField("homepage", packageOptions.homepage)
                writeField("repository", packageOptions.repository)
                writeField("license", packageOptions.license)
                writeField("license-file", packageOptions.licenseFile)
                writeArrayField("keywords", packageOptions.keywords)
                writeArrayField("categories", packageOptions.categories)
                writeField("workspace", packageOptions.workspace)
                writeField("build", packageOptions.buildFile)
                writeField("links", packageOptions.links)
                writeArrayField("exclude", packageOptions.exclude)
                writeArrayField("include", packageOptions.include)

                when {
                    !packageOptions.publishEnabled -> append("publish = false\n")
                    packageOptions.publish.isNotEmpty() -> writeArrayField("publish", packageOptions.publish)
                }

                writeField("default-run", packageOptions.defaultRun)
                writeBooleanField("autobins", packageOptions.autoBins)
                writeBooleanField("autoexamples", packageOptions.autoExamples)
                writeBooleanField("autotests", packageOptions.autoTests)
                writeBooleanField("autobenches", packageOptions.autoBenches)
            }

            if(dependencies.isNotEmpty()) {
                writeTable(content, "dependencies") {
                    dependencies.forEach { rustCrate ->
                        writeCrateField(rustCrate)
                    }
                }
            }

            if(devDependencies.isNotEmpty()) {
                writeTable(content, "dev-dependencies") {
                    devDependencies.forEach { rustCrate ->
                        writeCrateField(rustCrate)
                    }
                }
            }

            if(buildDependencies.isNotEmpty()) {
                writeTable(content, "build-dependencies") {
                    buildDependencies.forEach { rustCrate ->
                        writeCrateField(rustCrate)
                    }
                }
            }

            val libOptions = manifestOptions.libConfig

            writeTable(content, "lib") {
                writeField("path", libOptions.path)
                writeArrayField("crate-type", libOptions.crateType)
            }

            //TODO: Resolve custom registries
            /*writeTable(content, "registries") {
                writeField("path", libOptions.path)
                writeArrayField("crate-type", libOptions.crateType)
            }*/

            cargoToml.writeText(content.removePrefix("\n").toString())
        }
    }

    private fun writeTable(builder: StringBuilder, name: String, block: StringBuilder.() -> Unit) {
        builder.append("\n[$name]\n")
        builder.block()
    }

    private fun StringBuilder.writeField(key: String, value: String?) {
        if (!value.isNullOrBlank()) {
            append("$key = \"$value\"\n".replace("\\", "\\\\"))
        }
    }

    private fun StringBuilder.writeArrayField(key: String, values: List<String>) {
        if (values.isNotEmpty()) {
            append("$key = [")
            values.forEachIndexed { index, value ->
                if (index > 0) append(", ")
                append("\"${value.trim()}\"")
            }
            append("]\n")
        }
    }

    private fun StringBuilder.writeBooleanField(key: String, value: Boolean, defaultValue: Boolean = true) {
        if (value != defaultValue) {
            append("$key = $value\n")
        }
    }

    private fun StringBuilder.writeCrateField(crate: RustCrate) {
        if(crate.options.path.isBlank() &&
            crate.options.registry.isBlank() &&
            crate.options.features.isEmpty() &&
            crate.options.defaultFeatures &&
            !crate.options.optional)
            append("${crate.name} = \"${crate.version}\"\n")
        else {
            val crateOptions = mutableListOf<String>()

            if(crate.options.version.isNotBlank())
                crateOptions.add("version = \"${crate.options.version}\"")

            if(crate.options.path.isNotBlank())
                crateOptions.add("path = \"${crate.options.path}\"")

            if(crate.options.registry.isNotBlank())
                crateOptions.add("registry = \"${crate.options.registry}\"")

            if(crate.options.features.isNotEmpty())
                crateOptions.add("features = [${crate.options.features.joinToString(", ") { "\"$it\""}}]")

            if(!crate.options.defaultFeatures)
                crateOptions.add("default-features = false")

            if(crate.options.optional)
                crateOptions.add("optional = true")

            append("${crate.name} = { ${crateOptions.joinToString(", ")} }\n")
        }
    }
}