package asia.hombre.neorust.options

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

open class RustManifestOptions(project: Project) {

    @Internal
    internal val packageConfig = Package(project)
    @Internal
    internal val libConfig = Library(project)

    fun packaging(packageConfig: Action<Package>) {
        packageConfig.execute(this.packageConfig)
    }

    fun lib(libConfig: Action<Library>) {
        libConfig.execute(this.libConfig)
    }

    open class Package(private val project: Project) {
        //TODO: Add stricter checks for each field. For example, throw an error if name is set to blank.
        @get:Input
        var name: String = project.name

        @get:Input
        var version: String = project.version.toString().let { if(it == "unspecified") "0.0.0" else it }

        @get:Input
        var authors: MutableList<String> = mutableListOf()

        @get:Input
        var edition: String = "2015"

        @get:Input
        var rustVersion: String = ""

        @get:Input
        var description: String = project.description?: ""

        @get:Input
        var documentation: String = ""

        @get:Input
        var readme: String = ""

        @get:Input
        var homepage: String = ""

        @get:Input
        var repository: String = ""

        @get:Input
        var license: String = ""

        @get:Input
        var licenseFile: String = ""

        @get:Input
        var keywords: MutableList<String> = mutableListOf()

        @get:Input
        var categories: MutableList<String> = mutableListOf()

        @get:Input
        var workspace: String = ""

        @get:Input
        var buildFile: String = ""

        @get:Input
        var links: String = ""

        @get:Input
        var exclude: MutableList<String> = mutableListOf("/build")

        @get:Input
        var include: MutableList<String> = mutableListOf()

        @get:Input
        var publish: MutableList<String> = mutableListOf()

        @get:Input
        var publishEnabled: Boolean = true

        @get:Input
        var defaultRun: String = ""

        @get:Input
        var autoBins: Boolean = true

        @get:Input
        var autoExamples: Boolean = true

        @get:Input
        var autoTests: Boolean = true

        @get:Input
        var autoBenches: Boolean = true
    }

    open class Library(private val project: Project) {
        @get:Input
        var crateType: MutableList<String> = mutableListOf()

        @get:Input
        var path: String = project.layout.projectDirectory.dir("src").dir("main").dir("rust").file("lib.rs").asFile.absolutePath
    }
}